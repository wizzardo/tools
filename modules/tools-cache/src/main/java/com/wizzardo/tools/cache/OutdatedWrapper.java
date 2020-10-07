package com.wizzardo.tools.cache;

public class OutdatedWrapper<K, V> extends CacheWrapper<K, V> {
    protected AbstractCache<K, V> outdatedCache;

    public OutdatedWrapper(AbstractCache<K, V> cache, AbstractCache<K, V> outdatedCache) {
        super(cache);
        this.outdatedCache = outdatedCache;
    }

    @Override
    public void onAddItem(K k, V v) {
        super.onAddItem(k, v);
        outdatedCache.remove(k);
    }

    @Override
    public void onRemoveItem(K k, V v) {
        outdatedCache.put(k, v);
        super.onRemoveItem(k, v);
    }

    @Override
    protected Holder<K, V> getHolderFromCache(K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        Holder<K, V> holder = super.getHolderFromCache(key, c, updateTTL);
        if (!holder.done) {
            Holder<K, V> h = outdatedCache.getHolder(key);
            if (h != null)
                return h;
        }
        return holder;
    }

    @Override
    public void destroy() {
        super.destroy();
        outdatedCache.destroy();
    }
}
