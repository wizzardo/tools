package com.wizzardo.tools;

import com.wizzardo.tools.cache.Cache;
import com.wizzardo.tools.cache.Computable;
import org.junit.Assert;
import org.junit.Test;

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
    public void removeOldest() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        }) {
            @Override
            public void onRemoveItem(String s, String s2) {
                Assert.assertEquals("foo1", s);
            }
        };

        cache.get("foo1");
        cache.get("foo2");
        cache.get("foo3");

        Assert.assertEquals(3, cache.size());

        cache.removeOldest();

        Assert.assertEquals(2, cache.size());
    }
}
