package com.lh.im.common.config.thread;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

/**
 *
 */
public class ImThreadFactoryBuilder implements ThreadFactory {

    private int counter;

    private final String name;

    public ImThreadFactoryBuilder(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = new Thread(r, name + "_Thread | " + counter);
        counter++;
        return thread;
    }
}
