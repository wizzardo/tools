package com.wizzardo.tools.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by wizzardo on 08/10/16.
 */
public class CacheStatistics {
    protected final AtomicLong getCount = new AtomicLong();
    protected final AtomicLong putCount = new AtomicLong();
    protected final AtomicLong removeCount = new AtomicLong();
    protected final AtomicLong computeCount = new AtomicLong();
    protected final AtomicLong getLatency = new AtomicLong();
    protected final AtomicLong putLatency = new AtomicLong();
    protected final AtomicLong removeLatency = new AtomicLong();
    protected final AtomicLong computeLatency = new AtomicLong();
    protected final AtomicInteger size = new AtomicInteger();
    protected final String cacheName;

    public CacheStatistics(String cacheName) {
        this.cacheName = cacheName;
    }

    public long getGetCount() {
        return getCount.get();
    }

    public long getPutCount() {
        return putCount.get();
    }

    public long getRemoveCount() {
        return removeCount.get();
    }

    public long getComputeCount() {
        return computeCount.get();
    }

    public long getGetLatency() {
        return getLatency.get();
    }

    public long getPutLatency() {
        return putLatency.get();
    }

    public long getRemoveLatency() {
        return removeLatency.get();
    }

    public long getComputeLatency() {
        return computeLatency.get();
    }

    public int getSize() {
        return size.get();
    }

    public String getCacheName() {
        return cacheName;
    }
}
