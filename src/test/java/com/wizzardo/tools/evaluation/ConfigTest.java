package com.wizzardo.tools.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
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
    public void test_7() {
        Config config = new Config();
        EvalTools.prepare("a { b = 1 }").get(config);

        Assert.assertEquals(1, config.size());
        Assert.assertEquals(1, ((Map) config.get("a")).size());
        Assert.assertEquals(1, ((Map) config.get("a")).get("b"));

        EvalTools.prepare("a { c = 2 }").get(config);
        Assert.assertEquals(1, config.size());
        Assert.assertEquals(2, ((Map) config.get("a")).size());
        Assert.assertEquals(1, ((Map) config.get("a")).get("b"));
        Assert.assertEquals(2, ((Map) config.get("a")).get("c"));
    }

    @Test
    public void test_8() {
        String s = "foo = 'foo'\n" +
                "sub { bar = \"$foo\"}\n" +
                "foobar = \"${foo}${sub.bar}\"";

        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        Assert.assertEquals(3, config.size());
        Assert.assertEquals("foo", config.get("foo"));
        Assert.assertEquals("foo", ((Map) config.get("sub")).get("bar"));
        Assert.assertEquals("foofoo", config.get("foobar"));
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

    static class TestClass {
        int i;
        long l;
        short s;
        byte b;
        boolean flag;
        float f;
        double d;
        char c;

        Integer ii;
        Long ll;
        Short ss;
        Byte bb;
        Boolean aBoolean;
        Double dd;
        Float ff;
        Character cc;

        String string;
        List list;
        Map map;

        TestClass aClass;
    }

    @Test
    public void test_bind_1() {
        String s = "" +
                "a.i = 1\n" +
                "a.l = 1l\n" +
                "a.s = Short.valueOf('1')\n" +
                "a.b = Byte.valueOf('1')\n" +
                "a.flag = true\n" +
                "a.f = 1f\n" +
                "a.d = 1.0\n" +
                "a.c = '1'.charAt(0)\n" +
                "\n" +
                "a.ii = 1\n" +
                "a.ll = 1l\n" +
                "a.ss = Short.valueOf('1')\n" +
                "a.bb = Byte.valueOf('1')\n" +
                "a.aBoolean = true\n" +
                "a.ff = 1f\n" +
                "a.dd = 1.0\n" +
                "a.cc = '1'.charAt(0)\n" +
                "\n" +
                "a.string = 'string'\n" +
                "a.list = [1,2,3]\n" +
                "a.map = [1:'1', 2:'2']\n" +
                "";
        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        TestClass t = config.config("a").bind(TestClass.class);

        Assert.assertEquals(1, t.i);
        Assert.assertEquals(1l, t.l);
        Assert.assertEquals(1, t.s);
        Assert.assertEquals(1, t.b);
        Assert.assertEquals(true, t.flag);
        Assert.assertEquals(1f, t.f, 0);
        Assert.assertEquals(1d, t.d, 0);
        Assert.assertEquals('1', t.c);

        Assert.assertEquals(Integer.valueOf(1), t.ii);
        Assert.assertEquals(Long.valueOf(1l), t.ll);
        Assert.assertEquals(Short.valueOf((short) 1), t.ss);
        Assert.assertEquals(Byte.valueOf((byte) 1), t.bb);
        Assert.assertEquals(Boolean.TRUE, t.aBoolean);
        Assert.assertEquals(Float.valueOf(1f), t.ff);
        Assert.assertEquals(Double.valueOf(1d), t.dd);
        Assert.assertEquals(Character.valueOf('1'), t.cc);

        Assert.assertEquals("string", t.string);
        Assert.assertEquals(3, t.list.size());
        Assert.assertEquals(2, t.map.size());

        Assert.assertEquals(null, t.aClass);
    }

    @Test
    public void test_bind_2() {
        String s = "" +
                "a.i = 1\n" +
                "a.aClass.i = 1\n" +
                "";
        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        TestClass t = config.config("a").bind(TestClass.class);

        Assert.assertEquals(1, t.i);
        Assert.assertEquals(1, t.aClass.i);
    }

    @Test
    public void test_closure() {
        String s = "" +
                "closure = {'bar'}\n" +
                "foo = closure()" +
                "";
        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        Assert.assertEquals("bar", config.get("foo"));
    }

    @Test
    public void test_bind__should_fail() {
        String s = "" +
                "a.l = 1\n" +
                "";
        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        try {
            config.config("a").bind(TestClass.class);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertEquals(IllegalStateException.class, e.getClass());
            Assert.assertEquals("Cannot bind '1' of class class java.lang.Integer to long " + TestClass.class.getName() + ".l", e.getMessage());
        }
    }


    public static class TestImport {
        public int i;
    }

    @Test
    public void test_imports() {
        String s = "import com.wizzardo.tools.evaluation.ConfigTest\n" +
                "a = new ConfigTest.TestImport()\n" +
                "a.i = 1" +
                "";
        Expression expression = EvalTools.prepare(s);
        Config config = new Config();
        expression.get(config);

        Assert.assertTrue(config.get("a") instanceof TestImport);
        Assert.assertEquals(1, config.get("a", new TestImport()).i);
    }
}
