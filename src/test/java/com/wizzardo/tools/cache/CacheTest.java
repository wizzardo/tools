package com.wizzardo.tools.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: moxa
 * Date: 9/27/13
 */
public class CacheTest {

    @Test
    public void simpleCache() {
        Cache<Integer, Integer> cache = new Cache<Integer, Integer>(1, new Computable<Integer, Integer>() {

            @Override
            public Integer compute(Integer s) {
                return new Integer(s * s);
            }
        });

        Assert.assertEquals(Integer.valueOf(4), cache.get(2));

        Integer cached = cache.get(16);
        Assert.assertTrue(cached == cache.get(16));

        try {
            Thread.sleep(1501);
        } catch (InterruptedException ignored) {
        }

        Assert.assertTrue(cached != cache.get(16));
    }

    @Test
    public void exceptions() {
        Cache<Integer, Integer> cache = new Cache<Integer, Integer>(0, new Computable<Integer, Integer>() {

            @Override
            public Integer compute(Integer s) {
                throw new RuntimeException();
            }
        });

        try {
            cache.get(1);
            assert false;
        } catch (Exception e) {
        }

        try {
            cache.get(1);
            assert false;
        } catch (Exception e) {
        }

    }

    @Test
    public void testTTL() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });

        cache.get("foo", true);
        Thread.sleep(500);
        cache.get("bar");

        Thread.sleep(490);
        Assert.assertEquals(2, cache.size());

        Thread.sleep(500);
        Assert.assertEquals(1, cache.size());

        Thread.sleep(20);
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void testCustomTTL() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });

        cache.get("foo");

        String bar = "BAR";
        cache.put("bar", bar, 500);

        Assert.assertTrue(bar == cache.get("bar"));

        Thread.sleep(550);

        Assert.assertTrue(bar != cache.get("bar"));
    }

    @Test
    public void removeOldestTest() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }) {
            int i = 1;

            @Override
            public void onRemoveItem(String s, String s2) {
                Assert.assertEquals("foo" + (i++), s);
            }
        };

        cache.get("foo1");
        cache.get("foo2");
        cache.get("foo3");
        Assert.assertEquals(3, cache.size());

        cache.removeOldest();
        Assert.assertEquals(2, cache.size());

        cache.removeOldest();
        Assert.assertEquals(1, cache.size());

        cache.removeOldest();
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void sizeLimitedCacheTest() throws InterruptedException {
        Cache<String, String> cache = new SizeLimitedCache<String, String>(1, 1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });

        Assert.assertEquals(0, cache.size());
        cache.get("foo1");
        Assert.assertEquals(1, cache.size());
        cache.get("foo2");
        Assert.assertEquals(1, cache.size());
        cache.get("foo3");
        Assert.assertEquals(1, cache.size());
        cache.get("foo4");
        Assert.assertEquals(1, cache.size());
    }

    @Test
    public void memoryLimitedCacheTest() throws InterruptedException {
        MemoryLimitedCache<String, MemoryLimitedCache.SizeProvider> cache = new MemoryLimitedCache<String, MemoryLimitedCache.SizeProvider>(5, 1, new Computable<String, MemoryLimitedCache.SizeProvider>() {
            @Override
            public MemoryLimitedCache.SizeProvider compute(final String s) {
                return new MemoryLimitedCache.SizeProvider() {
                    @Override
                    public long size() {
                        return s.length();
                    }
                };
            }
        });

        Assert.assertEquals(0, cache.size());
        cache.get("foo1");
        Assert.assertEquals(1, cache.size());
        cache.get("foo2");
        Assert.assertEquals(1, cache.size());
        cache.get("foo3");
        Assert.assertEquals(1, cache.size());
        cache.get("foo4");
        Assert.assertEquals(1, cache.size());

        Assert.assertEquals(4, cache.memoryUsed());
        Assert.assertEquals(5, cache.limit());
    }

    @Test
    public void wait_test() {
        final AtomicInteger counter = new AtomicInteger();
        final Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                counter.incrementAndGet();
                return s.toUpperCase();
            }
        });
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                cache.get("foo");
                Assert.assertEquals(1, counter.get());
            }
        });
        t.start();
        cache.get("foo");
        try {
            t.join();
        } catch (InterruptedException e) {
        }
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void outdated_test() throws InterruptedException {
        final Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            AtomicInteger counter = new AtomicInteger();

            @Override
            public String compute(String s) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return s + "_" + counter.getAndIncrement();
            }
        }).allowOutdated();

        Assert.assertEquals("foo_0", cache.get("foo"));
        Assert.assertEquals(1, cache.size());

        Thread.sleep(1200);
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals("foo_1", cache.get("foo"));
                latch.countDown();
            }
        }).start();

        Thread.sleep(100);
        Assert.assertEquals("foo_0", cache.get("foo"));
        latch.await();
    }

    @Test
    public void destroy_test() throws InterruptedException {
        System.gc();
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });

        cache.get("foo");

        Assert.assertEquals(1, cache.size());

        Thread.sleep(1020);
        Assert.assertEquals(0, cache.size());
        Assert.assertEquals(1, CacheCleaner.size());
        cache.get("foo");
        Assert.assertEquals(1, cache.size());

        cache.destroy();
        Assert.assertEquals(0, cache.size());
        Assert.assertEquals(true, cache.isDestroyed());
        Assert.assertEquals(1, CacheCleaner.size());

        Thread.sleep(1020);
        Assert.assertEquals(0, CacheCleaner.size());

    }

    @Test
    public void test_statistics() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });
        cache.get("foo");

        CacheStatistics statistics = cache.getStatistics();

        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() > 0);
        Assert.assertTrue(statistics.getPutCount() > 0);
        Assert.assertTrue(statistics.getComputeLatency() > 0);
        Assert.assertTrue(statistics.getRemoveLatency() == 0);

        Thread.sleep(1020);

        Assert.assertEquals(0, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(1, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() > 0);
        Assert.assertTrue(statistics.getPutCount() > 0);
        Assert.assertTrue(statistics.getComputeLatency() > 0);
        Assert.assertTrue(statistics.getRemoveLatency() > 0);
    }

    @Test
    public void test_cache_iterable() {
        Cache<String, String> cache;
        cache = new Cache<String, String>(1);
        cache = new Cache<String, String>(1);
        cache = new Cache<String, String>(1);

        int before = CacheCleaner.size();
        Assert.assertTrue(before >= 3);

        System.gc();

        int i = 0;
        for (Cache c : CacheCleaner.iterable()) {
            i++;
        }

        Assert.assertEquals(1, i);
        Assert.assertEquals(1, CacheCleaner.size());
    }

    @Test
    public void test_cache_timings_cleanup() {
        Cache<String, String> cache = new Cache<String, String>(1);

        Assert.assertEquals(1, cache.timings.size());

        Cache.TimingsHolder<String, String> timingsHolder = cache.findTimingsHolder(2);
        Assert.assertEquals(2, cache.timings.size());
        System.gc();
        Assert.assertEquals(2, cache.timings.size());

        timingsHolder = null;
        System.gc();
        Assert.assertEquals(2, cache.timings.size());
        cache.refresh();
        Assert.assertEquals(1, cache.timings.size());


        int hash = cache.findTimingsHolder(2).hashCode();
        Assert.assertEquals(2, cache.timings.size());
        Assert.assertEquals(hash, cache.findTimingsHolder(2).hashCode());
        System.gc();
        Assert.assertEquals(2, cache.timings.size());

        cache.findTimingsHolder(2);
        Assert.assertNotEquals(hash, cache.findTimingsHolder(2).hashCode());
    }
}
