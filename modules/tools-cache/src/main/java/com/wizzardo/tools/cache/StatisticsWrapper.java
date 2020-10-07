package com.wizzardo.tools.cache;

public class StatisticsWrapper<K, V> extends CacheWrapper<K, V> {
    protected final CacheStatistics statistics;

    public StatisticsWrapper(AbstractCache<K, V> cache) {
        super(cache);
        statistics = createStatistics();
    }

    public CacheStatistics getStatistics() {
        return statistics;
    }

    protected CacheStatistics createStatistics() {
        return new CacheStatistics(this);
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
