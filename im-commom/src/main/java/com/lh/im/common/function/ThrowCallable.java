package com.lh.im.common.function;

/**
 * @author dengxiaolin
 * @since 2021/01/15
 */
@FunctionalInterface
public interface ThrowCallable<T> {
    T call() throws Throwable;
}
