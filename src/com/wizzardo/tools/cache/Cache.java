package com.wizzardo.tools.cache;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Moxa
 */
public class Cache<K, V> {

    private final ConcurrentHashMap<K, Holder<K, V>> cache = new ConcurrentHashMap<K, Holder<K, V>>();
    private final ConcurrentLinkedQueue<Entry<Holder<K, V>, Long>> timings = new ConcurrentLinkedQueue<Entry<Holder<K, V>, Long>>();
    private long lifetime;
    private Computable<? super K, ? extends V> computable;
    private volatile boolean removeOnFail = true;

    public Cache(long lifetimeSec, long checkPeriodSec, Computable<? super K, ? extends V> computable) {
        this.lifetime = lifetimeSec * 1000;
        this.computable = computable;
        new CacheControl(checkPeriodSec).start();
    }

    public Cache(long lifetimeSec, long checkPeriodSec) {
        this(lifetimeSec, checkPeriodSec, null);
    }

    public Cache(long lifetimeSec, Computable<? super K, ? extends V> computable) {
        this(lifetimeSec, lifetimeSec == 0 ? -1 : lifetimeSec / 2, computable);
    }

    public Cache(long lifetimeSec) {
        this(lifetimeSec, lifetimeSec / 2);
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

    public static interface Computable<K, V> {

        public V compute(K k);
    }

    public void setRemoveOnFail(boolean removeOnFail) {
        this.removeOnFail = removeOnFail;
    }

    public boolean isRemoveOnFail() {
        return removeOnFail;
    }

    public void clear() {
        timings.clear();
        cache.clear();
    }

    private class Holder<K, V> {

        protected V v;
        protected K k;
        protected boolean done = false;
        protected long validUntil;

        public Holder(K k) {
            this.k = k;
        }

        public Holder(K k, V v) {
            this.v = v;
            this.k = k;
            done = true;
        }

        public V get() {
            if (!done) {
                synchronized (this) {
                    while (!done) {
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
            return v;
        }

        public void run(Computable<? super K, ? extends V> c, K k) {
            v = c.compute(k);
            done();
        }

        void done() {
            synchronized (this) {
                done = true;
                this.notifyAll();
            }
        }

        public void setValidUntil(long validUntil) {
            this.validUntil = validUntil;
        }

        public long getValidUntil() {
            return validUntil;
        }

        public K getKey() {
            return k;
        }
    }

    private V getFromCache(final K key, Computable<? super K, ? extends V> c, boolean updateTTL) {
        Holder<K, V> f = cache.get(key);
        if (f == null) {
            if (c == null) {
                return null;
            }
            Holder<K, V> ft = new Holder<K, V>(key);
            f = cache.putIfAbsent(key, ft);
            boolean failed = true;
            if (f == null) {
                f = ft;
                try {
                    ft.run(c, key);
                    failed = false;
                } finally {
                    ft.done();
                    if (failed && removeOnFail)
                        cache.remove(key);
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
        Holder<K, V> h = new Holder<K, V>(key, value);
        cache.put(key, h);
        updateTimingCache(h);
    }

    public boolean putIfAbsent(final K key, final V value) {
        Holder<K, V> h = new Holder<K, V>(key, value);
        if (cache.putIfAbsent(key, h) == null) {
            updateTimingCache(h);
            return true;
        }
        return false;
    }

    private void updateTimingCache(final Holder<K, V> key) {
        final Long timing = lifetime + System.currentTimeMillis();
        key.setValidUntil(timing);
        timings.add(new Entry<Holder<K, V>, Long>() {
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
        return cache.size();
    }

    public boolean contains(K key) {
        return cache.containsKey(key);
    }

    private class CacheControl extends Thread {

        private long checkPeriod;
        private boolean enabled = true;

        public CacheControl(long checkPeriodSec) {
            setDaemon(true);
            this.checkPeriod = checkPeriodSec * 1000;
            if (checkPeriodSec < 0) {
                enabled = false;
            } else if (checkPeriodSec == 0) {
                this.checkPeriod = 500;
            }
        }

        @Override
        public void run() {
            Entry<Holder<K, V>, Long> entry = null;
            while (enabled) {
                try {
                    Thread.sleep(checkPeriod);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
                }
                Holder<K, V> h;
                Long time = System.currentTimeMillis();
                while ((entry = timings.peek()) != null && entry.getValue().compareTo(time) < 0) {
                    h = timings.poll().getKey();
                    if (h.validUntil < time)
                        cache.remove(h.getKey());
                }
            }
        }
    }
}
