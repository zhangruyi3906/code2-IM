package com.lh.im.common.util.redis;

import com.lh.im.common.util.UuidUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * @author dengxiaolin
 * @since 2021/10/14
 */
@Slf4j
@Component("imRedisMultiLockTemplate")
public class RedisMultiLockTemplate {
    /**
     * 默认锁2分钟
     */
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(2L);

    @Autowired
    @Qualifier("imCommonRedisLock")
    private CommonRedisLock commonRedisLock;

    public <T> T tryLockWithReturn(Set<String> keySet, Supplier<T> supplier) {
        String lockValue = UuidUtils.genUuid();
        Pair<Set<String>, CommonRedisLock.MultiWatchDogTask> lockedKeysPair = commonRedisLock.tryLock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        Set<String> lockedKeys = lockedKeysPair.getLeft();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = lockedKeysPair.getRight();

        try {
            if (lockedKeys.size() == keySet.size()) {
                return supplier.get();
            }
            else {
                log.info("竞争分布式锁失败，keySet:{}", keySet);
                throw new RuntimeException("系统繁忙，请稍后再重试");
            }
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(lockedKeys, lockValue);
        }
    }

    public void tryLock(Set<String> keySet, Runnable runnable) {
        String lockValue = UuidUtils.genUuid();
        Pair<Set<String>, CommonRedisLock.MultiWatchDogTask> lockedKeysPair = commonRedisLock.tryLock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        Set<String> lockedKeys = lockedKeysPair.getLeft();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = lockedKeysPair.getRight();

        try {
            if (lockedKeys.size() == keySet.size()) {
                runnable.run();
            }
            else {
                log.info("竞争分布式锁失败，keySet:{}", keySet);
                throw new RuntimeException("系统繁忙，请稍后再重试");
            }
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(lockedKeys, lockValue);
        }
    }

    /**
     * 适用在获取部分锁的场景
     * 尽可能获得多的锁，然后根据获得锁来执行业务
     */
    public void tryLock(Set<String> keySet, Consumer<Set<String>> consumer) {
        String lockValue = UuidUtils.genUuid();
        Pair<Set<String>, CommonRedisLock.MultiWatchDogTask> lockedKeysPair = commonRedisLock.tryLock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        Set<String> lockedKeys = lockedKeysPair.getLeft();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = lockedKeysPair.getRight();
        try {
            consumer.accept(lockedKeys);
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(lockedKeys, lockValue);
        }
    }

    /**
     * 适用在获取部分锁的场景
     * 尽可能获得多的锁，然后根据获得锁来执行业务
     */
    public <T> T tryLockWithReturn(Set<String> keySet, Function<Set<String>, T> function) {
        String lockValue = UuidUtils.genUuid();
        Pair<Set<String>, CommonRedisLock.MultiWatchDogTask> lockedKeysPair = commonRedisLock.tryLock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        Set<String> lockedKeys = lockedKeysPair.getLeft();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = lockedKeysPair.getRight();
        try {
            return function.apply(lockedKeys);
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(lockedKeys, lockValue);
        }
    }

    public <T> T lockWithReturn(Set<String> keySet, Supplier<T> supplier) {
        String lockValue = UuidUtils.genUuid();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = commonRedisLock.lock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        try {
            return supplier.get();
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(keySet, lockValue);
        }
    }

    public void lock(Set<String> keySet, Runnable runnable) {
        String lockValue = UuidUtils.genUuid();
        CommonRedisLock.MultiWatchDogTask multiWatchDogTask = commonRedisLock.lock(keySet, lockValue, DEFAULT_LOCK_DURATION);
        try {
            runnable.run();
        }
        finally {
            if (multiWatchDogTask != null) {
                multiWatchDogTask.setStopped(true);
            }
            commonRedisLock.unlock(keySet, lockValue);
        }
    }
}
