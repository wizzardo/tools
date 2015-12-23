package com.wizzardo.tools.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by wizzardo on 26.12.15.
 */
public class ConfigTest {

    @Test
    public void test_1() {
        String s = "key = 'value'";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("value", config.get("key"));
    }

    @Test
    public void test_2() {
        String s = "a.b = 'value'\n" +
                "a.c = 'value 2'";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("value", ((Map) config.get("a")).get("b"));
        Assert.assertEquals("value 2", ((Map) config.get("a")).get("c"));
    }

    @Test
    public void test_3() {
        String s = "a { b = 'value' }";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("value", ((Map) config.get("a")).get("b"));
    }

    @Test
    public void test_4() {
        String s = "a { b { c = 'value'} }";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("value", ((Map) ((Map) config.get("a")).get("b")).get("c"));
    }

    @Test
    public void test_5() {
        String s = "a { b = 'value'; c = 'value 2' }";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("value", ((Map) config.get("a")).get("b"));
        Assert.assertEquals("value 2", ((Map) config.get("a")).get("c"));
    }
}
