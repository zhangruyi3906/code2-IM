package com.lh.im.common.util.redis;

import cn.hutool.json.JSONUtil;
import com.lh.im.common.util.ExecuteUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 有些场景获取锁和释放锁都在不通的JVM里面，
 * 这里提供一个不需要校验redis value的Lock
 * 注意：这里不提供看门狗机制，业务上自己定义好redis ttl
 *
 * @author dengxiaolin
 * @since 2022/09/26
 */
@Slf4j
@Component("imCommonRedisLock")
public class CommonRedisLock {
    private static final String DEFAULT_LOCK_VALUE = "common_lock";
    /**
     * 默认锁30分钟
     */
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(30L);

    private static final DefaultRedisScript<Boolean> EXPIRE_SCRIPT;
    private static final DefaultRedisScript<Boolean> RELEASE_SCRIPT;

    static {
        EXPIRE_SCRIPT = new DefaultRedisScript<>();
        EXPIRE_SCRIPT.setResultType(Boolean.class);
        EXPIRE_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/expire.lua")));

        RELEASE_SCRIPT = new DefaultRedisScript<>();
        RELEASE_SCRIPT.setResultType(Boolean.class);
        RELEASE_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/release.lua")));
    }

    /**
     * io 密集型，线程数稍微多点
     */
    private static final ThreadPoolExecutor REDIS_LOCK_EXECUTOR = new ThreadPoolExecutor(
            16,
            128,
            1,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(1024, true),
            new ThreadFactory() {
                private final AtomicInteger atomicInteger = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "thread-redis-lock-" + atomicInteger.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    private static final ScheduledThreadPoolExecutor WATCH_DOG_EXECUTOR = new ScheduledThreadPoolExecutor(
            8,
            new ThreadFactory() {
                private final AtomicInteger atomicInteger = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "redis-multi-lock-watch-dog-" + atomicInteger.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy());
    private static final long DEFAULT_SLEEP_TIME = 2000L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 慎重使用，效率很低
     */
    public void lock(Set<String> keySet, Duration lockValueDuration) {
        lock(keySet, null, lockValueDuration);
    }

    public MultiWatchDogTask lock(Set<String> keySet, String lockValue, Duration lockValueDuration) {
        if (CollectionUtils.isEmpty(keySet)) {
            throw new RuntimeException("keySet is required");
        }
        String finalLockValue = StringUtils.isBlank(lockValue) ? DEFAULT_LOCK_VALUE : lockValue;
        Duration finalLockValueDuration = lockValueDuration == null ? DEFAULT_LOCK_DURATION : lockValueDuration;

        List<String> keys = new ArrayList<>(keySet);
        // 排序避免死锁
        Collections.sort(keys);
        log.debug("get distribute lock {}", JSONUtil.toJsonStr(keys));

        MultiWatchDogTask multiWatchDogTask = null;
        boolean needWatchDog = !DEFAULT_LOCK_VALUE.equals(lockValue);
        Set<String> lockedKeySet = new LinkedHashSet<>(keySet.size() * 2);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            while (true) {
                try {
                    Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, finalLockValue, finalLockValueDuration);
                    log.info("add lock {}, result {}", key, success);
                    if (success != null && success) {
                        lockedKeySet.add(key);
                        break;
                    }
                } catch (Exception e) {
                    log.error("执行redis加锁脚本异常", e);
                }
            }

            if (needWatchDog && i == 0) {
                multiWatchDogTask = new MultiWatchDogTask(stringRedisTemplate, lockedKeySet, lockValue, lockValueDuration);
                WATCH_DOG_EXECUTOR.schedule(multiWatchDogTask, DEFAULT_SLEEP_TIME, TimeUnit.MILLISECONDS);
            }
        }
        return multiWatchDogTask;
    }

    public Set<String> tryLock(Set<String> keySet, Duration lockValueDuration) {
        return tryLock(keySet, null, lockValueDuration).getLeft();
    }

    public Pair<Set<String>, MultiWatchDogTask> tryLock(Set<String> keySet, String lockValue, Duration lockValueDuration) {
        if (CollectionUtils.isEmpty(keySet)) {
            throw new RuntimeException("keySet is required");
        }
        String finalLockValue = StringUtils.isBlank(lockValue) ? DEFAULT_LOCK_VALUE : lockValue;
        Duration finalLockValueDuration = lockValueDuration == null ? DEFAULT_LOCK_DURATION : lockValueDuration;

        Set<String> lockedKeySet = new HashSet<>(keySet.size() * 2);
        MultiWatchDogTask multiWatchDogTask = null;
        synchronized (this) {
            CompletionService<String> completionService = new ExecutorCompletionService<>(REDIS_LOCK_EXECUTOR);
            keySet.forEach(key ->
                    completionService.submit(() -> {
                        try {
                            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, finalLockValue, finalLockValueDuration);
                            log.info("add lock {}, result {}", key, success);
                            return success != null && success ? key : "";
                        } catch (Exception e) {
                            log.error("执行redis加锁脚本异常", e);
                            return "";
                        }
                    })
            );

            boolean addWatchDog = false;
            boolean needWatchDog = !DEFAULT_LOCK_VALUE.equals(finalLockValue);
            for (String key : keySet) {
                String lockedKey = ExecuteUtils.submitQuietly(() -> completionService.take().get());
                if (StringUtils.isNotBlank(lockedKey)) {
                    lockedKeySet.add(lockedKey);
                    if (needWatchDog && !addWatchDog) {
                        addWatchDog = true;
                        multiWatchDogTask = new MultiWatchDogTask(stringRedisTemplate, lockedKeySet, lockValue, lockValueDuration);
                        WATCH_DOG_EXECUTOR.schedule(multiWatchDogTask, DEFAULT_SLEEP_TIME, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
        return Pair.of(lockedKeySet, multiWatchDogTask);
    }

    public void unlock(Set<String> keySet) {
        unlock(keySet, DEFAULT_LOCK_VALUE);
    }

    public void unlock(Set<String> keySet, String lockValue) {
        if (CollectionUtils.isEmpty(keySet)) {
            return;
        }

        String keysJson = JSONUtil.toJsonStr(keySet);
        log.debug("release distribute lock {}", keysJson);

        CompletionService<String> completionService = new ExecutorCompletionService<>(REDIS_LOCK_EXECUTOR);
        keySet.forEach(key ->
                completionService.submit(() -> {
                    try {
                        Boolean success = stringRedisTemplate.execute(RELEASE_SCRIPT, Arrays.asList(key), lockValue);
                        log.info("release lock {}, result {}", key, success);
                        return key;
                    } catch (Exception e) {
                        log.error("release lock {} error", key, e);
                        return "";
                    }
                })
        );
        keySet.forEach(key -> ExecuteUtils.executeQuietly(() -> completionService.take().get()));
    }

    @Getter
    @Setter
    public static class MultiWatchDogTask implements Runnable {
        private final StringRedisTemplate stringRedisTemplate;
        private final Set<String> lockedKeys;
        private final String lockedValue;
        private final Duration lockDuration;
        private volatile boolean stopped = false;

        public MultiWatchDogTask(StringRedisTemplate stringRedisTemplate, Set<String> lockedKeys, String lockedValue, Duration lockDuration) {
            this.stringRedisTemplate = stringRedisTemplate;
            this.lockedKeys = lockedKeys;
            this.lockedValue = lockedValue;
            this.lockDuration = lockDuration;
        }

        @Override
        public void run() {
            if (stopped) {
                return;
            }

            if (CollectionUtils.isEmpty(lockedKeys)) {
                return;
            }

            String lockSeconds = String.valueOf(lockDuration.getSeconds());
            CompletionService<String> completionService = new ExecutorCompletionService<>(REDIS_LOCK_EXECUTOR);

            lockedKeys.forEach(key ->
                    completionService.submit(() -> {
                        try {
                            stringRedisTemplate.execute(EXPIRE_SCRIPT, Arrays.asList(key), lockedValue, lockSeconds);
                        } catch (Exception e) {
                            log.error("expire key {} error", key, e);
                        }
                        return "";
                    })
            );
            lockedKeys.forEach(key -> ExecuteUtils.executeQuietly(() -> completionService.take().get()));

            if (!stopped) {
                WATCH_DOG_EXECUTOR.schedule(this, DEFAULT_SLEEP_TIME, TimeUnit.MILLISECONDS);
            }
        }
    }
}
