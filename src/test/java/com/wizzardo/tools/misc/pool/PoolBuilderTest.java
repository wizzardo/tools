package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.interfaces.Supplier;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by wizzardo on 10/07/16.
 */
public class PoolBuilderTest {

    @Test
    public void test_initSize() {
        final AtomicInteger counter = new AtomicInteger();
        Pool<Integer> pool = new PoolBuilder<Integer>()
                .supplier(new Supplier<Integer>() {
                    @Override
                    public Integer supply() {
                        return counter.incrementAndGet();
                    }
                })
                .queue(PoolBuilder.<Integer>createSharedQueueSupplier())
                .initialSize(1)
                .build();

        Assert.assertEquals(1, pool.size());
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_limitSize() {
        final AtomicInteger counter = new AtomicInteger();
        final Pool<Integer> pool = new PoolBuilder<Integer>()
                .supplier(new Supplier<Integer>() {
                    @Override
                    public Integer supply() {
                        return counter.incrementAndGet();
                    }
                })
                .queue(PoolBuilder.<Integer>createSharedQueueSupplier())
                .limitSize(1)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Holder<Integer> holder = pool.holder();
                Integer value = 0;
                try {
                    value = holder.get();
                    Thread.sleep(100);
                } catch (Exception ignored) {
                } finally {
                    holder.close();
                }
                Assert.assertEquals(1, value.intValue());
            }
        }).start();

        long time = System.currentTimeMillis();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }

        Assert.assertEquals(1, pool.get().intValue());
        time = System.currentTimeMillis() - time;
        Assert.assertTrue(time >= 100);
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_limitSize_2() {
        final AtomicInteger counter = new AtomicInteger();
        final Pool<Integer> pool = new PoolBuilder<Integer>()
                .supplier(new Supplier<Integer>() {
                    @Override
                    public Integer supply() {
                        return counter.incrementAndGet();
                    }
                })
                .queue(PoolBuilder.<Integer>createSharedQueueSupplier())
                .limitSize(1)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Integer value = pool.provide(new Mapper<Integer, Integer>() {
                    @Override
                    public Integer map(Integer integer)  {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return integer;
                    }
                });
                Assert.assertEquals(1, value.intValue());
            }
        }).start();

        long time = System.currentTimeMillis();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }

        Assert.assertEquals(1, pool.get().intValue());
        time = System.currentTimeMillis() - time;
        Assert.assertTrue(time >= 100);
        Assert.assertEquals(1, counter.get());
    }
}
