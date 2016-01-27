package com.sohu.cache.async;

import java.util.concurrent.Future;

/**
 * Created by yijunzhang on 15-1-20.
 */
public class KeyFuture<V> {
    private final String key;
    private final Future<V> future;

    public KeyFuture(String key, Future<V> future) {
        this.key = key;
        this.future = future;
    }

    public String getKey() {
        return key;
    }

    public Future<V> getFuture() {
        return future;
    }
}
