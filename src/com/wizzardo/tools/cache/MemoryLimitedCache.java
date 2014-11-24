package com.wizzardo.tools.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: wizzardo
 * Date: 24.11.14
 */
public class MemoryLimitedCache<K, V extends MemoryLimitedCache.SizeProvider> extends Cache<K, V> {
    public static interface SizeProvider {
        long size();
    }

    private final long limit;
    private AtomicLong size = new AtomicLong();

    public MemoryLimitedCache(long limit, long ttlSec, Computable<? super K, ? extends V> computable) {
        super(ttlSec, computable);
        this.limit = limit;
    }

    public MemoryLimitedCache(long limit, long ttlSec) {
        this(limit, ttlSec, null);
    }

    @Override
    public void onAddItem(K k, V v) {
        if (v == null)
            return;

        if (size.addAndGet(checkAndGetSize(v)) > limit)
            removeOldest();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        if (v == null)
            return;

        if (size.addAndGet(-checkAndGetSize(v)) > limit)
            removeOldest();
    }

    private long checkAndGetSize(V v) {
        long l = v.size();
        if (l < 0)
            throw new IllegalStateException("value size must be > 0");
        return l;
    }

    public long limit() {
        return limit;
    }

    public long memoryUsed() {
        return size.get();
    }
}
