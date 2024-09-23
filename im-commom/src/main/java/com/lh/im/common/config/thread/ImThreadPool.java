package com.lh.im.common.config.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ImThreadPool {

    private final ExecutorService executorService;

    public ImThreadPool() {
        ThreadFactory customThreadFactoryBuilder = new ImThreadFactoryBuilder("IM_thread");
        int cpuNum = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolExecutor(
                cpuNum,
                20,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                customThreadFactoryBuilder,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public ExecutorService getPool() {
        return executorService;
    }
}
