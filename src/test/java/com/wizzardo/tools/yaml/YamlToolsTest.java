package com.wizzardo.tools.yaml;

import com.wizzardo.tools.misc.Appender;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void simple_test_10() {
        String data = "key: \n" +
                "  sub:\n" +
                "#  comment: qwerty\n" +
                "    subsub: value\n" +
                "    #subsub2: value\n" +
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

    @Test
    public void test_toYaml() {
        YamlObject object = new YamlObject();
        object.append("key", "value");
        object.append("nullKey", (Object) null);

        YamlArray array = new YamlArray();
        array.add(new YamlItem("item1"));
        array.add(new YamlItem(2));
        object.append("array", array);

        YamlItem item = new YamlItem(object);
        StringBuilder sb = new StringBuilder();
        item.toYaml(Appender.create(sb));

        String yaml = sb.toString();
        System.out.println("Generated YAML:");
        System.out.println("---");
        System.out.println(yaml);
        System.out.println("---");

        Assert.assertEquals("key: \"value\"\n" +
                "nullKey: null\n" +
                "array: \n" +
                "  - \"item1\"\n" +
                "  - 2", yaml);
    }

    @Test
    public void test_documents() {
        String data = "key: value\n" +
                "\n" +
                "a: b\n" +
                "---\n" +
                "key: value2\n" +
                "a: c\n";
        List<YamlItem> docs = YamlTools.parseDocuments(data);
        Assert.assertEquals(2, docs.size());

        YamlItem item = docs.get(0);
        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(2, item.asYamlObject().size());
        Assert.assertEquals("value", item.asYamlObject().getAsString("key"));
        Assert.assertEquals("b", item.asYamlObject().getAsString("a"));

        item = docs.get(1);
        Assert.assertTrue(item.isYamlObject());
        Assert.assertEquals(2, item.asYamlObject().size());
        Assert.assertEquals("value2", item.asYamlObject().getAsString("key"));
        Assert.assertEquals("c", item.asYamlObject().getAsString("a"));
    }
}
