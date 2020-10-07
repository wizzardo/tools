package com.wizzardo.tools.cache;

/**
 * @author: wizzardo
 * Date: 24.11.14
 */
public class SizeLimitedCacheWrapper<K, V> extends CacheWrapper<K, V> {

    private final int limit;

    public SizeLimitedCacheWrapper(AbstractCache<K, V> cache, int limit) {
        super(cache);
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be > 0");

        this.limit = limit;
    }

    @Override
    public void onAddItem(K k, V v) {
        try {
            super.onAddItem(k, v);
        } finally {
            if (size() > limit)
                getThis().removeOldest();
        }
    }

    @Override
    public void onRemoveItem(K k, V v) {
        try {
            super.onRemoveItem(k, v);
        } finally {
            if (size() > limit)
                getThis().removeOldest();
        }
    }

    public int limit() {
        return limit;
    }
}
