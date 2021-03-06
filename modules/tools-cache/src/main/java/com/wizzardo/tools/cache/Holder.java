package com.wizzardo.tools.cache;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
public class Holder<K, V> {

    protected V v;
    protected final K k;
    protected volatile boolean done = false;
    protected volatile long validUntil;
    private Cache.TimingsHolder<K, V> timingsHolder;

    public Holder(K k, Cache.TimingsHolder<K, V> timingsHolder) {
        this.k = k;
        this.timingsHolder = timingsHolder;
    }

    public Holder(K k, V v, Cache.TimingsHolder<K, V> timingsHolder) {
        this.v = v;
        this.k = k;
        this.timingsHolder = timingsHolder;
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

    void compute(Computable<? super K, ? extends V> c, K k) throws Exception {
        v = c.compute(k);
    }

    void done() {
        synchronized (this) {
            done = true;
            this.notifyAll();
        }
    }

    void setValidUntil(long validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isValid() {
        return System.currentTimeMillis() < validUntil;
    }

    public K getKey() {
        return k;
    }

    Cache.TimingsHolder<K, V> getTimingsHolder() {
        return timingsHolder;
    }

    boolean isRemoved() {
        return timingsHolder == null;
    }

    void setRemoved() {
        timingsHolder = null;
        done = true; // just for hb
    }
}