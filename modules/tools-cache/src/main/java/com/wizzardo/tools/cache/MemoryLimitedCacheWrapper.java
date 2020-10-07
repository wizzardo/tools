package com.wizzardo.tools.cache;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: wizzardo
 * Date: 24.11.14
 */
public class MemoryLimitedCacheWrapper<K, V extends MemoryLimitedCacheWrapper.SizeProvider> extends CacheWrapper<K, V> {
    public interface SizeProvider {
        long size();
    }

    private final long limit;
    private AtomicLong size = new AtomicLong();

    public MemoryLimitedCacheWrapper(AbstractCache<K, V> cache, long limit) {
        super(cache);
        this.limit = limit;
    }

    @Override
    public void onAddItem(K k, V v) {
        if (v != null)
            updateSize(v, true);
        super.onAddItem(k, v);
    }

    private void updateSize(V v, boolean increment) {
        long add = increment ? checkAndGetSize(v) : -checkAndGetSize(v);
        if (size.addAndGet(add) > limit)
            getThis().removeOldest();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        if (v != null)
            updateSize(v, false);
        super.onRemoveItem(k, v);
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
}
