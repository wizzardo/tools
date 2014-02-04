package com.wizzardo.tools.tools;

import com.wizzardo.tools.cache.Cache;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: moxa
 * Date: 9/27/13
 */
public class CacheTest {

    @Test
    public void simpleCache() {
        Cache<Integer, Integer> cache = new Cache<Integer, Integer>(1, new Cache.Computable<Integer, Integer>() {

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
        Cache<Integer, Integer> cache = new Cache<Integer, Integer>(0, new Cache.Computable<Integer, Integer>() {

            @Override
            public Integer compute(Integer s) {
                throw new RuntimeException();
            }
        });

        try {
            cache.get(1);
        } catch (Exception e) {
        }

        try {
            cache.get(1);
        } catch (Exception e) {
        }

    }

    @Test
    public void testTTL() throws InterruptedException {
        Cache<String, String> cache = new Cache<String, String>(1, new Cache.Computable<String, String>() {
            @Override
            public String compute(String s) {
                return s.toUpperCase();
            }
        });

        cache.get("foo", true);
        Thread.sleep(500);
        cache.get("bar");

        Assert.assertEquals(2,cache.size());

        Thread.sleep(1010);
        Assert.assertEquals(1,cache.size());

        Thread.sleep(500);
        Assert.assertEquals(0,cache.size());
    }
}
