package com.wizzardo.tools.collections.lazy;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wizzardo on 10.11.15.
 */
public class LazyTest {

    @Test
    public void test_grouping_1() {
        List<List<Integer>> result = Lazy.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<LazyGroup<Boolean, Integer, Integer>, List<Integer>>() {
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
    public void test_grouping_2() {
        List<List<Integer>> result = Lazy.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .filter(new Filter<LazyGroup<Boolean, Integer, Integer>>() {
                    @Override
                    public boolean allow(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.getKey();
                    }
                })
                .flatMap(new Mapper<LazyGroup<Boolean, Integer, Integer>, List<Integer>>() {
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

    @Test
    public void test_grouping_3() {
        List<Integer> result = Lazy.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<LazyGroup<Boolean, Integer, Integer>, Integer>() {
                    @Override
                    public Integer map(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.first();
                    }
                })
                .toSortedList();

        Assert.assertEquals(2, result.size());

        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
    }

    @Test
    public void test_grouping_4() {
        final AtomicInteger counter = new AtomicInteger();
        Integer result = Lazy.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        counter.incrementAndGet();
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<LazyGroup<Boolean, Integer, Integer>, Integer>() {
                    @Override
                    public Integer map(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.first();
                    }
                })
                .first();

        Assert.assertEquals(Integer.valueOf(1), result);
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_sorted_list() {
        List<Integer> result = Lazy.of(3, 2, 1).toSortedList();

        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    public void test_each() {
        final AtomicInteger counter = new AtomicInteger();

        Lazy.of(1, 2, 3).each(new Consumer<Integer>() {
            @Override
            public void consume(Integer integer) {
                counter.incrementAndGet();
            }
        }).execute();

        Assert.assertEquals(3, counter.get());
    }

    @Test
    public void test_first() {
        Assert.assertEquals(Integer.valueOf(1), Lazy.of(1, 2, 3).first());
    }

    @Test
    public void test_stop_after_first() {
        final AtomicInteger counter = new AtomicInteger();

        Assert.assertEquals(Integer.valueOf(1), Lazy.of(1, 2, 3).each(new Consumer<Integer>() {
            @Override
            public void consume(Integer integer) {
                counter.incrementAndGet();
            }
        }).first());

        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_last() {
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(1, 2, 3).last());
    }

    @Test
    public void test_min() {
        Assert.assertEquals(Integer.valueOf(1), Lazy.of(1, 2, 3).min());
        Assert.assertEquals(Integer.valueOf(1), Lazy.of(3, 2, 1).min());
    }

    @Test
    public void test_max() {
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(1, 2, 3).max());
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(3, 2, 1).max());
    }

    @Test
    public void test_max_2() {
        Comparator<Number> comparator = new Comparator<Number>() {
            @Override
            public int compare(Number o1, Number o2) {
                return Integer.valueOf(o1.intValue()).compareTo(o2.intValue());
            }
        };
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(1, 2, 3).max(comparator));
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(3, 2, 1).max(comparator));
    }

    @Test
    public void test_reduce() {
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(1, 2, 3).reduce(new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer a, Integer b) {
                return a > b ? a : b;
            }
        }));
        Assert.assertEquals(Integer.valueOf(3), Lazy.of(3, 2, 1).reduce(new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer a, Integer b) {
                return a > b ? a : b;
            }
        }));
    }

    @Test
    public void test_collect() {
        List<Integer> list = new ArrayList<Integer>();
        List<Integer> result = Lazy.of(1, 2, 3)
                .collect(list, new BiConsumer<List<Integer>, Integer>() {
                    @Override
                    public void consume(List<Integer> integers, Integer integer) {
                        integers.add(integer * 2);
                    }
                });

        Assert.assertSame(list, result);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1));
        Assert.assertEquals(Integer.valueOf(6), result.get(2));
    }

    @Test
    public void test_iterate() {
        List<Integer> result = Lazy.of(new int[]{1}, new int[]{2, 3}, new int[]{4, 5, 6})
                .iterate(new Iterater<int[], Integer>() {
                    @Override
                    public void iterate(int[] ints, Consumer<Integer> consumer) {
                        for (int i : ints) {
                            consumer.consume(i);
                        }
                    }
                }).toList();
        Assert.assertEquals(6, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
        Assert.assertEquals(Integer.valueOf(4), result.get(3));
        Assert.assertEquals(Integer.valueOf(5), result.get(4));
        Assert.assertEquals(Integer.valueOf(6), result.get(5));
    }

    @Test
    public void test_iterate_2() {
        List<Integer> result = Lazy.of(Arrays.asList(1), Arrays.asList(2, 3), Arrays.asList(4, 5, 6))
                .<Integer>iterate()
                .toList();

        Assert.assertEquals(6, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
        Assert.assertEquals(Integer.valueOf(4), result.get(3));
        Assert.assertEquals(Integer.valueOf(5), result.get(4));
        Assert.assertEquals(Integer.valueOf(6), result.get(5));
    }

    @Test
    public void test_merge() {
        List<Integer> result = Lazy.of(1, 2, 3, 4, 5, 6)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .merge()
                .toList();

        Assert.assertEquals(6, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
        Assert.assertEquals(Integer.valueOf(4), result.get(3));
        Assert.assertEquals(Integer.valueOf(5), result.get(4));
        Assert.assertEquals(Integer.valueOf(6), result.get(5));
    }

    @Test
    public void test_merge_2() {
        List<Integer> result = Lazy.of(1, 2, 3, 4, 5, 6)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .filter(new Filter<LazyGroup<Boolean, Integer, Integer>>() {
                    @Override
                    public boolean allow(LazyGroup<Boolean, Integer, Integer> group) {
                        return group.getKey();
                    }
                })
                .merge()
                .toList();

        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1));
        Assert.assertEquals(Integer.valueOf(6), result.get(2));
    }
}
