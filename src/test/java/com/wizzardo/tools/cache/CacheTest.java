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

        Thread.sleep(400);
        Assert.assertEquals(2, cache.size());

        Thread.sleep(500);
        Assert.assertEquals(1, cache.size());

        Thread.sleep(150);
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
        SizeLimitedCacheWrapper<String, String> cache = new SizeLimitedCacheWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }), 1);

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
        MemoryLimitedCacheWrapper<String, MemoryLimitedCacheWrapper.SizeProvider> cache = new MemoryLimitedCacheWrapper<String, MemoryLimitedCacheWrapper.SizeProvider>(new Cache<String, MemoryLimitedCacheWrapper.SizeProvider>(1, new Computable<String, MemoryLimitedCacheWrapper.SizeProvider>() {
            @Override
            public MemoryLimitedCacheWrapper.SizeProvider compute(final String s) {
                return new MemoryLimitedCacheWrapper.SizeProvider() {
                    @Override
                    public long size() {
                        return s.length();
                    }
                };
            }
        }), 5);

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
        final AbstractCache<String, String> cache = new OutdatedWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
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
        }), new Cache<String, String>("outdated", 1));

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
        CacheCleaner.updateWakeUp(0);
        Thread.sleep(100);
        int sizeBefore = CacheCleaner.size();
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
        Assert.assertEquals(sizeBefore + 1, CacheCleaner.size());
        cache.get("foo");
        Assert.assertEquals(1, cache.size());

        cache.destroy();
        Assert.assertEquals(0, cache.size());
        Assert.assertEquals(true, cache.isDestroyed());
        Assert.assertTrue(sizeBefore + 1 == CacheCleaner.size() || sizeBefore == CacheCleaner.size());

        Thread.sleep(1020);
        Assert.assertEquals(sizeBefore, CacheCleaner.size());

    }

    @Test
    public void test_statistics() throws InterruptedException {
        StatisticsWrapper<String, String> cache = new StatisticsWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }));
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
    public void test_statistics_2() throws InterruptedException {
        StatisticsWrapper<String, String> cache = new StatisticsWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) throws InterruptedException {
                Thread.sleep(10);
                return s.toUpperCase();
            }
        }));
        cache.get("foo");

        CacheStatistics statistics = cache.getStatistics();

        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getGetLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() < 10000l);
        Assert.assertTrue(statistics.getComputeLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getComputeLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getRemoveLatency() == 0);

        long get = statistics.getLatency.get();
        long put = statistics.putLatency.get();
        long compute = statistics.computeLatency.get();
        cache.get("foo");
        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(2, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(put, statistics.getPutLatency());
        Assert.assertEquals(compute, statistics.getComputeLatency());
        Assert.assertTrue(statistics.getGetLatency() > get);
    }

    @Test
    public void test_statistics_3() throws InterruptedException {
        StatisticsWrapper<String, String> cache = new StatisticsWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }).onAdd(new CacheListener<String, String>() {
            @Override
            public void onEvent(String key, String value) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        cache.get("foo");

        CacheStatistics statistics = cache.getStatistics();

        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getGetLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getComputeLatency() < 100 * 1000l);
        Assert.assertTrue(statistics.getRemoveLatency() == 0);

        long get = statistics.getLatency.get();
        long put = statistics.putLatency.get();
        long compute = statistics.computeLatency.get();
        cache.get("foo");
        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(2, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(put, statistics.getPutLatency());
        Assert.assertEquals(compute, statistics.getComputeLatency());
        Assert.assertTrue(statistics.getGetLatency() > get);
    }

    @Test
    public void test_statistics_4() throws InterruptedException {
        StatisticsWrapper<String, String> cache = new StatisticsWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }).onRemove(new CacheListener<String, String>() {
            @Override
            public void onEvent(String key, String value) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        cache.get("foo");

        CacheStatistics statistics = cache.getStatistics();

        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() < 9 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() < 9 * 1000 * 1000l);
        Assert.assertTrue(statistics.getComputeLatency() < 100 * 1000l);
        Assert.assertTrue(statistics.getRemoveLatency() == 0);

        long get = statistics.getLatency.get();
        long put = statistics.putLatency.get();
        long compute = statistics.computeLatency.get();
        long remove = statistics.removeLatency.get();
        cache.put("foo", "bar");
        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(2, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(1, statistics.getRemoveCount());
        Assert.assertEquals(get, statistics.getGetLatency());
        Assert.assertEquals(compute, statistics.getComputeLatency());
        Assert.assertTrue(statistics.getRemoveLatency() > remove);
        Assert.assertTrue(statistics.getPutLatency() > put);
    }

    @Test
    public void test_statistics_5() throws InterruptedException {
        StatisticsWrapper<String, String> cache = new StatisticsWrapper<String, String>(new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }).onAdd(new CacheListener<String, String>() {
            @Override
            public void onEvent(String key, String value) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        cache.get("foo");

        CacheStatistics statistics = cache.getStatistics();

        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertTrue(statistics.getGetLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getGetLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() > 10 * 1000 * 1000l);
        Assert.assertTrue(statistics.getPutLatency() < 15 * 1000 * 1000l);
        Assert.assertTrue(statistics.getComputeLatency() < 100 * 1000l);
        Assert.assertTrue(statistics.getRemoveLatency() == 0);

        cache.putIfAbsent("foo", "bar");
        Assert.assertEquals(1, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(1, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());

        long get = statistics.getLatency.get();
        long put = statistics.putLatency.get();
        long compute = statistics.computeLatency.get();
        long remove = statistics.removeLatency.get();
        cache.putIfAbsent("bar", "bar");
        Assert.assertEquals(2, statistics.getSize());
        Assert.assertEquals(1, statistics.getGetCount());
        Assert.assertEquals(2, statistics.getPutCount());
        Assert.assertEquals(1, statistics.getComputeCount());
        Assert.assertEquals(0, statistics.getRemoveCount());
        Assert.assertEquals(get, statistics.getGetLatency());
        Assert.assertEquals(remove, statistics.getRemoveLatency());
        Assert.assertEquals(compute, statistics.getComputeLatency());
        Assert.assertTrue(statistics.getPutLatency() > put);
    }

    @Test
    public void test_cache_iterable() throws InterruptedException {
        System.gc();
        CacheCleaner.updateWakeUp(0);
        Thread.sleep(100);
        int sizeBefore = CacheCleaner.size();
        Cache<String, String> cache;
        cache = new Cache<String, String>("test_cache_iterable", 1);
        cache = new Cache<String, String>("test_cache_iterable", 1);
        cache = new Cache<String, String>("test_cache_iterable", 1);

        Assert.assertTrue(CacheCleaner.size() >= sizeBefore + 3);

        System.gc();

        int i = 0;
        for (Cache c : CacheCleaner.iterable()) {
            if ("test_cache_iterable".equals(c.getName()))
                i++;
        }

        Assert.assertEquals(1, i);
        Assert.assertFalse(cache.isDestroyed());
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
        Assert.assertTrue(cache.timings.size() == 2 || cache.timings.size() == 1);
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
