package com.wizzardo.tools.cache;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
public class Holder<K, V> {

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