package com.wizzardo.tools.cache;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Cache<K, V> extends AbstractCache<K, V> {

    protected static final CacheListener<Object, Object> NOOP_LISTENER = new CacheListener<Object, Object>() {
        @Override
        public void onEvent(Object o, Object o2) {
        }
    };
    protected static final CacheErrorListener DEFAULT_ERROR_LISTENER = new CacheErrorListener() {
        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    };
    protected static final AtomicInteger NAME_COUNTER = new AtomicInteger(1);

    protected final ConcurrentHashMap<K, Holder<K, V>> map = new ConcurrentHashMap<K, Holder<K, V>>();
    protected final ConcurrentHashMap<Long, WeakReference<TimingsHolder<K, V>>> timings = new ConcurrentHashMap<Long, WeakReference<TimingsHolder<K, V>>>();
    protected final String name;
    protected long ttl;
    protected Computable<? super K, ? extends V> computable;
    protected volatile boolean removeOnException = true;
    protected volatile boolean destroyed;
    protected CacheListener<? super K, ? super V> onAdd = NOOP_LISTENER;
    protected CacheListener<? super K, ? super V> onRemove = NOOP_LISTENER;
    protected CacheErrorListener onErrorDuringRefresh = DEFAULT_ERROR_LISTENER;
    protected TimingsHolder<K, V> timingsHolder;

    public Cache(String name, long ttlSec, Computable<? super K, ? extends V> computable) {
        this.name = name != null ? name : "Cache-" + NAME_COUNTER.incrementAndGet();
        this.ttl = ttlSec * 1000;
        this.computable = computable;
        timings.put(ttl, new WeakReference<TimingsHolder<K, V>>(timingsHolder = new TimingsHolder<K, V>(ttl)));
        CacheCleaner.addCache(this);
    }

    public Cache(long ttlSec, Computable<? super K, ? extends V> computable) {
        this(null, ttlSec, computable);
    }

    public Cache(long ttlSec) {
        this(ttlSec, null);
    }

    public Cache(String name, long ttlSec) {
        this(name, ttlSec, null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V get(K k) {
        return getFromHolder(getThis().getHolder(k, computable, false));
    }

    @Override
    public V get(K k, boolean updateTTL) {
        return getFromHolder(getThis().getHolder(k, computable, updateTTL));
    }

    @Override
    public V get(K k, Computable<? super K, ? extends V> computable) {
        return getFromHolder(getThis().getHolder(k, computable, false));
    }

    @Override
    public V get(K k, Computable<? super K, ? extends V> computable, boolean updateTTL) {
        return getFromHolder(getThis().getHolder(k, computable, updateTTL));
    }

    protected V getFromHolder(Holder<K, V> holder) {
        return holder == null ? null : holder.get();
    }

    @Override
    public Holder<K, V> getHolder(K k) {
        return getThis().getHolderFromCache(k, computable, false);
    }

    @Override
    public Holder<K, V> getHolder(K k, boolean updateTTL) {
        return getThis().getHolderFromCache(k, computable, updateTTL);
    }

    @Override
    public Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable) {
        return getThis().getHolderFromCache(k, computable, false);
    }

    @Override
    public Holder<K, V> getHolder(K k, Computable<? super K, ? extends V> computable, boolean updateTTL) {
        return getThis().getHolderFromCache(k, computable, updateTTL);
    }

    @Override
    public void setRemoveOnException(boolean removeOnException) {
        this.removeOnException = removeOnException;
    }

    @Override
    public boolean isRemoveOnException() {
        return removeOnException;
    }

    @Override
    public V remove(K k) {
        Holder<K, V> holder = map.remove(k);
        if (holder == null)
            return null;

        holder.setRemoved();
        getThis().onRemoveItem(holder.k, holder.v);
        return holder.v;
    }

    @Override
    public void refresh() {
        refresh(System.currentTimeMillis());
    }

    synchronized long refresh(long time) {
        TimingEntry<Holder<K, V>> entry;
        Holder<K, V> h;
        long nextWakeUp = Long.MAX_VALUE;

        TimingsHolder<K, V> th;
        Iterator<WeakReference<TimingsHolder<K, V>>> i = timings.values().iterator();
        while ((th = next(i)) != null) {
            Queue<TimingEntry<Holder<K, V>>> timings = th.timings;

            while ((entry = timings.peek()) != null) {
                h = entry.value.get();
                if (h == null || h.isRemoved() || h.validUntil != entry.timing) {
                    timings.poll();
                } else if (entry.timing <= time) {
                    h = timings.poll().value.get();
                    if (h != null && h.validUntil <= time) {
                        if (map.remove(h.k, h)) {
                            try {
                                h.setRemoved();
                                getThis().onRemoveItem(h.k, h.v);
                            } catch (Exception e) {
                                onErrorDuringRefresh(e);
                            }
                        }
                    }
                } else
                    break;
            }
            if (entry != null)
                nextWakeUp = Math.min(nextWakeUp, entry.timing);
        }

        return nextWakeUp;
    }

    protected void onErrorDuringRefresh(Exception e) {
        onErrorDuringRefresh.onError(e);
    }

    @Override
    public Cache<K, V> onErrorDuringRefresh(CacheErrorListener errorListener) {
        this.onErrorDuringRefresh = errorListener;
        return this;
    }

    @Override
    public void destroy() {
        destroyed = true;
        clear();
    }

    @Override
    public void clear() {
        timings.clear();
        map.clear();
    }

    @Override
    public void onRemoveItem(K k, V v) {
        onRemove.onEvent(k, v);
    }

    @Override
    public void onAddItem(K k, V v) {
        onAdd.onEvent(k, v);
    }

    @Override
    public Cache<K, V> onAdd(CacheListener<? super K, ? super V> onAdd) {
        this.onAdd = onAdd;
        return this;
    }

    @Override
    public Cache<K, V> onRemove(CacheListener<? super K, ? super V> onRemove) {
        this.onRemove = onRemove;
        return this;
    }

    protected Holder<K, V> getHolderFromCache(final K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        Holder<K, V> f = map.get(key);
        if (f == null) {
            if (c == null || destroyed) {
                return null;
            }

            Holder<K, V> ft = new Holder<K, V>(key, timingsHolder);
            f = map.putIfAbsent(key, ft);
            if (f == null) {
                boolean failed = true;
                f = ft;
                try {
                    getThis().compute(key, c, ft);
                    failed = false;
                } catch (Exception e) {
                    throw Unchecked.rethrow(e);
                } finally {
                    ft.done();
                    if (failed && removeOnException) {
                        map.remove(key);
                        f.setRemoved();
                    } else {
                        updateTimingCache(f);
                        getThis().onAddItem(f.getKey(), f.get());
                    }
                }
                return f;
            }
        }

        if (updateTTL)
            updateTimingCache(f);

        return f;
    }

    @Override
    public void put(final K key, final V value) {
        put(key, value, ttl);
    }

    @Override
    public void put(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value, findTimingsHolder(ttl));
        Holder<K, V> old = map.put(key, h);
        getThis().onAddItem(key, value);
        updateTimingCache(h);
        if (old != null) {
            old.setRemoved();
            getThis().onRemoveItem(old.k, old.v);
        }
    }

    @Override
    public boolean putIfAbsent(final K key, final V value) {
        return putIfAbsent(key, value, ttl);
    }

    @Override
    public boolean putIfAbsent(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value, findTimingsHolder(ttl));
        if (map.putIfAbsent(key, h) == null) {
            updateTimingCache(h);
            getThis().onAddItem(key, value);
            return true;
        }
        return false;
    }

    protected TimingsHolder<K, V> findTimingsHolder(long ttl) {
        WeakReference<TimingsHolder<K, V>> ref = timings.get(ttl);
        TimingsHolder<K, V> timingsHolder;
        while (true) {
            if (ref == null) {
                timingsHolder = new TimingsHolder<K, V>(ttl);
                WeakReference<TimingsHolder<K, V>> existed = timings.putIfAbsent(ttl, ref = new WeakReference<TimingsHolder<K, V>>(timingsHolder));
                if (existed == null)
                    return timingsHolder;

                TimingsHolder<K, V> th = existed.get();
                if (th != null)
                    return th;

                if (timings.replace(ttl, existed, ref))
                    return timingsHolder;

                ref = timings.get(ttl);
            } else {
                timingsHolder = ref.get();
                if (timingsHolder != null)
                    return timingsHolder;

                ref = null;
            }
        }
    }

    private void updateTimingCache(final Holder<K, V> key) {
        TimingsHolder<K, V> timingsHolder = key.getTimingsHolder();
        if (timingsHolder == null || timingsHolder.ttl <= 0)
            return;

        long timing = timingsHolder.ttl + System.currentTimeMillis();
        key.setValidUntil(timing);

        CacheCleaner.updateWakeUp(timing);
        timingsHolder.timings.add(new TimingEntry<Holder<K, V>>(key, timing));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public long getTTL() {
        return ttl;
    }

    @Override
    public long getTTL(K k) {
        Holder<K, V> holder = map.get(k);
        if (holder != null)
            return holder.getTimingsHolder().ttl;

        return ttl;
    }

    @Override
    public void removeOldest() {
        Holder<K, V> holder = null;
        TimingsHolder<K, V> th;
        Iterator<WeakReference<TimingsHolder<K, V>>> i = timings.values().iterator();
        while ((th = next(i)) != null) {
            Iterator<TimingEntry<Holder<K, V>>> iterator = th.timings.iterator();
            while (iterator.hasNext()) {
                TimingEntry<Holder<K, V>> next = iterator.next();
                Holder<K, V> temp = next.value.get();
                if (temp == null || temp.isRemoved() || next.timing != temp.validUntil) {
                    iterator.remove();
                    continue;
                }

                if (holder == null || temp.validUntil < holder.validUntil)
                    holder = temp;

                break;
            }
        }
        if (holder != null)
            remove(holder.getKey());
    }

    protected <T> T next(Iterator<WeakReference<T>> iterator) {
        if (!iterator.hasNext())
            return null;

        do {
            T t = iterator.next().get();
            if (t != null)
                return t;

            iterator.remove();
        } while (iterator.hasNext());

        return null;
    }

    static class TimingsHolder<K, V> {
        Queue<TimingEntry<Holder<K, V>>> timings = new ConcurrentLinkedQueue<TimingEntry<Holder<K, V>>>();
        long ttl;

        private TimingsHolder(long ttl) {
            this.ttl = ttl;
        }
    }

    static class TimingEntry<K> {
        final WeakReference<K> value;
        final long timing;

        public TimingEntry(K value, long timing) {
            this.value = new WeakReference<K>(value);
            this.timing = timing;
        }
    }
}
