package com.wizzardo.tools.collections;

import org.junit.Assert;
import org.junit.Test;

public class RangeTest {

    @Test
    public void test() {
        Assert.assertTrue(new Range(0, 0).isEmpty());

        Range range = new Range(0, 3);

        Assert.assertFalse(range.isEmpty());
        Assert.assertEquals(3, range.size());
        Assert.assertEquals(Integer.valueOf(0), range.get(0));
        Assert.assertEquals(Integer.valueOf(1), range.get(1));
        Assert.assertEquals(Integer.valueOf(2), range.get(2));
        Assert.assertEquals(1, range.indexOf(1));

        Assert.assertTrue(range.contains(2));
        Assert.assertFalse(range.contains(3));

        Assert.assertEquals("0..3", range.toString());
    }

    @Test
    public void test_exceptions() {
        check(IndexOutOfBoundsException.class, "Index -1 is out of this range [0, 3)", new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).get(-1);
            }
        });
        check(IndexOutOfBoundsException.class, "Index 3 is out of this range [0, 3)", new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).get(3);
            }
        });
        check(IllegalArgumentException.class, "from must be lower or equal then to. 2 - 1", new Runnable() {
            @Override
            public void run() {
                new Range(2, 1);
            }
        });

        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).remove(0);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).remove(null);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).removeAll(new Range(0, 0));
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).retainAll(new Range(0, 0));
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).clear();
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).set(0, 1);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).add(0);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).add(0, -1);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).addAll(new Range(0, 0));
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).addAll(0, new Range(0, 0));
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).listIterator();
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).listIterator(0);
            }
        });
        check(UnsupportedOperationException.class, null, new Runnable() {
            @Override
            public void run() {
                new Range(0, 3).iterator().remove();
            }
        });
    }

    private void check(Class<? extends Exception> exceptionClass, String message, Runnable runnable) {
        try {
            runnable.run();
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertEquals(message, e.getMessage());
            Assert.assertEquals(exceptionClass, e.getClass());
        }
    }
}
