package com.wizzardo.tools.collections;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Created by wizzardo on 14.09.15.
 */
public class CollectionToolsTest {

    @Test
    public void collection_tools() {
        Assert.assertNotNull(new CollectionTools());
    }

    @Test
    public void each() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        final AtomicInteger counter = new AtomicInteger();
        CollectionTools.each(list, new CollectionTools.VoidClosure<Integer>() {
            @Override
            public void execute(Integer it) {
                counter.addAndGet(it);
            }
        });
        Assert.assertEquals(6, counter.get());

        Map<Integer, Integer> map = new HashMap<Integer, Integer>() {{
            put(1, 2);
            put(2, 3);
        }};
        counter.set(0);
        CollectionTools.each(map, new CollectionTools.VoidClosure2<Integer, Integer>() {
            @Override
            public void execute(Integer key, Integer value) {
                counter.addAndGet(key * value);
            }
        });
        Assert.assertEquals(8, counter.get());
    }

    @Test
    public void eachWithIndex() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        final AtomicInteger counter = new AtomicInteger();
        CollectionTools.eachWithIndex(list, new CollectionTools.VoidClosure2<Integer, Integer>() {
            @Override
            public void execute(Integer i, Integer value) {
                counter.addAndGet(value * i);
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
    public void grep() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        List<Integer> result = CollectionTools.grep(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });

        Assert.assertEquals(1, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals((Integer) 2, result.get(0));
    }

    @Test
    public void grep_pattern() {
        List<String> list = new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
        }};
        List<String> result = CollectionTools.grep(list, Pattern.compile("2"));

        Assert.assertEquals(1, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals("2", result.get(0));
    }

    @Test
    public void grep_class() {
        List<Number> list = new ArrayList<Number>() {{
            add(1);
            add(2l);
            add(3d);
        }};
        List<Number> result = CollectionTools.grep(list, Long.class);

        Assert.assertEquals(1, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals(2l, result.get(0));
    }

    @Test
    public void grep_collection() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};


        List<Integer> result = CollectionTools.grep(list, new HashSet<Integer>() {{
            add(2);
        }});

        Assert.assertEquals(1, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals((Integer) 2, result.get(0));
    }

    @Test
    public void findAll() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        List<Integer> result = CollectionTools.findAll(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });

        Assert.assertEquals(1, result.size());
        Assert.assertNotSame(list, result);
        Assert.assertEquals((Integer) 2, result.get(0));
    }

    @Test
    public void find() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        Integer result = CollectionTools.find(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });
        Assert.assertEquals((Integer) 2, result);

        result = CollectionTools.find(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it > 5;
            }
        });
        Assert.assertEquals(null, result);
    }

    @Test
    public void remove() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        Integer result = CollectionTools.remove(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });

        Assert.assertEquals((Integer) 2, result);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals((Integer) 1, list.get(0));
        Assert.assertEquals((Integer) 3, list.get(1));

        result = CollectionTools.remove(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it > 5;
            }
        });
        Assert.assertEquals(null, result);
    }

    @Test
    public void removeAll() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        List<Integer> result = CollectionTools.removeAll(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });

        Assert.assertEquals(2, result.size());
        Assert.assertSame(list, result);
        Assert.assertEquals((Integer) 1, list.get(0));
        Assert.assertEquals((Integer) 3, list.get(1));
    }

    @Test
    public void every() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        boolean result = CollectionTools.every(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it > 0;
            }
        });
        Assert.assertEquals(true, result);

        result = CollectionTools.every(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });
        Assert.assertEquals(false, result);
    }

    @Test
    public void any() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        boolean result = CollectionTools.any(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        });
        Assert.assertEquals(true, result);

        result = CollectionTools.any(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it > 5;
            }
        });
        Assert.assertEquals(false, result);
    }

    @Test
    public void join() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
        }};
        String result = CollectionTools.join(list, ", ");
        Assert.assertEquals("1, 2, 3", result);
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

    @Test
    public void times() {
        final AtomicInteger counter = new AtomicInteger();
        CollectionTools.times(3, new CollectionTools.VoidClosure<Integer>() {
            @Override
            public void execute(Integer integer) {
                counter.addAndGet(integer);
            }
        });
        Assert.assertEquals(3, counter.get());
    }
}
