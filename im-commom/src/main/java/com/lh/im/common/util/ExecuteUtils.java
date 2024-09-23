package com.lh.im.common.util;

import com.lh.im.common.function.ThrowCallable;
import com.lh.im.common.function.ThrowRunnable;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * @author dengxiaolin
 * @since 2021/11/22
 */
@Slf4j
public class ExecuteUtils {
    public static <T> T submitQuietly(ThrowCallable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            log.error("execute exception", e);
        }

        return null;
    }

    public static <T> T submitQuietly(ThrowCallable<T> callable, Consumer<Throwable> callBack) {
        try {
            return callable.call();
        } catch (Throwable e) {
            executeQuietly(() -> callBack.accept(e));
        }

        return null;
    }

    public static <T> T submit(ThrowCallable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> T submit(ThrowCallable<T> callable, Consumer<Throwable> callBack) {
        try {
            return callable.call();
        } catch (Throwable e) {
            executeQuietly(() -> callBack.accept(e));
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static void executeQuietly(ThrowRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error("execute exception", e);
        }
    }

    public static void executeQuietly(ThrowRunnable runnable, Consumer<Throwable> callBack) {
        try {
            runnable.run();
        } catch (Throwable e) {
            executeQuietly(() -> callBack.accept(e));
        }
    }

    public static void execute(ThrowRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static void execute(ThrowRunnable runnable, Consumer<Throwable> callBack) {
        try {
            runnable.run();
        } catch (Throwable e) {
            executeQuietly(() -> callBack.accept(e));
            if (e instanceof RuntimeException) {
                throw ((RuntimeException) e);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}