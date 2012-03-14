package org.bordl.utils.cache;

import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moxa
 */
public class Cache<K, V> implements Computable<K, V> {

    private final ConcurrentHashMap<K, Future<V>> cache = new ConcurrentHashMap<K, Future<V>>();
    private final ConcurrentLinkedQueue<Entry<K, Long>> timings = new ConcurrentLinkedQueue<Entry<K, Long>>();
    private long lifetime;
    private final Computable<K, V> c;

    public Cache(Computable<K, V> c, long lifetime, long checkPeriod) {
        this.lifetime = lifetime;
        this.c = c;
        new CacheControl(checkPeriod).start();
    }

    @Override
    public V compute(final K key) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(key);
            if (f == null) {
                Callable<V> eval = new Callable<V>() {

                    @Override
                    public V call() throws Exception {
                        return c.compute(key);
                    }
                };
                FutureTask<V> ft = new FutureTask<V>(eval);
                f = cache.putIfAbsent(key, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                    updateTimingCache(key);
                }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                cache.remove(key, f);
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    public void put(final K key, final V value) {
        cache.put(key, new Future<V>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public V get() throws InterruptedException, ExecutionException {
                return value;
            }

            @Override
            public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return value;
            }
        });
        updateTimingCache(key);
    }

    private void updateTimingCache(final K key) {
        final Long timing = lifetime + System.currentTimeMillis();
        timings.add(new Entry<K, Long>() {

            @Override
            public K getKey() {
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
        return cache.contains(key);
    }

    private class CacheControl extends Thread {

        private long checkPeriod;

        public CacheControl(long checkPeriod) {
            setDaemon(true);
            this.checkPeriod = checkPeriod;
        }

        @Override
        public void run() {
            Entry<K, Long> entry = null;
            while (true) {
                try {
                    Thread.sleep(checkPeriod);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Cache.class.getName()).log(Level.SEVERE, null, ex);
                }
                Long time = System.currentTimeMillis();
                while ((entry = timings.peek()) != null && entry.getValue().compareTo(time) < 0) {
                    cache.remove(timings.poll().getKey()).cancel(true);
                }
            }
        }
    }
}
