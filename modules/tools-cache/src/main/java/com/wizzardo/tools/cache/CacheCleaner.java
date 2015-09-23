package com.wizzardo.tools.cache;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: wizzardo
 * Date: 2/12/14
 */
class CacheCleaner extends Thread {

    private Set<Cache> caches = Collections.newSetFromMap(new ConcurrentHashMap<Cache, Boolean>());
    private volatile long wakeup = -1;
    private volatile boolean sleeping = false;

    private final static CacheCleaner instance;

    static {
        instance = new CacheCleaner();
        instance.start();
    }

    private CacheCleaner() {
        setDaemon(true);
        setName(this.getClass().getName());
    }

    static void addCache(Cache cache) {
        instance.caches.add(cache);
    }

    static int size() {
        return instance.caches.size();
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

            Iterator<Cache> iterator = caches.iterator();
            Long wakeup = time + (24 * 3600 * 1000);

            while (iterator.hasNext()) {
                Cache<?, ?> cache = iterator.next();

                if (cache.isDestroyed()) {
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