package com.wizzardo.tools.collections.lazy;

import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;

/**
 * Created by wizzardo on 10.11.15.
 */
public class LazyTest {

    @Test
    public void test_1() {
        List<List<Integer>> result = Lazy.of(1, 2, 3)
                .groupBy(new Lazy.Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Lazy.Mapper<LazyGroup<Boolean, Integer, Integer>, List<Integer>>() {
                    @Override
                    public List<Integer> map(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.toList();
                    }
                })
                .toSortedList(new Comparator<List<Integer>>() {
                    @Override
                    public int compare(List<Integer> o1, List<Integer> o2) {
                        return o1.size() < o2.size() ? -1 : (o1.size() == o2.size() ? 0 : 1);
                    }
                });

        Assert.assertEquals(2, result.size());

        Assert.assertEquals(1, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0).get(0));

        Assert.assertEquals(2, result.get(1).size());
        Assert.assertEquals(Integer.valueOf(1), result.get(1).get(0));
        Assert.assertEquals(Integer.valueOf(3), result.get(1).get(1));
    }

    @Test
    public void test_2() {
        List<List<Integer>> result = Lazy.of(1, 2, 3)
                .groupBy(new Lazy.Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .filter(new Lazy.Filter<LazyGroup<Boolean, Integer, Integer>>() {
                    @Override
                    public boolean allow(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.getKey();
                    }
                })
                .flatMap(new Lazy.Mapper<LazyGroup<Boolean, Integer, Integer>, List<Integer>>() {
                    int counter = 0;

                    @Override
                    public List<Integer> map(LazyGroup<Boolean, Integer, Integer> group) {
                        Assert.assertEquals("should be executed only once", 1, ++counter);
                        return group.toList();
                    }
                })
                .toSortedList(new Comparator<List<Integer>>() {
                    @Override
                    public int compare(List<Integer> o1, List<Integer> o2) {
                        return o1.size() < o2.size() ? -1 : (o1.size() == o2.size() ? 0 : 1);
                    }
                });

        Assert.assertEquals(1, result.size());

        Assert.assertEquals(1, result.get(0).size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0).get(0));
    }
}
