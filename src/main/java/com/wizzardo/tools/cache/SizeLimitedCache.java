package main.java.com.wizzardo.tools.cache;

import com.wizzardo.tools.cache.Cache;
import com.wizzardo.tools.cache.Computable;

/**
 * @author: wizzardo
 * Date: 24.11.14
 */
public class SizeLimitedCache<K, V> extends Cache<K, V> {

    private final int limit;

    public SizeLimitedCache(int limit, long ttlSec, Computable<? super K, ? extends V> computable) {
        super(ttlSec, computable);
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be > 0");

        this.limit = limit;
    }

    public SizeLimitedCache(int limit, long ttlSec) {
        this(limit, ttlSec, null);
    }

    @Override
    public void onAddItem(K k, V v) {
        if (size() > limit)
            removeOldest();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        if (size() > limit)
            removeOldest();
    }

    public int limit() {
        return limit;
    }
}
