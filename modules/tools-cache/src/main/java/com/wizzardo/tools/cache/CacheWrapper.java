package com.wizzardo.tools.cache;

public class CacheWrapper<K, V> extends AbstractCache<K, V> {
    protected final AbstractCache<K, V> cache;

    public CacheWrapper(AbstractCache<K, V> cache) {
        this.cache = cache;
        cache.setThis(this);
    }

    @Override
    protected Holder<K, V> getHolderFromCache(K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        return cache.getHolderFromCache(key, c, updateTTL);
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public V get(K k) {
        return cache.get(k);
    }

    @Override
    public V get(K k, boolean updateTTL) {
        return cache.get(k, updateTTL);
    }

    @Override
    public V get(K k, Computable<? super K, ? extends V> computable) {
        return cache.get(k, computable);
    }

    @Override
    public V get(K k, Computable<? super K, ? extends V> computable, boolean updateTTL) {
        return cache.get(k, computable, updateTTL);
    }

    @Override
    public Holder<K, V> getHolder(K k) {
        return cache.getHolder(k);
    }

    @Override
    public Holder<K, V> getHolder(K k, boolean updateTTL) {
        return cache.getHolder(k, updateTTL);
    }

    @Override
    public Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable) {
        return cache.getHolder(k, computable);
    }

    @Override
    public Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable, boolean updateTTL) {
        return cache.getHolder(k, computable, updateTTL);
    }

    @Override
    public void setRemoveOnException(boolean removeOnException) {
        cache.setRemoveOnException(removeOnException);
    }

    @Override
    public boolean isRemoveOnException() {
        return cache.isRemoveOnException();
    }

    @Override
    public V remove(K k) {
        return cache.remove(k);
    }

    @Override
    public void refresh() {
        cache.refresh();
    }

    @Override
    public AbstractCache<K, V> onErrorDuringRefresh(CacheErrorListener errorListener) {
        return cache.onErrorDuringRefresh(errorListener);
    }

    @Override
    public void destroy() {
        cache.destroy();
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        cache.onRemoveItem(k, v);
    }

    @Override
    public void onAddItem(K k, V v) {
        cache.onAddItem(k, v);
    }

    @Override
    public AbstractCache<K, V> onAdd(CacheListener<? super K, ? super V> onAdd) {
        return cache.onAdd(onAdd);
    }

    @Override
    public AbstractCache<K, V> onRemove(CacheListener<? super K, ? super V> onRemove) {
        return cache.onRemove(onRemove);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public void put(K key, V value, long ttl) {
        cache.put(key, value, ttl);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    @Override
    public boolean putIfAbsent(K key, V value, long ttl) {
        return cache.putIfAbsent(key, value, ttl);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean contains(K key) {
        return cache.contains(key);
    }

    @Override
    public boolean isDestroyed() {
        return cache.isDestroyed();
    }

    @Override
    public long getTTL() {
        return cache.getTTL();
    }

    @Override
    public long getTTL(K k) {
        return cache.getTTL(k);
    }

    @Override
    public void removeOldest() {
        cache.removeOldest();
    }
}
