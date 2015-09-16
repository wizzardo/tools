package com.wizzardo.tools.collections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wizzardo on 14.09.15.
 */
public class CollectionToolsTest {

    @Test
    public void each() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        final AtomicInteger counter = new AtomicInteger();
        CollectionTools.each(list, new CollectionTools.Closure<Void, Integer>() {
            @Override
            public Void execute(Integer it) {
                counter.addAndGet(it);
                return null;
            }
        });
        Assert.assertEquals(6, counter.get());
    }

    @Test
    public void eachWithIndex() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        final AtomicInteger counter = new AtomicInteger();
        CollectionTools.eachWithIndex(list, new CollectionTools.Closure2<Void, Integer, Integer>() {
            @Override
            public Void execute(Integer i, Integer value) {
                counter.addAndGet(value * i);
                return null;
            }
        });
        Assert.assertEquals(8, counter.get());
    }

    @Test
    public void collect() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        List<Integer> result = CollectionTools.collect(list, new CollectionTools.Closure<Integer, Integer>() {
            @Override
            public Integer execute(Integer it) {
                return it * 2;
            }
        });

        Assert.assertEquals(3, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals((Integer) 2, result.get(0));
        Assert.assertEquals((Integer) 4, result.get(1));
        Assert.assertEquals((Integer) 6, result.get(2));
    }

    @Test
    public void group() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
        }};

        Map<Boolean, List<Integer>> groups = CollectionTools.group(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        }, new CollectionTools.Closure<Integer, Integer>() {
            @Override
            public Integer execute(Integer it) {
                return it;
            }
        });

        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(2, groups.get(true).size());
        Assert.assertEquals(3, groups.get(false).size());
    }
}
