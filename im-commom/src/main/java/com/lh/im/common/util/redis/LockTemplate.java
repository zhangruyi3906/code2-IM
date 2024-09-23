package com.lh.im.common.util.redis;

import java.util.function.Supplier;

/**
 * @author dengxiaolin
 * @since 2021/06/18
 */
public interface LockTemplate {
    <T> T tryLockWithReturn(String key, Supplier<T> supplier);

    void tryLock(String key, Runnable runnable);

    <T> T lockWithReturn(String key, Supplier<T> supplier);

    void lock(String lockKey, Runnable runnable);
}
