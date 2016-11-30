package com.wizzardo.tools.misc.event;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class EventBusTest {

    @Test
    public void test_on() {
        EventBus<String> bus = new EventBus<String>();

        final AtomicInteger counter = new AtomicInteger();
        bus.on("foo", new Listener<String, Integer>() {
            @Override
            public void on(String event, Integer data) throws Exception {
                counter.addAndGet(data);
            }
        });

        bus.trigger("foo", 1);
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_off_all() {
        EventBus<String> bus = new EventBus<String>();

        final AtomicInteger counter = new AtomicInteger();
        bus.on("foo", new Listener<String, Number>() {
            @Override
            public void on(String event, Number data) throws Exception {
                counter.addAndGet(data.intValue());
            }
        });

        bus.trigger("foo", 1);
        Assert.assertEquals(1, counter.get());


        bus.off("foo");
        bus.trigger("foo", 1);
        Assert.assertEquals(1, counter.get());
    }

    @Test
    public void test_off() {
        EventBus<String> bus = new EventBus<String>();

        final AtomicInteger counter = new AtomicInteger();
        Listener<String, Integer> listener = new Listener<String, Integer>() {
            @Override
            public void on(String event, Integer data) throws Exception {
                counter.addAndGet(data);
            }
        };
        bus.on("foo", listener);

        bus.trigger("foo", 1);
        Assert.assertEquals(1, counter.get());


        bus.off("foo", listener);
        bus.trigger("foo", 1);
        Assert.assertEquals(1, counter.get());
    }

}
