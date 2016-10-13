package com.wizzardo.tools.cache;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
public class CacheCleaner extends Thread {

    public interface OnCacheAddedListener {
        void onAdd(Cache cache);
    }

    private Set<WeakReference<Cache>> caches = Collections.newSetFromMap(new ConcurrentHashMap<WeakReference<Cache>, Boolean>());
    private volatile long wakeup = -1;
    private volatile boolean sleeping = false;

    private final static CacheCleaner instance;
    private final Queue<OnCacheAddedListener> listeners = new ConcurrentLinkedQueue<OnCacheAddedListener>();

    static {
        instance = new CacheCleaner();
        instance.start();
    }

    private CacheCleaner() {
        setDaemon(true);
        setName(this.getClass().getName());
    }

    static void addCache(Cache cache) {
        instance.caches.add(new WeakReference<Cache>(cache));
        for (OnCacheAddedListener listener : instance.listeners) {
            listener.onAdd(cache);
        }
    }

    public static void addListener(OnCacheAddedListener listener) {
        instance.listeners.add(listener);
    }

    public static int size() {
        return instance.caches.size();
    }

    public static Iterable<Cache> iterable() {
        return new Iterable<Cache>() {
            @Override
            public Iterator<Cache> iterator() {
                final Iterator<WeakReference<Cache>> iterator = instance.caches.iterator();
                return new Iterator<Cache>() {
                    Cache next;

                    @Override
                    public boolean hasNext() {
                        if (next != null)
                            return true;

                        while (iterator.hasNext() && next == null) {
                            next = iterator.next().get();
                            if (next == null || next.isDestroyed()) {
                                iterator.remove();
                                if (next != null)
                                    next = null;
                            }

                        }
                        return next != null;
                    }

                    @Override
                    public Cache next() {
                        if (hasNext()) {
                            try {
                                return next;
                            } finally {
                                next = null;
                            }
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        throw new IllegalStateException();
                    }
                };
            }
        };
    }

    static void updateWakeUp(long wakeup) {
//        System.out.println("updateWakeUp");
        if (instance.wakeup < wakeup && instance.wakeup > 0)
            return;

        synchronized (instance) {
            if (instance.wakeup < wakeup && instance.wakeup > 0)
                return;

//            System.out.println("set wakeup after " + (wakeup - System.currentTimeMillis()));
            instance.wakeup = wakeup;
            if (instance.sleeping) {
//                System.out.println("notify");
                instance.notify();
            }
        }
    }

    @Override
    public void run() {
//        Map.Entry<? extends Holder<?, ?>, Long> entry = null;
//        Holder<?, ?> h;
        while (true) {
//            System.out.println();
//            System.out.println("cleaning");

            Long time = System.currentTimeMillis();

            Iterator<WeakReference<Cache>> iterator = caches.iterator();
            Long wakeup = time + (24 * 3600 * 1000);

            while (iterator.hasNext()) {
                Cache<?, ?> cache = iterator.next().get();

                if (cache == null || cache.isDestroyed()) {
                    iterator.remove();
                    continue;
                }

                long l = cache.refresh(time);
                if (l > 0 && l < wakeup)
                    wakeup = l;
            }


//            System.out.println("can sleep for " + (wakeup - time));
            while (wakeup.compareTo(time = System.currentTimeMillis()) > 0) {
                synchronized (this) {
                    if (this.wakeup < wakeup && this.wakeup > time)
                        wakeup = this.wakeup;
                    else
                        this.wakeup = wakeup;

                    sleeping = true;
//                    System.out.println("going to sleep for " + (wakeup - time));
                    if (wakeup - time > 0)
                        try {
//                            System.out.println("sleep for: " + (wakeup - time));
                            this.wait(wakeup - time);
                        } catch (InterruptedException ignored) {
                        }
//                    System.out.println("wake up, can sleep " + (this.wakeup - System.currentTimeMillis()) + "ms more");

                    sleeping = false;
                }
            }
        }
    }
}