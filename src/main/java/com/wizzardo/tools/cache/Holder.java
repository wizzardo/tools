package com.wizzardo.tools.cache;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
class Holder<K, V> {

    protected V v;
    protected K k;
    protected volatile boolean done = false;
    protected long validUntil;
    private Cache.TimingsHolder timingsHolder;

    public Holder(K k, Cache.TimingsHolder timingsHolder) {
        this.k = k;
        this.timingsHolder = timingsHolder;
    }

    public Holder(K k, V v, Cache.TimingsHolder timingsHolder) {
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

    public void run(Computable<? super K, ? extends V> c, K k) {
        v = c.compute(k);
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

    Cache.TimingsHolder getTimingsHolder() {
        return timingsHolder;
    }
}