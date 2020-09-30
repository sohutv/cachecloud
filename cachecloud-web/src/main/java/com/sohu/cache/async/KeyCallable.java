package com.sohu.cache.async;

import java.util.concurrent.Callable;

/**
 * Created by yijunzhang on 14-6-18.
 */
public abstract class KeyCallable<V> implements Callable<V> {
    private final String key;

    private volatile boolean cancelled = false;

    public KeyCallable(String key) {
        this.key = key;
    }

    public abstract V execute();

    @Override
    public V call() throws Exception {
        if (!cancelled) {
            V v =  execute();
            return v;
        }
        return null;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public String getKey() {
        return key;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
