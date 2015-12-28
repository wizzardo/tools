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

    @Test
    public void test_6() {
        String s = "a { ['user', 'password'].each {\n" +
//                "this.\"${it}\" = 'secret'\n" + // not implemented dynamic filed name yet
                "this[\"${it}\"] = 'secret'\n" +
                "} }";

        Expression expression = EvalTools.prepare(s);

        Config config = new Config();

        expression.get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals("secret", ((Map) config.get("a")).get("user"));
        Assert.assertEquals("secret", ((Map) config.get("a")).get("password"));
    }

    @Test
    public void test_merge_1() {
        Config configA = new Config();
        Config configB = new Config();
        EvalTools.prepare("a { b = 1 }").get(configA);
        EvalTools.prepare("a { c = 2 }").get(configB);

        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(1, ((Map) configA.get("a")).size());
        Assert.assertEquals(1, ((Map) configA.get("a")).get("b"));

        configA.merge(configB);
        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(2, ((Map) configA.get("a")).size());
        Assert.assertEquals(1, ((Map) configA.get("a")).get("b"));
        Assert.assertEquals(2, ((Map) configA.get("a")).get("c"));
    }

    @Test
    public void test_merge_2() {
        Config configA = new Config();
        Config configB = new Config();
        EvalTools.prepare("a { b = 1 }").get(configA);
        EvalTools.prepare("a = 1").get(configB);

        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(1, ((Map) configA.get("a")).size());
        Assert.assertEquals(1, ((Map) configA.get("a")).get("b"));

        configA.merge(configB);
        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(1, configA.get("a"));
    }

    @Test
    public void test_merge_3() {
        Config configA = new Config();
        Config configB = new Config();
        EvalTools.prepare("a = 1").get(configA);
        EvalTools.prepare("a { c = 2 }").get(configB);

        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(1, configA.get("a"));

        configA.merge(configB);
        Assert.assertEquals(1, configA.size());
        Assert.assertEquals(1, ((Map) configA.get("a")).size());
        Assert.assertEquals(2, ((Map) configA.get("a")).get("c"));
    }
}
