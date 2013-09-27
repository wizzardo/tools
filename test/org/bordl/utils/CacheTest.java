package org.bordl.utils;

import org.bordl.utils.cache.Cache;
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
}
