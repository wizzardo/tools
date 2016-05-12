package com.wizzardo.tools.collections.flow;

import com.wizzardo.tools.collections.flow.flows.FlowCount;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wizzardo on 10.11.15.
 */
public class FlowTest {

    @Test
    public void test_grouping_1() {
        List<List<Integer>> result = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<FlowGroup<Boolean, Integer>, List<Integer>>() {
                    @Override
                    public List<Integer> map(FlowGroup<Boolean, Integer> group) {
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
        List<List<Integer>> result = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .filter(new Filter<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public boolean allow(FlowGroup<Boolean, Integer> group) {
                        return group.getKey();
                    }
                })
                .flatMap(new Mapper<FlowGroup<Boolean, Integer>, List<Integer>>() {
                    int counter = 0;

                    @Override
                    public List<Integer> map(FlowGroup<Boolean, Integer> group) {
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
        List<Integer> result = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<FlowGroup<Boolean, Integer>, Integer>() {
                    @Override
                    public Integer map(FlowGroup<Boolean, Integer> group) {
                        return group.first().execute().get();
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
        Integer result = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer it) {
                        counter.incrementAndGet();
                        return it % 2 == 0;
                    }
                })
                .flatMap(new Mapper<FlowGroup<Boolean, Integer>, Integer>() {
                    @Override
                    public Integer map(FlowGroup<Boolean, Integer> group) {
                        return group.first().execute().get();
                    }
                })
                .first()
                .execute()
                .get();

        Assert.assertEquals(Integer.valueOf(1), result);
        Assert.assertEquals(1, counter.get());
    }

    static class Person {
        final String name;
        final int age;
        final long salary;

        Person(String name, int age, long salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }
    }

    @Test
    public void test_grouping_5() {
        Map<Integer, Map<Long, List<Person>>> result = Flow.of(new Person("Paul", 24, 20000),
                new Person("Mark", 24, 30000),
                new Person("Will", 28, 28000),
                new Person("William", 28, 28000)
        ).groupBy(new Mapper<Person, Integer>() {
            @Override
            public Integer map(Person person) {
                return person.age;
            }
        }).toMap(new Mapper<FlowGroup<Integer, Person>, Map<Long, List<Person>>>() {
            @Override
            public Map<Long, List<Person>> map(FlowGroup<Integer, Person> ageGroup) {
                return ageGroup.groupBy(new Mapper<Person, Long>() {
                    @Override
                    public Long map(Person person) {
                        return person.salary;
                    }
                }).toMap(new Mapper<FlowGroup<Long, Person>, List<Person>>() {
                    @Override
                    public List<Person> map(FlowGroup<Long, Person> salaryGroup) {
                        return salaryGroup.toList();
                    }
                });
            }
        });

        Assert.assertEquals(2, result.size());

        Assert.assertEquals(2, result.get(24).size());
        Assert.assertEquals(1, result.get(24).get(20000l).size());
        Assert.assertEquals("Paul", result.get(24).get(20000l).get(0).name);

        Assert.assertEquals(2, result.get(24).size());
        Assert.assertEquals(1, result.get(24).get(30000l).size());
        Assert.assertEquals("Mark", result.get(24).get(30000l).get(0).name);

        Assert.assertEquals(1, result.get(28).size());
        Assert.assertEquals(2, result.get(28).get(28000l).size());
        Assert.assertEquals("Will", result.get(28).get(28000l).get(0).name);
        Assert.assertEquals("William", result.get(28).get(28000l).get(1).name);

    }

    @Test
    public void test_sorted_list() {
        List<Integer> result = Flow.of(3, 2, 1).toSortedList();

        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    public void test_each() {
        final AtomicInteger counter = new AtomicInteger();

        Flow.of(1, 2, 3).each(new Consumer<Integer>() {
            @Override
            public void consume(Integer integer) {
                counter.incrementAndGet();
            }
        }).execute();

        Assert.assertEquals(3, counter.get());
    }

    @Test
    public void test_each_2() {
        final AtomicInteger counter = new AtomicInteger();

        Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .each(new Consumer<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public void consume(FlowGroup<Boolean, Integer> group) {
                        counter.incrementAndGet();
                    }
                }).execute();

        Assert.assertEquals(2, counter.get());
    }

    @Test
    public void test_each_3() {
        final AtomicInteger counter = new AtomicInteger();

        Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .each(new Consumer<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public void consume(FlowGroup<Boolean, Integer> group) {
                        counter.incrementAndGet();
                    }
                })
                .first()
                .execute()
                .get();

        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_first() {
        Assert.assertEquals(Integer.valueOf(1), Flow.of(1, 2, 3).first().execute().get());
    }

    @Test
    public void test_stop_after_first() {
        final AtomicInteger counter = new AtomicInteger();

        Assert.assertEquals(Integer.valueOf(1), Flow.of(1, 2, 3).each(new Consumer<Integer>() {
            @Override
            public void consume(Integer integer) {
                counter.incrementAndGet();
            }
        }).first().execute().get());

        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_last() {
        Assert.assertEquals(Integer.valueOf(3), Flow.of(1, 2, 3).last().execute().get());
    }

    @Test
    public void test_min() {
        Assert.assertEquals(Integer.valueOf(1), Flow.of(1, 2, 3).min());
        Assert.assertEquals(Integer.valueOf(1), Flow.of(3, 2, 1).min());
    }

    @Test
    public void test_min_2() {
        Comparator<Number> comparator = new Comparator<Number>() {
            @Override
            public int compare(Number o1, Number o2) {
                return Integer.valueOf(o1.intValue()).compareTo(o2.intValue());
            }
        };
        Assert.assertEquals(Integer.valueOf(1), Flow.of(1, 2, 3).min(comparator));
        Assert.assertEquals(Integer.valueOf(1), Flow.of(3, 2, 1).min(comparator));
    }

    @Test
    public void test_max() {
        Assert.assertEquals(Integer.valueOf(3), Flow.of(1, 2, 3).max());
        Assert.assertEquals(Integer.valueOf(3), Flow.of(3, 2, 1).max());
    }

    @Test
    public void test_max_2() {
        Comparator<Number> comparator = new Comparator<Number>() {
            @Override
            public int compare(Number o1, Number o2) {
                return Integer.valueOf(o1.intValue()).compareTo(o2.intValue());
            }
        };
        Assert.assertEquals(Integer.valueOf(3), Flow.of(1, 2, 3).max(comparator));
        Assert.assertEquals(Integer.valueOf(3), Flow.of(3, 2, 1).max(comparator));
    }

    @Test
    public void test_reduce() {
        Assert.assertEquals(Integer.valueOf(3), Flow.of(1, 2, 3).reduce(new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer a, Integer b) {
                return a > b ? a : b;
            }
        }).execute().get());
        Assert.assertEquals(Integer.valueOf(3), Flow.of(3, 2, 1).reduce(new Reducer<Integer>() {
            @Override
            public Integer reduce(Integer a, Integer b) {
                return a > b ? a : b;
            }
        }).execute().get());
    }

    @Test
    public void test_collect() {
        List<Integer> list = new ArrayList<Integer>();
        List<Integer> result = Flow.of(1, 2, 3)
                .collect(list, new BiConsumer<List<Integer>, Integer>() {
                    @Override
                    public void consume(List<Integer> integers, Integer integer) {
                        integers.add(integer * 2);
                    }
                })
                .execute()
                .get();

        Assert.assertSame(list, result);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1));
        Assert.assertEquals(Integer.valueOf(6), result.get(2));
    }

    @Test
    public void test_merge() {
        List<Integer> result = Flow.of(1, 2, 3, 4, 5, 6)
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
        List<Integer> result = Flow.of(1, 2, 3, 4, 5, 6)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .filter(new Filter<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public boolean allow(FlowGroup<Boolean, Integer> group) {
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

    @Test
    public void test_merge_3() {
        List<Integer> result = Flow.of(new int[]{1, 2}, new int[]{3, 4}, new int[]{5, 6})
                .merge(new Mapper<int[], Flow<Integer>>() {
                    @Override
                    public Flow<Integer> map(int[] ints) {
                        return Flow.of(ints);
                    }
                })
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
    public void test_merge_4() {
        List<Integer> result = Flow.of(1, 2, 3, 4, 5, 6)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .filter(new Filter<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public boolean allow(FlowGroup<Boolean, Integer> group) {
                        return group.getKey();
                    }
                })
                .merge(new Mapper<FlowGroup<Boolean, Integer>, Flow<Integer>>() {
                    @Override
                    public Flow<Integer> map(FlowGroup<Boolean, Integer> group) {
                        return group.map(new Mapper<Integer, Integer>() {
                            @Override
                            public Integer map(Integer integer) {
                                return integer / 2;
                            }
                        });
                    }
                })
                .toList();

        Assert.assertEquals(3, result.size());
        Assert.assertEquals(Integer.valueOf(1), result.get(0));
        Assert.assertEquals(Integer.valueOf(2), result.get(1));
        Assert.assertEquals(Integer.valueOf(3), result.get(2));
    }

    @Test
    public void test_count() {
        int result = Flow.of(1, 2, 3, 4, 5, 6).count().execute().get();

        Assert.assertEquals(6, result);
    }

    @Test
    public void test_map() {
        List<String> result = Flow.of(1, 2, 3)
                .map(new Mapper<Integer, String>() {
                    @Override
                    public String map(Integer integer) {
                        return integer.toString();
                    }
                }).toList();

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("1", result.get(0));
        Assert.assertEquals("2", result.get(1));
        Assert.assertEquals("3", result.get(2));
    }

    @Test
    public void test_filter() {
        List<Integer> result = Flow.of(1, 2, 3, 4)
                .filter(new Filter<Integer>() {
                    @Override
                    public boolean allow(Integer integer) {
                        return integer % 2 == 0;
                    }
                }).toList();

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Integer.valueOf(2), result.get(0));
        Assert.assertEquals(Integer.valueOf(4), result.get(1));
    }

    @Test
    public void test_of_iterable() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        int result = Flow.of(list).count().execute().get();

        Assert.assertEquals(3, result);
    }

    @Test
    public void test_of_iterable_2() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        int result = Flow.of(list).first().execute().get();

        Assert.assertEquals(1, result);
    }

    @Test
    public void test_of_iterator() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        int result = Flow.of(list.iterator()).first().execute().get();

        Assert.assertEquals(1, result);
    }

    @Test
    public void test_do_nothing() {
        Flow.of(new Iterator() {
            @Override
            public boolean hasNext() {
                throw new IllegalStateException("should not be called");
            }

            @Override
            public Object next() {
                throw new IllegalStateException("should not be called");
            }

            @Override
            public void remove() {
                throw new IllegalStateException("should not be called");
            }
        }).start();
    }

    @Test
    public void test_toMap() {
        Map<Boolean, List<Integer>> map = Flow.of(1, 2, 3)
                .toMap(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                }, Flow.<Boolean, Integer>flowGroupListMapper());

        Assert.assertEquals(2, map.size());

        Assert.assertEquals(1, map.get(true).size());
        Assert.assertEquals(Integer.valueOf(2), map.get(true).get(0));

        Assert.assertEquals(2, map.get(false).size());
        Assert.assertEquals(Integer.valueOf(1), map.get(false).get(0));
        Assert.assertEquals(Integer.valueOf(3), map.get(false).get(1));
    }

    @Test
    public void test_toMap_2() {
        Map<Boolean, List<Integer>> map = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .toMap();

        Assert.assertEquals(2, map.size());

        Assert.assertEquals(1, map.get(true).size());
        Assert.assertEquals(Integer.valueOf(2), map.get(true).get(0));

        Assert.assertEquals(2, map.get(false).size());
        Assert.assertEquals(Integer.valueOf(1), map.get(false).get(0));
        Assert.assertEquals(Integer.valueOf(3), map.get(false).get(1));
    }

    @Test
    public void test_toMap_3() {
        Map<Boolean, List<Integer>> map = Flow.of(1, 2, 3)
                .groupBy(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                })
                .filter(new Filter<FlowGroup<Boolean, Integer>>() {
                    @Override
                    public boolean allow(FlowGroup<Boolean, Integer> group) {
                        return group.getKey();
                    }
                })
                .toMap();

        Assert.assertEquals(1, map.size());

        Assert.assertEquals(1, map.get(true).size());
        Assert.assertEquals(Integer.valueOf(2), map.get(true).get(0));
    }

    @Test
    public void test_toMap_4() {
        Map<Boolean, List<String>> map = Flow.of(1, 2, 3)
                .toMap(new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                }, new Mapper<FlowGroup<Boolean, Integer>, List<String>>() {
                    @Override
                    public List<String> map(FlowGroup<Boolean, Integer> group) {
                        return group.map(new Mapper<Integer, String>() {
                            @Override
                            public String map(Integer integer) {
                                return integer.toString();
                            }
                        }).toList();
                    }
                });

        Assert.assertEquals(2, map.size());

        Assert.assertEquals(1, map.get(true).size());
        Assert.assertEquals("2", map.get(true).get(0));

        Assert.assertEquals(2, map.get(false).size());
        Assert.assertEquals("1", map.get(false).get(0));
        Assert.assertEquals("3", map.get(false).get(1));
    }

    @Test
    public void test_toMap_6() {
        Map<Boolean, List<Integer>> map = Flow.of(1, 2, 3)
                .toMap(new Supplier<Map<Boolean, FlowGroup<Boolean, Integer>>>() {
                    @Override
                    public Map<Boolean, FlowGroup<Boolean, Integer>> supply() {
                        return new TreeMap<Boolean, FlowGroup<Boolean, Integer>>();
                    }
                }, new Mapper<Integer, Boolean>() {
                    @Override
                    public Boolean map(Integer integer) {
                        return integer % 2 == 0;
                    }
                });

        Assert.assertEquals(2, map.size());

        Assert.assertEquals(1, map.get(true).size());
        Assert.assertEquals(Integer.valueOf(2), map.get(true).get(0));

        Assert.assertEquals(2, map.get(false).size());
        Assert.assertEquals(Integer.valueOf(1), map.get(false).get(0));
        Assert.assertEquals(Integer.valueOf(3), map.get(false).get(1));
    }

    @Test
    public void test_join() {
        Assert.assertEquals("1,2,3", Flow.of(1, 2, 3).join(","));
    }

    @Test
    public void improveCoverage() {
        Assert.assertEquals(null, new Flow().get());
        Assert.assertEquals(Integer.valueOf(0), new FlowCount().get());
    }

    @Test
    public void test_of_ints() {
        Assert.assertEquals("1,2,3", Flow.of(new int[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new int[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_longs() {
        Assert.assertEquals("1,2,3", Flow.of(new long[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new long[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_shorts() {
        Assert.assertEquals("1,2,3", Flow.of(new short[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new short[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_bytes() {
        Assert.assertEquals("1,2,3", Flow.of(new byte[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new byte[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_floats() {
        Assert.assertEquals("1.0,2.0,3.0", Flow.of(new float[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new float[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_doubles() {
        Assert.assertEquals("1.0,2.0,3.0", Flow.of(new double[]{1, 2, 3}).join(","));

        IllegalState flow = Flow.of(new double[]{1, 2, 3}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_booleans() {
        Assert.assertEquals("true,false", Flow.of(new boolean[]{true, false}).join(","));

        IllegalState flow = Flow.of(new boolean[]{true, false}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    @Test
    public void test_of_chars() {
        Assert.assertEquals("a,b,c", Flow.of(new char[]{'a', 'b', 'c'}).join(","));

        IllegalState flow = Flow.of(new char[]{'a', 'b', 'c'}).then(new IllegalState());
        flow.stop();
        flow.execute();
        Assert.assertEquals(null, flow.get());
    }

    static class IllegalState extends FlowProcessor {
        @Override
        public void process(Object o) {
            throw new IllegalStateException();
        }
    }

    @Test
    public void test_of_map() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "1");
        map.put(2, "2");
        map.put(3, "3");

        String result = Flow.of(map).map(new Mapper<Map.Entry<Integer, String>, Integer>() {
            @Override
            public Integer map(Map.Entry<Integer, String> entry) {
                return entry.getKey();
            }
        }).join(", ");

        Assert.assertEquals("1, 2, 3", result);
    }

    @Test
    public void test_each_with_index() {
        final StringBuilder sb = new StringBuilder();
        Flow.of(2, 4, 6).each(new ConsumerWithInt<Integer>() {
            @Override
            public void consume(int i, Integer integer) {
                if (sb.length() != 0)
                    sb.append(", ");
                sb.append(i);
            }
        }).execute();

        Assert.assertEquals("0, 1, 2", sb.toString());
    }

    @Test
    public void test_each_with_index_2() {
        final StringBuilder sb = new StringBuilder();
        Flow.of(2, 4, 6)
                .groupBy(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer) {
                        return integer;
                    }
                })
                .each(new ConsumerWithInt<FlowGroup<Integer, Integer>>() {
                    @Override
                    public void consume(int i, FlowGroup<Integer, Integer> integerIntegerFlowGroup) {
                        if (sb.length() != 0)
                            sb.append(", ");
                        sb.append(i);
                    }
                })
                .execute();

        Assert.assertEquals("0, 1, 2", sb.toString());
    }

    @Test
    public void test_each_with_index_3() {
        final StringBuilder sb = new StringBuilder();
        Flow.of(2, 4, 6)
                .each(new ConsumerWithInt<Integer>() {
                    @Override
                    public void consume(int i, Integer integer) {
                        if (sb.length() != 0)
                            sb.append(", ");
                        sb.append(i);
                    }
                })
                .first()
                .execute()
                .get();

        Assert.assertEquals("0", sb.toString());
    }

    @Test
    public void test_each_with_index_4() {
        final StringBuilder sb = new StringBuilder();
        Flow.of(2, 4, 6)
                .groupBy(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer) {
                        return integer;
                    }
                })
                .each(new ConsumerWithInt<FlowGroup<Integer, Integer>>() {
                    @Override
                    public void consume(int i, FlowGroup<Integer, Integer> integerIntegerFlowGroup) {
                        if (sb.length() != 0)
                            sb.append(", ");
                        sb.append(i);
                    }
                })
                .first()
                .execute()
                .get();

        Assert.assertEquals("0", sb.toString());
    }

    @Test
    public void test_any() {
        Assert.assertTrue(Flow.of(1, 2, 3).any(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 == 0;
            }
        }));
        Assert.assertFalse(Flow.of(1, 3, 5).any(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 == 0;
            }
        }));
    }

    @Test
    public void test_all() {
        Assert.assertTrue(Flow.of(1, 3, 5).all(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 != 0;
            }
        }));
        Assert.assertFalse(Flow.of(1, 2, 3).all(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 != 0;
            }
        }));
    }

    @Test
    public void test_none() {
        Assert.assertTrue(Flow.of(1, 3, 5).none(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 == 0;
            }
        }));
        Assert.assertFalse(Flow.of(1, 2, 3).none(new Filter<Integer>() {
            @Override
            public boolean allow(Integer integer) {
                return integer % 2 != 0;
            }
        }));
    }

    @Test
    public void test_skip() {
        Assert.assertEquals("3,4,5", Flow.of(1, 2, 3, 4, 5).skip(2).join(","));
    }

    @Test
    public void test_skip_2() {
        Assert.assertEquals("3,4,5", Flow.of(1, 2, 3, 4, 5)
                .groupBy(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer) {
                        return integer;
                    }
                })
                .skip(2)
                .map(new Mapper<FlowGroup<Integer, Integer>, Integer>() {
                    @Override
                    public Integer map(FlowGroup<Integer, Integer> group) {
                        return group.key;
                    }
                }).join(","));
    }

    @Test
    public void test_limit() {
        Assert.assertEquals("1,2,3", Flow.of(1, 2, 3, 4, 5).limit(3).join(","));
    }

    @Test
    public void test_limit_2() {
        Assert.assertEquals("1,2,3", Flow.of(1, 2, 3, 4, 5)
                .groupBy(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer) {
                        return integer;
                    }
                })
                .limit(3)
                .map(new Mapper<FlowGroup<Integer, Integer>, Integer>() {
                    @Override
                    public Integer map(FlowGroup<Integer, Integer> group) {
                        return group.key;
                    }
                })
                .join(","));
    }

    @Test
    public void test_limit_3() {
        Assert.assertEquals("{1=[1], 2=[2], 3=[3]}", Flow.of(1, 2, 3, 4, 5)
                .groupBy(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer) {
                        return integer;
                    }
                })
                .limit(3)
                .toMap()
                .toString()
        );
    }

    @Test
    public void test_limit_4() {
        final AtomicInteger i = new AtomicInteger();
        Assert.assertEquals("0,1,2", Flow.of(new Supplier<Integer>() {
            @Override
            public Integer supply() {
                Assert.assertTrue(i.get() < 5);
                return i.get() < 5 ? i.getAndIncrement() : null;
            }
        }).limit(3).join(","));
    }

    @Test
    public void test_supplier() {
        final int[] ints = new int[]{1, 2, 3};
        final AtomicInteger i = new AtomicInteger();
        Assert.assertEquals("1,2,3", Flow.of(new Supplier<Integer>() {
            @Override
            public Integer supply() {
                return i.get() < ints.length ? ints[i.getAndIncrement()] : null;
            }
        }).join(","));
    }

    @Test
    public void test_async() {
        List<String> result = Flow.of("a", "b", "c").async(new Mapper<String, Flow<String>>() {
            @Override
            public Flow<String> map(String s) {
                return Flow.of(s.toUpperCase());
            }
        }).toList();

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsAll(Arrays.asList("A", "B", "C")));
    }
}
