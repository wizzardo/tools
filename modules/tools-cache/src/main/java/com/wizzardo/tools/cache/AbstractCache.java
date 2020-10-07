package com.wizzardo.tools.cache;

public abstract class AbstractCache<K, V> {

    protected AbstractCache<K, V> that = this;

    protected void setThis(AbstractCache<K, V> that) {
        this.that = that;
    }

    protected AbstractCache<K, V> getThis() {
        return that;
    }


    protected void compute(K key, Computable<? super K, ? extends V> c, Holder<K, V> ft) throws Exception {
        ft.compute(c, key);
    }

    protected abstract Holder<K, V> getHolderFromCache(final K key, Computable<? super K, ? extends V> c, boolean updateTTL);

    public abstract String getName();

    public abstract V get(K k);

    public abstract V get(K k, boolean updateTTL);

    public abstract V get(K k, Computable<? super K, ? extends V> computable);

    public abstract V get(K k, Computable<? super K, ? extends V> computable, boolean updateTTL);

    public abstract Holder<K, V> getHolder(K k);

    public abstract Holder<K, V> getHolder(K k, boolean updateTTL);

    public abstract Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable);

    public abstract Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable, boolean updateTTL);

    public abstract void setRemoveOnException(boolean removeOnException);

    public abstract boolean isRemoveOnException();

    public abstract V remove(K k);

    public abstract void refresh();

    public abstract AbstractCache<K, V> onErrorDuringRefresh(CacheErrorListener errorListener);

    public abstract void destroy();

    public abstract void clear();

    public abstract void onRemoveItem(K k, V v);

    public abstract void onAddItem(K k, V v);

    public abstract AbstractCache<K, V> onAdd(CacheListener<? super K, ? super V> onAdd);

    public abstract AbstractCache<K, V> onRemove(CacheListener<? super K, ? super V> onRemove);

    public abstract void put(K key, V value);

    public abstract void put(K key, V value, long ttl);

    public abstract boolean putIfAbsent(K key, V value);

    public abstract boolean putIfAbsent(K key, V value, long ttl);

    public abstract int size();

    public abstract boolean contains(K key);

    public abstract boolean isDestroyed();

    public abstract long getTTL();

    public abstract long getTTL(K k);

    public abstract void removeOldest();
}
