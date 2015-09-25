package com.wizzardo.tools.cache;

import com.wizzardo.tools.misc.Unchecked;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Moxa
 */
public class Cache<K, V> {

    private final ConcurrentHashMap<K, Holder<K, V>> map = new ConcurrentHashMap<K, Holder<K, V>>();
    private final Queue<TimingsHolder<K, V>> timings = new ConcurrentLinkedQueue<TimingsHolder<K, V>>();
    private long ttl;
    private Computable<? super K, ? extends V> computable;
    private volatile boolean removeOnException = true;
    private volatile boolean destroyed;
    private Cache<K, V> outdated;

    public Cache(long ttlSec, Computable<? super K, ? extends V> computable) {
        this.ttl = ttlSec * 1000;
        this.computable = computable;
        timings.add(new TimingsHolder<K, V>(ttl));
        CacheCleaner.addCache(this);
    }

    public Cache(long ttlSec) {
        this(ttlSec, null);
    }

    public Cache<K, V> allowOutdated() {
        return allowOutdated(ttl <= 1000 ? 1 : ttl / 2000);
    }

    public Cache<K, V> allowOutdated(long ttlSec) {
        outdated = new Cache<K, V>(ttlSec);
        return this;
    }

    public V get(K k) {
        return getFromCache(k, computable, false);
    }

    public V get(K k, boolean updateTTL) {
        return getFromCache(k, computable, updateTTL);
    }

    public V get(K k, Computable<? super K, ? extends V> computable) {
        return getFromCache(k, computable, false);
    }

    public V get(K k, Computable<? super K, ? extends V> computable, boolean updateTTL) {
        return getFromCache(k, computable, updateTTL);
    }

    public void setRemoveOnException(boolean removeOnException) {
        this.removeOnException = removeOnException;
    }

    public boolean isRemoveOnException() {
        return removeOnException;
    }

    public V remove(K k) {
        Holder<K, V> holder = map.remove(k);
        if (holder == null)
            return null;
        holder.setRemoved();
        onRemoveItem(holder.getKey(), holder.get());
        putToOutdated(holder);
        return holder.get();
    }

    long refresh(long time) {
        TimingEntry<Holder<K, V>> entry;
        Holder<K, V> h;
        long nextWakeUp = Long.MAX_VALUE;

        for (TimingsHolder<K, V> timingsHolder : timings) {
            Queue<TimingEntry<Holder<K, V>>> timings = timingsHolder.timings;

            while ((entry = timings.peek()) != null) {
                if (entry.k.isRemoved()) {
                    timings.poll();
                } else if (entry.timing <= time) {
                    h = timings.poll().getKey();
                    if (h.validUntil <= time) {
//                System.out.println("remove: " + h.k + " " + h.v + " because it is invalid for " + (time - h.validUntil));
                        if (map.remove(h.k, h)) {
                            onRemoveItem(h.k, h.v);
                            putToOutdated(h);
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

    private void putToOutdated(Holder<K, V> h) {
        if (outdated != null)
            outdated.put(h.k, h.v);
    }

    public void destroy() {
        destroyed = true;
        clear();
        if (outdated != null)
            outdated.destroy();
    }

    public void clear() {
        timings.clear();
        map.clear();
    }

    public void onRemoveItem(K k, V v) {
    }

    public void onAddItem(K k, V v) {
    }

    private V getFromCache(final K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        Holder<K, V> f = map.get(key);
        if (f == null) {
            if (c == null || destroyed) {
                return null;
            }
            Holder<K, V> ft = new Holder<K, V>(key, timings.peek());
            f = map.putIfAbsent(key, ft);
            if (f == null) {
                boolean failed = true;
                f = ft;
                try {
                    ft.run(c, key);
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
                        onAddItem(f.getKey(), f.get());
                        if (outdated != null)
                            outdated.remove(key);
                    }
                }
                return f.get();
            }
        }

        if (!f.done && outdated != null) {
            V v = outdated.get(key);
            if (v != null)
                return v;
        }

        V v = f.get();
        if (updateTTL)
            updateTimingCache(f);

        return v;
    }

    public void put(final K key, final V value) {
        put(key, value, ttl);
    }

    public void put(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value, findTimingsHolder(ttl));
        Holder<K, V> old = map.put(key, h);
        onAddItem(key, value);
        updateTimingCache(h);
        if (old != null) {
            old.setRemoved();
            onRemoveItem(old.k, old.v);
        }
    }

    public boolean putIfAbsent(final K key, final V value) {
        return putIfAbsent(key, value, ttl);
    }

    public boolean putIfAbsent(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value, findTimingsHolder(ttl));
        if (map.putIfAbsent(key, h) == null) {
            updateTimingCache(h);
            onAddItem(key, value);
            return true;
        }
        return false;
    }

    private TimingsHolder<K, V> findTimingsHolder(long ttl) {
        for (TimingsHolder<K, V> holder : timings)
            if (holder.ttl == ttl)
                return holder;

        TimingsHolder<K, V> holder = new TimingsHolder<K, V>(ttl);
        timings.add(holder);
        return holder;
    }

    private void updateTimingCache(final Holder<K, V> key) {
        TimingsHolder<K, V> timingsHolder = key.getTimingsHolder();
        if (timingsHolder.ttl <= 0)
            return;

        final Long timing = timingsHolder.ttl + System.currentTimeMillis();
        key.setValidUntil(timing);

        CacheCleaner.updateWakeUp(timing);
        timingsHolder.timings.add(new TimingEntry<Holder<K, V>>(key, timing));
    }

    public int size() {
        return map.size();
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public long getTTL() {
        return ttl;
    }

    public long getTTL(K k) {
        Holder<K, V> holder = map.get(k);
        if (holder != null)
            return holder.getTimingsHolder().ttl;

        return ttl;
    }

    public void removeOldest() {
        Holder<K, V> holder = null;
        for (TimingsHolder<K, V> th : timings) {
            for (TimingEntry<Holder<K, V>> e : th.timings) {
                if (e.timing != e.getKey().validUntil)
                    continue;

                if (!e.getKey().isRemoved()) {
                    if (holder == null || e.getKey().validUntil < holder.validUntil)
                        holder = e.getKey();

                    break;
                }
            }
        }
        if (holder != null)
            remove(holder.getKey());
    }

    static class TimingsHolder<K, V> {
        Queue<TimingEntry<Holder<K, V>>> timings = new ConcurrentLinkedQueue<TimingEntry<Holder<K, V>>>();
        long ttl;

        private TimingsHolder(long ttl) {
            this.ttl = ttl;
        }
    }

    static class TimingEntry<K> {
        private final K k;
        private final long timing;

        public TimingEntry(K k, long timing) {
            this.k = k;
            this.timing = timing;
        }

        public K getKey() {
            return k;
        }
    }
}
