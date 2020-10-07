package com.wizzardo.tools.cache;

import java.util.concurrent.atomic.AtomicLong;

public class StatisticsWithHeapUsageWrapper<K, V extends MemoryLimitedCacheWrapper.SizeProvider> extends CacheWrapper<K, V> {
    protected final CacheStatisticsWithHeapUsage statistics;

    public static class CacheStatisticsWithHeapUsage extends CacheStatistics {
        protected final AtomicLong heapUsage = new AtomicLong();

        public CacheStatisticsWithHeapUsage(AbstractCache cache) {
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

    public StatisticsWithHeapUsageWrapper(AbstractCache<K, V> cache) {
        super(cache);
        statistics = createStatistics();
    }

    public CacheStatisticsWithHeapUsage getStatistics() {
        return statistics;
    }

    protected CacheStatisticsWithHeapUsage createStatistics() {
        return new CacheStatisticsWithHeapUsage(this);
    }

    @Override
    public void onRemoveItem(K k, V v) {
        long latency = System.nanoTime();
        try {
            super.onRemoveItem(k, v);
        } finally {
            latency = System.nanoTime() - latency;
            statistics.removeCount.incrementAndGet();
            statistics.removeLatency.addAndGet(latency);
            statistics.heapUsage.addAndGet(-v.size());
            updateSizeMetric();
        }
    }

    @Override
    public void onAddItem(K k, V v) {
        long latency = System.nanoTime();
        try {
            super.onAddItem(k, v);
        } finally {
            latency = System.nanoTime() - latency;
            statistics.putCount.incrementAndGet();
            statistics.putLatency.addAndGet(latency);
            statistics.heapUsage.addAndGet(v.size());
            updateSizeMetric();
        }
    }

    @Override
    protected void compute(K key, Computable<? super K, ? extends V> c, Holder<K, V> ft) throws Exception {
        long latency = System.nanoTime();
        try {
            super.compute(key, c, ft);
        } finally {
            latency = System.nanoTime() - latency;
            statistics.computeCount.incrementAndGet();
            statistics.computeLatency.addAndGet(latency);
        }
    }

    @Override
    protected Holder<K, V> getHolderFromCache(K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        long latency = System.nanoTime();
        try {
            return super.getHolderFromCache(key, c, updateTTL);
        } finally {
            latency = System.nanoTime() - latency;
            statistics.getCount.incrementAndGet();
            statistics.getLatency.addAndGet(latency);
        }
    }

    protected void updateSizeMetric() {
        statistics.size.set(size());
    }
}
