package com.wizzardo.tools.yaml;

import org.junit.Assert;
import org.junit.Test;

public class YamlToolsTest {

    @Test
    public void simple_test_1() {
        String data = "key: value";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(1, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
    }

    @Test
    public void simple_test_2() {
        String data = "key: ";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(1, item.asYamlObject().size());
        Assert.assertTrue(item.asYamlObject().isNull("key"));
    }

    @Test
    public void simple_test_3() {
        String data = "key: \n" +
                "  a: b";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(1, item.asYamlObject().size());
        Assert.assertNotNull(item.asYamlObject().getAsYamlObject("key"));
        Assert.assertEquals(1, item.asYamlObject().getAsYamlObject("key").size());
        Assert.assertEquals("b", item.asYamlObject().getAsYamlObject("key").getAsString("a"));
    }

    @Test
    public void simple_test_5() {
        String data = "key: 'value'";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(1, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
    }

    @Test
    public void simple_test_6() {
        String data = "key: \"value\"";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(1, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
    }

    @Test
    public void simple_test_7() {
        String data = "key: value\n" +
                "# comment\n" +
                "a: b";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(2, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
        Assert.assertEquals("b", item.asYamlObject().getAsString("a"));
    }

    @Test
    public void simple_test_8() {
        String data = "key: value\n" +
                "\n" +
                "a: b";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(2, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
        Assert.assertEquals("b", item.asYamlObject().getAsString("a"));
    }

    @Test
    public void simple_test_9() {
        String data = "key: \n" +
                "  sub:\n" +
                "    subsub: value\n" +
                "\n" +
                "key2: b";
        YamlItem item = YamlTools.parse(data);

        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(2, item.asYamlObject().size());
        Assert.assertEquals(1, item.asYamlObject().getAsYamlObject("key").size());
        Assert.assertEquals(1, item.asYamlObject().getAsYamlObject("key").getAsYamlObject("sub").size());
        Assert.assertEquals("value", item.asYamlObject().getAsYamlObject("key").getAsYamlObject("sub").getAsString("subsub"));
        Assert.assertEquals("b", item.asYamlObject().getAsString("key2"));
    }
}
