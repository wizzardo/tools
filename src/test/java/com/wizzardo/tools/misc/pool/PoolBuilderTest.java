package com.wizzardo.tools.misc.pool;

import com.wizzardo.tools.misc.Supplier;
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
}
