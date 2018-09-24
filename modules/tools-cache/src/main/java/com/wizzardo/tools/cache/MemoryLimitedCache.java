package com.wizzardo.tools.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: wizzardo
 * Date: 24.11.14
 */
public class MemoryLimitedCache<K, V extends MemoryLimitedCache.SizeProvider> extends Cache<K, V> {
    public interface SizeProvider {
        long size();
    }

    private final long limit;
    private AtomicLong size = new AtomicLong();
    private CacheStatisticsWithHeapUsage statisticsWithHeapUsage;

    public MemoryLimitedCache(long limit, long ttlSec, Computable<? super K, ? extends V> computable) {
        this(null, limit, ttlSec, computable);
    }

    public MemoryLimitedCache(String name, long limit, long ttlSec, Computable<? super K, ? extends V> computable) {
        super(name, ttlSec, computable);
        this.limit = limit;
        statisticsWithHeapUsage = (CacheStatisticsWithHeapUsage) statistics;
    }

    @Override
    protected CacheStatistics createStatistics() {
        return new CacheStatisticsWithHeapUsage(this);
    }

    public MemoryLimitedCache(long limit, long ttlSec) {
        this(limit, ttlSec, null);
    }

    @Override
    public void onAddItem(K k, V v) {
        if (v != null)
            updateSize(v, true);
        super.onAddItem(k, v);
    }

    private void updateSize(V v, boolean increment) {
        long add = increment ? checkAndGetSize(v) : -checkAndGetSize(v);
        if (statisticsWithHeapUsage.update(size.addAndGet(add)) > limit)
            removeOldest();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        if (v != null)
            updateSize(v, false);
        super.onRemoveItem(k, v);
    }

    @Override
    public MemoryLimitedCache<K, V> onAdd(CacheListener<? super K, ? super V> onAdd) {
        super.onAdd(onAdd);
        return this;
    }

    @Override
    public MemoryLimitedCache<K, V> onRemove(CacheListener<? super K, ? super V> onRemove) {
        super.onRemove(onRemove);
        return this;
    }

    private long checkAndGetSize(V v) {
        long l = v.size();
        if (l < 0)
            throw new IllegalStateException("value's size must be >= 0");
        return l;
    }

    public long limit() {
        return limit;
    }

    public long memoryUsed() {
        return size.get();
    }

    public static class CacheStatisticsWithHeapUsage extends CacheStatistics {
        protected final AtomicLong heapUsage = new AtomicLong();

        public CacheStatisticsWithHeapUsage(Cache cache) {
            super(cache);
        }

        public long getHeapUsage() {
            return heapUsage.get();
        }

        public long update(long bytes) {
            heapUsage.set(bytes);
            return bytes;
        }
    }
}
