package com.wizzardo.tools.cache;

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
    private final Queue<TimingsHolder> timings = new ConcurrentLinkedQueue<TimingsHolder>();
    private long ttl;
    private Computable<? super K, ? extends V> computable;
    private volatile boolean removeOnException = true;
    private volatile boolean destroyed;

    public Cache(long ttlSec, Computable<? super K, ? extends V> computable) {
        this.ttl = ttlSec * 1000;
        this.computable = computable;
        timings.add(new TimingsHolder(ttl));
        CacheCleaner.addCache(this);
    }

    public Cache(long ttlSec) {
        this(ttlSec, null);
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
        onRemoveItem(holder.getKey(), holder.get());
        return holder.get();
    }

    long refresh(long time) {
        Map.Entry<Holder<K, V>, Long> entry;
        Holder<K, V> h;
        long nextWakeUp = Long.MAX_VALUE;

        for (TimingsHolder timingsHolder : timings) {
            Queue<Entry<Holder<K, V>, Long>> timings = timingsHolder.timings;

            while ((entry = timings.peek()) != null && entry.getValue().compareTo(time) <= 0) {
                h = timings.poll().getKey();
                if (h.validUntil <= time) {
//                System.out.println("remove: " + h.k + " " + h.v + " because it is invalid for " + (time - h.validUntil));
                    if (map.remove(h.getKey(), h))
                        onRemoveItem(h.getKey(), h.get());
                }
            }
            if (entry != null)
                nextWakeUp = Math.min(nextWakeUp, entry.getValue());
        }

        return nextWakeUp;
    }

    public void destroy() {
        destroyed = true;
        clear();
    }

    public void clear() {
        timings.clear();
        map.clear();
    }

    public void onRemoveItem(K k, V v) {
    }

    private V getFromCache(final K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        Holder<K, V> f = map.get(key);
        if (f == null) {
            if (c == null || destroyed) {
                return null;
            }
            Holder<K, V> ft = new Holder<K, V>(key);
            f = map.putIfAbsent(key, ft);
            boolean failed = true;
            if (f == null) {
                f = ft;
                try {
                    ft.run(c, key);
                    failed = false;
                } finally {
                    ft.done();
                    if (failed && removeOnException)
                        map.remove(key);
                    else
                        updateTimingCache(f);
                }
            }
        } else if (updateTTL) {
            updateTimingCache(f);
        }
        return f.get();
    }

    public void put(final K key, final V value) {
        put(key, value, ttl);
    }

    public void put(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value);
        map.put(key, h);
        updateTimingCache(h, findTimingsHolder(ttl));
    }

    public boolean putIfAbsent(final K key, final V value) {
        return putIfAbsent(key, value, ttl);
    }

    public boolean putIfAbsent(final K key, final V value, long ttl) {
        Holder<K, V> h = new Holder<K, V>(key, value);
        if (map.putIfAbsent(key, h) == null) {
            updateTimingCache(h, findTimingsHolder(ttl));
            return true;
        }
        return false;
    }

    private TimingsHolder findTimingsHolder(long ttl) {
        for (TimingsHolder holder : timings)
            if (holder.ttl == ttl)
                return holder;

        TimingsHolder holder = new TimingsHolder(ttl);
        timings.add(holder);
        return holder;
    }

    private void updateTimingCache(final Holder<K, V> key) {
        updateTimingCache(key, timings.peek());
    }

    private void updateTimingCache(final Holder<K, V> key, TimingsHolder timingsHolder) {
        if (timingsHolder.ttl <= 0)
            return;

        final Long timing = timingsHolder.ttl + System.currentTimeMillis();
        key.setValidUntil(timing);

        CacheCleaner.updateWakeUp(timing);

        timingsHolder.timings.add(new Entry<Holder<K, V>, Long>() {
            @Override
            public Holder<K, V> getKey() {
                return key;
            }

            @Override
            public Long getValue() {
                return timing;
            }

            @Override
            public Long setValue(Long value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
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

    private class TimingsHolder {
        Queue<Entry<Holder<K, V>, Long>> timings = new ConcurrentLinkedQueue<Entry<Holder<K, V>, Long>>();
        long ttl;

        private TimingsHolder(long ttl) {
            this.ttl = ttl;
        }
    }
}
