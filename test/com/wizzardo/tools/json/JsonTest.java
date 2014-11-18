package com.wizzardo.tools.json;

import com.wizzardo.tools.io.FileTools;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;
import com.wizzardo.tools.misc.WrappedException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author: moxa
 * Date: 1/18/13
 */
public class JsonTest {

    @Test
    public void parse() {
        String s;


        s = "{}";
        Assert.assertEquals(0, JsonTools.parse(s).asJsonObject().size());

        s = "{qwe:qwe}";
        assertEquals(1, JsonTools.parse(s).asJsonObject().size());
        s = "{qwe:1}";
        assertEquals(new Integer(1), JsonTools.parse(s).asJsonObject().getAsInteger("qwe"));
        s = "{qwe:null}";
        assertEquals(new Integer(1), JsonTools.parse(s).asJsonObject().getAsInteger("qwe", 1));
        assertEquals(true, JsonTools.parse(s).asJsonObject().get("qwe").isNull());
        assertEquals(true, JsonTools.parse(s).asJsonObject().isNull("qwe"));

        s = "{qwe:qwe, qwee:qweqw}";
        assertEquals(2, JsonTools.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\"}";
        assertEquals(1, JsonTools.parse(s).asJsonObject().size());

        s = "{\"qwe\":\"q\\\"we\\n\"}";
        assertEquals(1, JsonTools.parse(s).asJsonObject().size());
        assertEquals("q\"we\n", JsonTools.parse(s).asJsonObject().get("qwe").asString());

        s = "{qwe:[1,2,3], qwee:qweqw}";
        assertEquals(3, JsonTools.parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "{qwe:qwe, qwee:[1,2,3]}";
        assertEquals(3, JsonTools.parse(s).asJsonObject().getAsJsonArray("qwee").size());
        s = "{qwe:qwe, qwee:[1,2,3],ewq:qwe}";
        assertEquals(3, JsonTools.parse(s).asJsonObject().size());

        s = "[{},{},{}]";
        assertEquals(3, JsonTools.parse(s).asJsonArray().size());
        s = "{qwe:[]}";
        assertEquals(true, JsonTools.parse(s).asJsonObject().isJsonArray("qwe"));
        assertEquals(0, JsonTools.parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "[{qwe:qwe},{},{}]";
        assertEquals(1, JsonTools.parse(s).asJsonArray().get(0).asJsonObject().size());

        s = "[{qwe:qwe},{},{qwe: true \n},werwr]";
        assertEquals(4, JsonTools.parse(s).asJsonArray().size());
        assertEquals(true, JsonTools.parse(s).asJsonArray().get(2).asJsonObject().getAsBoolean("qwe"));
        s = "{qwe:{qwe:qwe},rew:{},qw:{qwe: true \n},werwr:rew}";
        assertEquals(4, JsonTools.parse(s).asJsonObject().size());
        assertEquals(true, JsonTools.parse(s).asJsonObject().isJsonObject("qwe"));
        assertEquals(false, JsonTools.parse(s).asJsonObject().isJsonObject("werwr"));
        assertEquals(true, JsonTools.parse(s).asJsonObject().getAsJsonObject("qw").getAsBoolean("qwe"));


        s = "{qwe:\"qw\\\"e\"}";
        assertEquals("qw\"e", JsonTools.parse(s).asJsonObject().getAsString("qwe"));

        s = "{'qwe\\\\':\"qw\\\"e\\\\\"}";
        assertEquals("qw\"e\\", JsonTools.parse(s).asJsonObject().getAsString("qwe\\"));

        s = "{'1':{'2':{'3':'value'}}}";
        assertEquals("value", JsonTools.parse(s).asJsonObject().getAsJsonObject("1").getAsJsonObject("2").getAsString("3"));

        s = "[[[value]]]";
        assertEquals("value", JsonTools.parse(s).asJsonArray().get(0).asJsonArray().get(0).asJsonArray().get(0).asString());
    }

    static public class Wrapper<T> {
        public T value;
    }

    static public class ListWrapper<E> extends Wrapper<List<E>> {
    }

    static public class AnotherWrapper extends ListWrapper<Wrapper<String>> {
    }

    private static enum SomeEnum {
        ONE, TWO, THREE
    }

    private static class SimpleClass {
        int i;
        public Integer integer;
        public float f;
        public double d;
        public long l;
        public short ss;
        public char c;
        public byte b;
        public String s;
        public boolean flag;
        public int[] array;
        public ArrayList<Integer> list;
        public AnotherWrapper wrapped;
        public final SomeEnum anEnum = SomeEnum.ONE;
    }

    private static class Child extends SimpleClass {
        private int value;
    }

    private static class Parent {
        private List<Child> children;
    }

    @Test
    public void bind() {
        String s = "{" +
                "i:1," +
                "integer:2," +
                "f:3.1," +
                "d:4.1," +
                "l:5," +
                "b:6," +
                "ss:7," +
                "c:\"8\"," +
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        SimpleClass r = JsonTools.parse(s, SimpleClass.class);
        assertEquals(r.i, 1);
        assertEquals(r.integer, new Integer(2));
        assertTrue(r.f > 3.f && r.f < 3.2f);
        assertTrue(r.d > 4.d && r.d < 4.2);
        assertEquals(r.l, 5l);
        assertEquals(r.b, 6);
        assertEquals(r.ss, 7);
        assertEquals(r.c, '8');
        assertEquals(r.s, "ololo lo lo");
        assertEquals(r.flag, true);
        assertEquals(r.anEnum, SomeEnum.THREE);
        assertEquals(r.wrapped.value.size(), 2);
        assertEquals(r.wrapped.value.get(0).value, "wrapped!");
        assertEquals(r.wrapped.value.get(1).value, "ololo");

        assertEquals(r.array.length, 3);
        assertEquals(r.array[0], 1);
        assertEquals(r.array[1], 2);
        assertEquals(r.array[2], 3);

        assertEquals(r.list.size(), 3);
        assertEquals(new Integer(1), r.list.get(0));
        assertEquals(new Integer(2), r.list.get(1));
        assertEquals(new Integer(3), r.list.get(2));


        s = "{" +
                "i:1," +
                "integer:2," +
                "f:3.1," +
                "d:4.1," +
                "l:5," +
                "b:6," +
                "ss:7," +
                "c:\"8\"," +
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "value:3," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        Child child = JsonTools.parse(s, Child.class);
        assertEquals(child.value, 3);
        assertEquals(child.i, 1);
        assertEquals(child.integer, new Integer(2));
        assertTrue(child.f > 3.f && child.f < 3.2f);
        assertTrue(child.d > 4.d && child.d < 4.2);
        assertEquals(child.l, 5l);
        assertEquals(child.b, 6);
        assertEquals(child.ss, 7);
        assertEquals(child.c, '8');
        assertEquals(child.s, "ololo lo lo");
        assertEquals(child.flag, true);
        assertEquals(child.wrapped.value.size(), 2);
        assertEquals(child.wrapped.value.get(0).value, "wrapped!");
        assertEquals(child.wrapped.value.get(1).value, "ololo");

        assertEquals(child.array.length, 3);
        assertEquals(child.array[0], 1);
        assertEquals(child.array[1], 2);
        assertEquals(child.array[2], 3);

        assertEquals(child.list.size(), 3);
        assertEquals(new Integer(1), child.list.get(0));
        assertEquals(new Integer(2), child.list.get(1));
        assertEquals(new Integer(3), child.list.get(2));


        s = "{" +
                "i:1," +
                "integer:2," +
                "f:3.1," +
                "d:4.1," +
                "l:5," +
                "b:6," +
                "ss:7," +
                "c:8," +
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "value:3," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        s = "{children:[" + s + "," + s + "," + s + "]}";
        Parent parent = JsonTools.parse(s, Parent.class);
        assertEquals(3, parent.children.size());
        for (int i = 0; i < 3; i++) {
            child = parent.children.get(0);

            assertEquals(child.value, 3);
            assertEquals(child.i, 1);
            assertEquals(child.integer, new Integer(2));
            assertTrue(child.f > 3.f && child.f < 3.2f);
            assertTrue(child.d > 4.d && child.d < 4.2);
            assertEquals(child.l, 5l);
            assertEquals(child.b, 6);
            assertEquals(child.ss, 7);
            assertEquals(child.c, 8);
            assertEquals(child.s, "ololo lo lo");
            assertEquals(child.flag, true);
            assertEquals(child.wrapped.value.size(), 2);
            assertEquals(child.wrapped.value.get(0).value, "wrapped!");
            assertEquals(child.wrapped.value.get(1).value, "ololo");

            assertEquals(child.array.length, 3);
            assertEquals(child.array[0], 1);
            assertEquals(child.array[1], 2);
            assertEquals(child.array[2], 3);

            assertEquals(child.list.size(), 3);
            assertEquals(new Integer(1), child.list.get(0));
            assertEquals(new Integer(2), child.list.get(1));
            assertEquals(new Integer(3), child.list.get(2));
        }

    }

    @Test
    public void testEscape() {
        String s = "СТОЯТЬ";
        assertEquals(s, JsonTools.escape(s));

        Assert.assertEquals("\\r\\n\\b\\t\\f\\\"\\\\", JsonTools.escape("\r\n\b\t\f\"\\"));
        Assert.assertEquals("\\u0001\\u0002\\u0003", JsonTools.escape("\u0001\u0002\u0003"));
        Assert.assertEquals("", JsonTools.escape(""));
        Assert.assertEquals("1", JsonTools.escape("1"));
        Assert.assertEquals("12", JsonTools.escape("12"));
        Assert.assertEquals("123", JsonTools.escape("123"));
        Assert.assertEquals("\\n", JsonTools.escape("\n"));
        Assert.assertEquals("1\\n", JsonTools.escape("1\n"));
        Assert.assertEquals("12\\n", JsonTools.escape("12\n"));
    }

    @Test
    public void testJson5() {
        String s = "{\n" +
                "    foo: 'bar',\n" +
                "    while: true,\n" +
                "\n" +
                "    this: 'is a \n" +
                "multi-line string',\n" +
                "\n" +
                //    "    // this is an inline comment\n" +          //comments not supported yet
                "    here: 'is another'," +
                //    " // inline comment\n" +
                "\n" +
//                "    /* this is a block comment\n" +
//                "       that continues on another line */\n" +
                "\n" +
//                "    hex: 0xDEADbeef,\n" +
                "    half: .5,\n" +
                "    delta: +10,\n" +
                //    "    to: Infinity," +                 // also not supported
                //    "   // and beyond!\n" +
                "\n" +
                "    finally: 'a trailing comma',\n" +
                "    oh: [\n" +
                "        \"we shouldn't forget\",\n" +
                "        'arrays can have',\n" +
                "        'trailing commas too',\n" +
                "    ],\n" +
                "}";

        JsonObject json = JsonTools.parse(s).asJsonObject();
//        System.out.println(json);

        assertEquals(8, json.size());
        assertEquals("bar", json.getAsString("foo"));
        assertEquals(true, json.getAsBoolean("while"));
        assertEquals("is a \nmulti-line string", json.getAsString("this"));
        assertEquals("is another", json.getAsString("here"));
//        assertEquals(0xDEADbeef, json.getAsInteger("hex").intValue());
        assertEquals("0.5", json.getAsFloat("half").toString());
        assertEquals(10, json.getAsInteger("delta").intValue());
//        assertEquals(Double.POSITIVE_INFINITY, json.getAsDouble("to").doubleValue());
        assertEquals("a trailing comma", json.getAsString("finally"));

        JsonArray array = json.getAsJsonArray("oh");
        assertEquals(3, array.size());
        assertEquals("we shouldn't forget", array.get(0).asString());
        assertEquals("arrays can have", array.get(1).asString());
        assertEquals("trailing commas too", array.get(2).asString());


    }

    @Test
    public void testUnicode() {
        String myString = "\\u0048\\u0065\\u006C\\u006C\\u006F World";
        Assert.assertEquals("Hello World", JsonTools.unescape(myString.toCharArray(), 0, myString.toCharArray().length));
        myString = "\\u0048\\u0065\\u006C\\u006C\\u006F";
        Assert.assertEquals("Hello", JsonTools.unescape(myString.toCharArray(), 0, myString.toCharArray().length));
        myString = "\\\"\\r\\n\\t\\b\\f\\\\";
        Assert.assertEquals("\"\r\n\t\b\f\\", JsonTools.unescape(myString.toCharArray(), 0, myString.toCharArray().length));

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.unescape("\\".toCharArray(), 0, 1);
            }
        }, IndexOutOfBoundsException.class, "unexpected end");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.unescape("\\u004".toCharArray(), 0, 5);
            }
        }, IndexOutOfBoundsException.class, "can't decode unicode character");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.unescape("\\a".toCharArray(), 0, 2);
            }
        }, IllegalStateException.class, "unexpected escaped char: a");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.unescape("\\u004G".toCharArray(), 0, 6);
            }
        }, IllegalStateException.class, "unexpected char for hex value");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.unescape("\\u004Я".toCharArray(), 0, 6);
            }
        }, IllegalStateException.class, "unexpected char for hex value: Я");


        Assert.assertEquals("\\u007F", JsonTools.escape("\u007F"));

        StringBuilder sb = new StringBuilder();
        Appender appender = Appender.create(sb);
        JsonTools.escape('\u007F', appender);
        JsonTools.escape('a', appender);
        JsonTools.escape('я', appender);
        Assert.assertEquals("\\u007Faя", sb.toString());
    }

    static class MapTest {
        Map<Integer, String> integerStringMap;
        Map<Integer, String[]> integerStringArrayMap;
        Map map;
        List<Map<String, Integer>> listMaps;
    }

    @Test
    public void testMapBinding() {
        String data = "{integerStringMap:{'1':'value'},integerStringArrayMap:{'2':['foo','bar']},map:{'key':'object'},listMaps:[{'foo':1,'bar':2},{'foo':3,'bar':4}]}";
        MapTest mapTest = JsonTools.parse(data, MapTest.class);

        assertEquals("value", mapTest.integerStringMap.get(1));
        assertArrayEquals(new String[]{"foo", "bar"}, mapTest.integerStringArrayMap.get(2));
        assertEquals("object", mapTest.map.get("key"));
        assertEquals((Integer) 1, mapTest.listMaps.get(0).get("foo"));
        assertEquals((Integer) 2, mapTest.listMaps.get(0).get("bar"));
        assertEquals((Integer) 3, mapTest.listMaps.get(1).get("foo"));
        assertEquals((Integer) 4, mapTest.listMaps.get(1).get("bar"));
    }

    @Test
    public void testListArrayBinding() {
        String s = "[[\"foo1\",\"foo2\",\"foo3\"],[\"bar1\",\"bar2\"]]";
        List<String[]> l = JsonTools.parse(s, List.class, (new String[0]).getClass());

        assertEquals(2, l.size());
        assertEquals(3, l.get(0).length);
        assertEquals(2, l.get(1).length);

        assertEquals("foo1", l.get(0)[0]);
        assertEquals("foo2", l.get(0)[1]);
        assertEquals("foo3", l.get(0)[2]);

        assertEquals("bar1", l.get(1)[0]);
        assertEquals("bar2", l.get(1)[1]);
    }

    @Test
    public void testListListBinding() {
        String s = "[[\"foo1\",\"foo2\",\"foo3\"],[\"bar1\",\"bar2\"]]";
        List<List<String>> l = JsonTools.parse(s, List.class, new Generic(List.class, String.class));

        assertEquals(2, l.size());
        assertEquals(3, l.get(0).size());
        assertEquals(2, l.get(1).size());

        assertEquals("bar1", l.get(1).get(0));
        assertEquals("bar2", l.get(1).get(1));
    }

    @Test
    public void testMapStringListIntegerBinding() {
        String s = "{\"key1\":[1,2,3],\"key2\":[4,5]}";
        Map<String, List<Integer>> map = JsonTools.parse(s, Map.class, new Generic(String.class), new Generic(List.class, Integer.class));

        assertEquals(2, map.size());
        assertEquals(3, map.get("key1").size());
        assertEquals(2, map.get("key2").size());

        assertEquals(new Integer(1), map.get("key1").get(0));
        assertEquals(new Integer(2), map.get("key1").get(1));
        assertEquals(new Integer(3), map.get("key1").get(2));

        assertEquals(new Integer(4), map.get("key2").get(0));
        assertEquals(new Integer(5), map.get("key2").get(1));
    }

    @Test
    public void testListMapStringIntegerBinding() {
        String s = "[{key:1}]";
        List<Map<String, Integer>> l = JsonTools.parse(s, List.class, new Generic(Map.class, String.class, Integer.class));

        assertEquals(1, l.size());
        assertEquals(new Integer(1), l.get(0).get("key"));
    }

    @Test
    public void testListMapIntegerIntegerBinding() {
        String s = "[{10:2}]";
        List<Map<Integer, Integer>> l = JsonTools.parse(s, List.class, new Generic(Map.class, Integer.class, Integer.class));

        assertEquals(1, l.size());
        assertEquals(new Integer(2), l.get(0).get(10));
    }

    @Test
    public void testIntegerArray() {
        String s = "[1,2,3]";
        Integer[] l = (Integer[]) JsonTools.parse(s, new Generic(Array.class, Integer.class));

        assertEquals(3, l.length);
        assertEquals(new Integer(1), l[0]);
        assertEquals(new Integer(2), l[1]);
        assertEquals(new Integer(3), l[2]);

        int[] arr = (int[]) JsonTools.parse(s, new Generic(Array.class, int.class));

        assertEquals(3, arr.length);
        assertEquals(1, arr[0]);
        assertEquals(2, arr[1]);
        assertEquals(3, arr[2]);

        int[] arr2 = JsonTools.parse(s, int[].class);

        assertEquals(3, arr2.length);
        assertEquals(1, arr2[0]);
        assertEquals(2, arr2[1]);
        assertEquals(3, arr2[2]);
    }

    static class StringHolder {
        String value;
    }

    @Test
    public void testObjectArray() {
        String s = "[{value:'value'}]";

        StringHolder[] arr = JsonTools.parse(s, StringHolder[].class);

        assertEquals(1, arr.length);
        assertEquals("value", arr[0].value);

        arr = (StringHolder[]) (Object) JsonTools.parse(s.toCharArray(), Array.class, new Generic(StringHolder.class));
        assertEquals(1, arr.length);
        assertEquals("value", arr[0].value);

        arr = (StringHolder[]) (Object) JsonTools.parse(s.toCharArray(), Array.class, StringHolder.class);
        assertEquals(1, arr.length);
        assertEquals("value", arr[0].value);
    }

    @Test
    public void testDate() {
        String data = "['2012-07-31T08:26:56+0000']";
        Date[] l = JsonTools.parse(data, Date[].class);

        assertEquals(1, l.length);
        assertEquals(1343723216000l, l[0].getTime());
    }

    static class ArraySerializeTest {
        String[][] strings;
    }

    static class ArraySerializeTest2 {
        List<String>[] strings;
    }

    static class ArraySerializeTest3 {
        Map<String, Long>[] strings;
    }

    static class SerializeTest {
        SerializeTestInner inner;
    }

    static class SerializeTestInner {
        String name;
        int counter;
    }

    static class IllegalAccessTest {
        private String value;
    }

    @Test
    public void serializeTest() {
        Map data = new LinkedHashMap();
        data.put(1, 1);
        data.put("2", "2");
        data.put("escaped", "esca\"ped");
        data.put("array", new int[]{1, 2, 3});
        data.put("list", Arrays.asList(1, 2, 3));
        data.put("map", new HashMap() {{
            put("foo", "bar");
        }});
        data.put("null", null);
        data.put("integers", new Integer[]{1, 2, 3});
        data.put("enum", SomeEnum.ONE);
        data.put("date", new Date(1343723216000l));

        String result = "{\"1\":1" +
                ",\"2\":\"2\"" +
                ",\"escaped\":\"esca\\\"ped\"" +
                ",\"array\":[1,2,3]" +
                ",\"list\":[1,2,3]" +
                ",\"map\":{\"foo\":\"bar\"}" +
                ",\"null\":null" +
                ",\"integers\":[1,2,3]" +
                ",\"enum\":\"ONE\"" +
                ",\"date\":\"2012-07-31T08:26:56.000Z\"" +
                "}";
        Assert.assertEquals(result, JsonTools.serialize(data));

        StringBuilder sb = new StringBuilder();
        JsonTools.serialize(data, sb);
        Assert.assertEquals(result, sb.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonTools.serialize(data, out);
        Assert.assertEquals(result, new String(out.toByteArray()));

        data = new LinkedHashMap();
        data.put("escaped", "\r\n\b\t\"");
        Assert.assertEquals("{\"escaped\":\"\\r\\n\\b\\t\\\"\"}", JsonTools.serialize(data));
        Assert.assertEquals("\\r\\n\\b\\t\\\"", JsonTools.escape("0\r\n\b\t\"".substring(1)));

        Assert.assertEquals("[1,2,3]", JsonTools.serialize(new int[]{1, 2, 3}));
        Assert.assertEquals("[1,2,3]", JsonTools.serialize(new long[]{1l, 2l, 3l}));
        Assert.assertEquals("[1,2,3]", JsonTools.serialize(new short[]{1, 2, 3}));
        Assert.assertEquals("[1,2,3]", JsonTools.serialize(new byte[]{1, 2, 3}));
        Assert.assertEquals("[1.0,2.0,3.0]", JsonTools.serialize(new float[]{1f, 2f, 3f}));
        Assert.assertEquals("[1.0,2.0,3.0]", JsonTools.serialize(new double[]{1d, 2d, 3d}));
        Assert.assertEquals("[true,false,true]", JsonTools.serialize(new boolean[]{true, false, true}));
        Assert.assertEquals("[\"\\u0001\",\"\\u0002\",\"\\u0003\"]", JsonTools.serialize(new char[]{1, 2, 3}));
        Assert.assertEquals("[\"\\u0001\",\"\\u0002\",\"\\u0003\"]",
                JsonTools.serialize(new Character[]{(char) 1, (char) 2, (char) 3}));
        Assert.assertEquals("[\"foo\",\"bar\"]", JsonTools.serialize(new String[]{"foo", "bar"}));
        Assert.assertEquals("[\"2012-07-31T08:26:56.000Z\",\"2012-07-31T08:26:57.000Z\"]",
                JsonTools.serialize(new Date[]{new Date(1343723216000l), new Date(1343723217000l)}));
        Assert.assertEquals("[[\"foo\",\"bar\"]]", JsonTools.serialize(new List[]{new ArrayList<String>() {{
            add("foo");
            add("bar");
        }}}));
        Assert.assertEquals("[{\"foo\":\"bar\"}]", JsonTools.serialize(new Map[]{new HashMap<String, String>() {{
            put("foo", "bar");
        }}}));
        Assert.assertEquals("[[\"foo\",\"bar\"]]", JsonTools.serialize(new String[][]{{"foo", "bar"}}));
        Assert.assertEquals("[[\"foo\",\"bar\"]]", JsonTools.serialize(new Object[][]{{"foo", "bar"}}));
        Assert.assertEquals("[\"ONE\",\"TWO\",\"THREE\"]", JsonTools.serialize(new SomeEnum[]{SomeEnum.ONE, SomeEnum.TWO, SomeEnum.THREE}));

        ArraySerializeTest arraySerializeTest = new ArraySerializeTest();
        arraySerializeTest.strings = new String[][]{{"foo", "bar"}};
        Assert.assertEquals("{\"strings\":[[\"foo\",\"bar\"]]}", JsonTools.serialize(arraySerializeTest));

        ArraySerializeTest2 arraySerializeTest2 = new ArraySerializeTest2();
        arraySerializeTest2.strings = new List[1];
        arraySerializeTest2.strings[0] = new ArrayList<String>();
        arraySerializeTest2.strings[0].add("foo");
        arraySerializeTest2.strings[0].add("bar");
        Assert.assertEquals("{\"strings\":[[\"foo\",\"bar\"]]}", JsonTools.serialize(arraySerializeTest2));

        arraySerializeTest2 = new ArraySerializeTest2();
        arraySerializeTest2.strings = new List[1];
        arraySerializeTest2.strings[0] = new ArrayList<String>();
        arraySerializeTest2.strings[0].add(null);
        arraySerializeTest2.strings[0].add(null);
        Assert.assertEquals("{\"strings\":[[null,null]]}", JsonTools.serialize(arraySerializeTest2));

        ArraySerializeTest3 arraySerializeTest3 = new ArraySerializeTest3();
        arraySerializeTest3.strings = new Map[1];
        arraySerializeTest3.strings[0] = new HashMap<String, Long>();
        arraySerializeTest3.strings[0].put("foo", 1l);
        arraySerializeTest3.strings[0].put("bar", 2l);
        Assert.assertEquals("{\"strings\":[{\"foo\":1,\"bar\":2}]}", JsonTools.serialize(arraySerializeTest3));

        JsonObject jsonObject = new JsonObject();
        jsonObject.put("null", null);
        Assert.assertEquals("{\"null\":null}", jsonObject.toString());
        jsonObject.append("null", new JsonItem(null));
        Assert.assertEquals("{\"null\":null}", jsonObject.toString());

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(null);
        Assert.assertEquals("[null]", jsonArray.toString());
        jsonArray.append(new JsonItem(null));
        Assert.assertEquals("[null,null]", jsonArray.toString());

        Assert.assertEquals("null", JsonTools.serialize(null));

        SerializeTest serializeTest = new SerializeTest();
        serializeTest.inner = new SerializeTestInner();
        serializeTest.inner.counter = 1;
        serializeTest.inner.name = "foo";
        Assert.assertEquals("{\"inner\":{\"name\":\"foo\",\"counter\":1}}", JsonTools.serialize(serializeTest));
    }

    @Test
    public void testJsonItem() {
        Assert.assertEquals(1, (int) JsonTools.parse("{key:1}").asJsonObject().get("key").asInteger(0));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:1}").asJsonObject().get("key").asLong(0l));
        Assert.assertEquals(1, (short) JsonTools.parse("{key:1}").asJsonObject().get("key").asShort((short) 0));
        Assert.assertEquals(1, (byte) JsonTools.parse("{key:1}").asJsonObject().get("key").asByte((byte) 0));
        Assert.assertEquals(1, (char) JsonTools.parse("{key:1}").asJsonObject().get("key").asChar((char) 0));
        Assert.assertEquals(1f, (float) JsonTools.parse("{key:1.0}").asJsonObject().get("key").asFloat(0f), 0);
        Assert.assertEquals(1d, (double) JsonTools.parse("{key:1.0}").asJsonObject().get("key").asDouble(0d), 0);
        Assert.assertEquals(true, JsonTools.parse("{key:true}").asJsonObject().get("key").asBoolean(false));

        Assert.assertEquals(1, (int) JsonTools.parse("{key:'1'}").asJsonObject().get("key").asInteger(0));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:'1'}").asJsonObject().get("key").asLong(0l));
        Assert.assertEquals(1, (short) JsonTools.parse("{key:'1'}").asJsonObject().get("key").asShort((short) 0));
        Assert.assertEquals(1, (byte) JsonTools.parse("{key:'1'}").asJsonObject().get("key").asByte((byte) 0));
        Assert.assertEquals('1', (char) JsonTools.parse("{key:'1'}").asJsonObject().get("key").asChar((char) 0));
        Assert.assertEquals(1f, (float) JsonTools.parse("{key:'1.0'}").asJsonObject().get("key").asFloat(0f), 0);
        Assert.assertEquals(1d, (double) JsonTools.parse("{key:'1.0'}").asJsonObject().get("key").asDouble(0d), 0);
        Assert.assertEquals(true, JsonTools.parse("{key:'true'}").asJsonObject().get("key").asBoolean(false));

        Assert.assertEquals(1, (int) JsonTools.parse("{key:null}").asJsonObject().get("key").asInteger(1));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:null}").asJsonObject().get("key").asLong(1l));
        Assert.assertEquals(1, (short) JsonTools.parse("{key:null}").asJsonObject().get("key").asShort((short) 1));
        Assert.assertEquals(1, (byte) JsonTools.parse("{key:null}").asJsonObject().get("key").asByte((byte) 1));
        Assert.assertEquals(1, (char) JsonTools.parse("{key:null}").asJsonObject().get("key").asChar((char) 1));
        Assert.assertEquals(1f, (float) JsonTools.parse("{key:null}").asJsonObject().get("key").asFloat(1f), 0);
        Assert.assertEquals(1d, (double) JsonTools.parse("{key:null}").asJsonObject().get("key").asDouble(1d), 0);
        Assert.assertEquals(true, JsonTools.parse("{key:null}").asJsonObject().get("key").asBoolean(true));

        Assert.assertEquals(1, (int) JsonTools.parse("{key:nan}").asJsonObject().get("key").asInteger(1));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:nan}").asJsonObject().get("key").asLong(1l));
        Assert.assertEquals(1, (short) JsonTools.parse("{key:nan}").asJsonObject().get("key").asShort((short) 1));
        Assert.assertEquals(1, (byte) JsonTools.parse("{key:nan}").asJsonObject().get("key").asByte((byte) 1));
        Assert.assertEquals(1, (char) JsonTools.parse("{key:nan}").asJsonObject().get("key").asChar((char) 1));
        Assert.assertEquals(1f, (float) JsonTools.parse("{key:nan}").asJsonObject().get("key").asFloat(1f), 0);
        Assert.assertEquals(1d, (double) JsonTools.parse("{key:nan}").asJsonObject().get("key").asDouble(1d), 0);
        Assert.assertEquals(true, JsonTools.parse("{key:[]}").asJsonObject().get("key").asBoolean(true));


        Assert.assertEquals(1, (int) JsonTools.parse("{key:'+1'}").asJsonObject().get("key").asInteger(0));
        Assert.assertEquals(-1, (int) JsonTools.parse("{key:'-1'}").asJsonObject().get("key").asInteger(0));
        Assert.assertEquals(255, (int) JsonTools.parse("{key:'0xff'}").asJsonObject().get("key").asInteger(0));

        Assert.assertEquals(1l, (long) JsonTools.parse("{key:'+1'}").asJsonObject().get("key").asLong(0l));
        Assert.assertEquals(-1l, (long) JsonTools.parse("{key:'-1'}").asJsonObject().get("key").asLong(0l));
        Assert.assertEquals(255l, (long) JsonTools.parse("{key:'0xff'}").asJsonObject().get("key").asLong(0l));

        Assert.assertEquals(1, (short) JsonTools.parse("{key:'+1'}").asJsonObject().get("key").asShort((short) 0));
        Assert.assertEquals(-1, (short) JsonTools.parse("{key:'-1'}").asJsonObject().get("key").asShort((short) 0));
        Assert.assertEquals(255, (short) JsonTools.parse("{key:'0xff'}").asJsonObject().get("key").asShort((short) 0));

        Assert.assertEquals(1, (byte) JsonTools.parse("{key:'+1'}").asJsonObject().get("key").asByte((byte) 0));
        Assert.assertEquals(-1, (byte) JsonTools.parse("{key:'-1'}").asJsonObject().get("key").asByte((byte) 0));
        Assert.assertEquals((byte) 255, (byte) JsonTools.parse("{key:'0xff'}").asJsonObject().get("key").asByte((byte) 0));

        Assert.assertEquals(1, (char) JsonTools.parse("{key:'+1'}").asJsonObject().get("key").asChar((char) 0));
        Assert.assertEquals('a', (char) JsonTools.parse("{key:'a'}").asJsonObject().get("key").asChar((char) 0));
        JsonItem item = JsonTools.parse("{key:'0xff'}").asJsonObject().get("key");
        Assert.assertEquals(255, (char) item.asChar((char) 0));
        Assert.assertEquals(255, (char) item.asChar((char) 0));

        Assert.assertEquals(1f, (float) JsonTools.parse("{key:'+1.0'}").asJsonObject().get("key").asFloat(0f), 0);
        Assert.assertEquals(-1f, (float) JsonTools.parse("{key:'-1.0'}").asJsonObject().get("key").asFloat(0f), 0);

        Assert.assertEquals(1d, (double) JsonTools.parse("{key:'+1.0'}").asJsonObject().get("key").asDouble(0d), 0);
        Assert.assertEquals(-1d, (double) JsonTools.parse("{key:'-1.0'}").asJsonObject().get("key").asDouble(0d), 0);

        Assert.assertEquals(true, JsonTools.parse("{key:'True'}").asJsonObject().get("key").asBoolean(false));


        Assert.assertEquals(false, JsonTools.parse("{key:value}").asJsonObject().get("key").isJsonArray());
        Assert.assertEquals(true, JsonTools.parse("{key:[]}").asJsonObject().get("key").isJsonArray());
        Assert.assertEquals(null, JsonTools.parse("{key:value}").asJsonObject().get("key").asJsonArray());

        Assert.assertEquals(false, JsonTools.parse("{key:value}").asJsonObject().get("key").isJsonObject());
        Assert.assertEquals(true, JsonTools.parse("{key:{}}").asJsonObject().get("key").isJsonObject());
        Assert.assertEquals(null, JsonTools.parse("{key:value}").asJsonObject().get("key").asJsonObject());


        Assert.assertEquals("1", JsonTools.parse("{key:1}").asJsonObject().get("key").getAs(String.class));
        Assert.assertEquals(1d, JsonTools.parse("{key:'1.0'}").asJsonObject().get("key").getAs(Double.class), 0);
        Assert.assertEquals(1f, JsonTools.parse("{key:'1.0'}").asJsonObject().get("key").getAs(Float.class), 0);
        Assert.assertEquals(1, (int) JsonTools.parse("{key:'1'}").asJsonObject().get("key").getAs(Integer.class));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:'1'}").asJsonObject().get("key").getAs(Long.class));
        Assert.assertEquals(1, (short) JsonTools.parse("{key:'1'}").asJsonObject().get("key").getAs(Short.class));
        Assert.assertEquals(1, (byte) JsonTools.parse("{key:'1'}").asJsonObject().get("key").getAs(Byte.class));
        Assert.assertEquals('1', (char) JsonTools.parse("{key:'1'}").asJsonObject().get("key").getAs(Character.class));
        Assert.assertEquals(true, JsonTools.parse("{key:True}").asJsonObject().get("key").getAs(Boolean.class));
        Assert.assertEquals(null, JsonTools.parse("{key:null}").asJsonObject().get("key").getAs(Object.class));
        Assert.assertEquals(null, JsonTools.parse("{key:object}").asJsonObject().get("key").getAs(StringBuilder.class));

        String value = "value";
        item = new JsonItem(value);
        Assert.assertTrue(value == item.get());
        value = "value_2";
        item.set(value);
        Assert.assertTrue(value == item.get());


        String json = new JsonObject()
                .append("foo", "bar")
                .append("object", new JsonObject()
                                .append("key", "value")
                )
                .append("array", new JsonArray()
                                .append(1)
                                .append(2)
                                .append(3)
                                .append(null)
                ).toString();
        Assert.assertEquals("{\"foo\":\"bar\",\"object\":{\"key\":\"value\"},\"array\":[1,2,3,null]}", json);
        Assert.assertEquals("1", new JsonItem(1).toString());
    }

    public static class WithoutFields {
    }

    @Test
    public void testNonExistsFields() {
        Assert.assertNotNull(JsonTools.parse("{key:value}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:[]}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:{}}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:{inner:{}}}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:{inner:[]}}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:[{}]}", WithoutFields.class));
        Assert.assertNotNull(JsonTools.parse("{key:[[]]}", WithoutFields.class));
    }

    @Test
    public void exceptionsTests() {
        testException(new Runnable() {
            @Override
            public void run() {
                Assert.assertNull(JsonTools.parse("[[]]", List.class));
            }
        }, IllegalArgumentException.class, "this binder only for collections and arrays! not for class java.lang.Object");

        testException(new Runnable() {
            @Override
            public void run() {
                new JavaArrayBinder(new Generic(List.class)).setTemporaryKey("key");
            }
        }, UnsupportedOperationException.class, "arrays has no keys");

        testException(new Runnable() {
            @Override
            public void run() {
                new JsonArrayBinder().setTemporaryKey("key");
            }
        }, UnsupportedOperationException.class, "arrays has no keys");

        testException(new Runnable() {
            @Override
            public void run() {
                Binder.getField(IntPrimitiveClass.class, "i").serializer.serialize(null, null, null);
            }
        }, IllegalStateException.class, "PrimitiveSerializer can serialize only primitives");
    }

    @Test
    public void jsonToolsTests() throws IOException {
        Assert.assertNotNull(new JsonTools()); // just for coverage

        File temp = File.createTempFile("prefix", "suffix");
        temp.deleteOnExit();
        FileTools.text(temp, "{key:value}");
        Assert.assertEquals("value", JsonTools.parse(temp).asJsonObject().get("key").asString());

        //only for java 6
        Assert.assertEquals("value", JsonTools.parse(" {key:value}".substring(1)).asJsonObject().get("key").asString());

        Assert.assertEquals("value", JsonTools.parse("{key:value}".toCharArray()).asJsonObject().get("key").asString());
        Assert.assertEquals(null, JsonTools.parse("key").get());

        Assert.assertEquals(null, JsonTools.parse("{key:}").asJsonObject().get("key"));

        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.parse("{key:value ]");
            }
        }, IllegalStateException.class, "here must be ',' or '}' , but found: ]");
        testException(new Runnable() {
            @Override
            public void run() {
                JsonTools.parse("[value }");
            }
        }, IllegalStateException.class, "here must be ',' or ']' , but found: }");


        Assert.assertEquals(1, (int) JsonTools.parse("{key:1}").asJsonObject().getAsInteger("key"));
        Assert.assertEquals(1l, (long) JsonTools.parse("{key:1}").asJsonObject().getAsLong("key"));
        Assert.assertEquals(1f, (float) JsonTools.parse("{key:1.0}").asJsonObject().getAsFloat("key"), 0);
        Assert.assertEquals(1d, (double) JsonTools.parse("{key:1.0}").asJsonObject().getAsDouble("key"), 0);
        Assert.assertEquals(true, JsonTools.parse("{key:true}").asJsonObject().getAsBoolean("key"));
    }

    public static class ForJsonUtilsTest {
        float f;
        double d;
        boolean b;
    }

    @Test
    public void jsonUtilsTests() throws IOException {
        Assert.assertNotNull(new JsonUtils()); // just for coverage

        Assert.assertEquals(3, JsonUtils.trimRight("123 ".toCharArray(), 0, 4));

        Assert.assertEquals(-1, (int) JsonTools.parse("{key:-1}").asJsonObject().get("key").asInteger());
        Assert.assertEquals(-1.2f, (float) JsonTools.parse("{key:-1.2}").asJsonObject().get("key").asFloat(), 0);

        ForJsonUtilsTest test = JsonTools.parse("{f:1,d:1,b:1}", ForJsonUtilsTest.class);
        Assert.assertEquals(1f, test.f, 0);
        Assert.assertEquals(1d, test.d, 0);
        Assert.assertEquals(true, test.b);

        Assert.assertEquals(false, JsonTools.parse("{key:false}").asJsonObject().get("key").asBoolean());
        Assert.assertEquals(false, JsonTools.parse("{ke\\\"y :false}").asJsonObject().get("ke\"y").asBoolean());
        Assert.assertEquals("val\\ue", JsonTools.parse("{key:val\\\\ue}").asJsonObject().get("key").asString());

        Assert.assertEquals(1, JsonUtils.parseNumber(null, "1".toCharArray(), 0, 1));
        Assert.assertEquals(1, JsonUtils.parseValue(null, "1   ".toCharArray(), 0, 4, ' '));
        Assert.assertEquals(4, JsonUtils.parseKey(null, "k  : ".toCharArray(), 0, 5));

        FieldInfo.charTree.append("k\\\"ey");
        Assert.assertEquals(8, JsonUtils.parseKey(null, "'k\\\"ey':value".toCharArray(), 0, 15));

        testException(new Runnable() {
            @Override
            public void run() {
                JsonUtils.parseNumber(null, "-".toCharArray(), 0, 1);
            }
        }, IllegalStateException.class, "no number found, only '-'");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonUtils.parseKey(null, "key value".toCharArray(), 0, 15);
            }
        }, IllegalStateException.class, "here must be key-value separator ':', but found: v");

        testException(new Runnable() {
            @Override
            public void run() {
                JsonUtils.parseKey(null, "key".toCharArray(), 0, 0);
            }
        }, IllegalStateException.class, "key not found");
    }

    public static class CustomMap extends HashMap<String, String> {
        String field;
    }

    public static class CustomMap2 extends HashMap<String, WithoutFields> {
        CustomMap foo;
        int[] arr;
    }

    public static class CustomMapParent extends HashMap<String, CustomMap> {
    }

    @Test
    public void javaMapBinderTests() throws IOException {
        CustomMap map = JsonTools.parse("{field:value, foo:bar}", CustomMap.class);
        Assert.assertEquals("value", map.field);
        Assert.assertEquals("bar", map.get("foo"));

        CustomMapParent mapParent = JsonTools.parse("{key:{field:value, foo:bar}}", CustomMapParent.class);
        map = mapParent.get("key");
        Assert.assertEquals("value", map.field);
        Assert.assertEquals("bar", map.get("foo"));

        CustomMap2 map2 = JsonTools.parse("{foo:{field:value},withoutFields:{},arr:[1,2,3]}", CustomMap2.class);
        Assert.assertEquals("value", map2.foo.field);
        Assert.assertNotNull(map2.get("withoutFields"));
        Assert.assertArrayEquals(new int[]{1, 2, 3}, map2.arr);
    }

    public static class FieldSetterTestClass {
        int i;
        long l;
        double d;
        float f;
        boolean b;
        byte bb;
        short s;
    }

    @Test
    public void jsonFieldSetterTests() throws IOException, NoSuchFieldException {
        String json = "{" +
                "i:'1'" +
                ",l:'2'" +
                ",d:'3.0'" +
                ",f:'4.0'" +
                ",b:'false'" +
                ",bb:'6'" +
                ",s:'7'" +
                "}";
        FieldSetterTestClass aClass = JsonTools.parse(json, FieldSetterTestClass.class);

        Assert.assertEquals(1, aClass.i);
        Assert.assertEquals(2l, aClass.l);
        Assert.assertEquals(3.0d, aClass.d, 0);
        Assert.assertEquals(4.0f, aClass.f, 0);
        Assert.assertEquals(false, aClass.b);
        Assert.assertEquals((byte) 6, aClass.bb);
        Assert.assertEquals((short) 7, aClass.s);

        Field f = FieldSetterTestClass.class.getDeclaredField("b");
        f.setAccessible(true);
        JsonFieldSetter.BooleanSetter booleanSetter = new JsonFieldSetter.BooleanSetter(f);
        booleanSetter.set(aClass, new JsonItem("True"));
        Assert.assertEquals(true, aClass.b);
    }

    static class IntPrimitiveClass {
        int i;
    }

    static class LongPrimitiveClass {
        long l;
    }

    static class BooleanPrimitiveClass {
        boolean b;
    }

    static class FloatPrimitiveClass {
        float f;
    }

    static class DoublePrimitiveClass {
        double d;
    }

    static class ShortPrimitiveClass {
        short s;
    }

    static class BytePrimitiveClass {
        byte b;
    }

    static class CharPrimitiveClass {
        char c;
    }

    @Test
    public void primitivesTests() {
        IntPrimitiveClass i = new IntPrimitiveClass();
        i.i = 1;
        Assert.assertEquals("{\"i\":1}", JsonTools.serialize(i));

        LongPrimitiveClass l = new LongPrimitiveClass();
        l.l = 1;
        Assert.assertEquals("{\"l\":1}", JsonTools.serialize(l));

        BooleanPrimitiveClass b = new BooleanPrimitiveClass();
        b.b = true;
        Assert.assertEquals("{\"b\":true}", JsonTools.serialize(b));

        FloatPrimitiveClass f = new FloatPrimitiveClass();
        f.f = 1.0f;
        Assert.assertEquals("{\"f\":1.0}", JsonTools.serialize(f));

        DoublePrimitiveClass d = new DoublePrimitiveClass();
        d.d = 1.0;
        Assert.assertEquals("{\"d\":1.0}", JsonTools.serialize(d));

        ShortPrimitiveClass s = new ShortPrimitiveClass();
        s.s = 1;
        Assert.assertEquals("{\"s\":1}", JsonTools.serialize(s));

        CharPrimitiveClass c = new CharPrimitiveClass();
        c.c = '1';
        Assert.assertEquals("{\"c\":\"1\"}", JsonTools.serialize(c));

        BytePrimitiveClass bytePrimitiveClass = new BytePrimitiveClass();
        bytePrimitiveClass.b = 1;
        Assert.assertEquals("{\"b\":1}", JsonTools.serialize(bytePrimitiveClass));
    }

    @Test
    public void primitivesTestsStringBuilder() {
        StringBuilder sb = new StringBuilder();
        IntPrimitiveClass i = new IntPrimitiveClass();
        i.i = 1;
        JsonTools.serialize(i, sb);
        Assert.assertEquals("{\"i\":1}", sb.toString());
        sb.setLength(0);

        LongPrimitiveClass l = new LongPrimitiveClass();
        l.l = 1;
        JsonTools.serialize(l, sb);
        Assert.assertEquals("{\"l\":1}", sb.toString());
        sb.setLength(0);

        BooleanPrimitiveClass b = new BooleanPrimitiveClass();
        b.b = true;
        JsonTools.serialize(b, sb);
        Assert.assertEquals("{\"b\":true}", sb.toString());
        sb.setLength(0);

        FloatPrimitiveClass f = new FloatPrimitiveClass();
        f.f = 1.0f;
        JsonTools.serialize(f, sb);
        Assert.assertEquals("{\"f\":1.0}", sb.toString());
        sb.setLength(0);

        DoublePrimitiveClass d = new DoublePrimitiveClass();
        d.d = 1.0;
        JsonTools.serialize(d, sb);
        Assert.assertEquals("{\"d\":1.0}", sb.toString());
        sb.setLength(0);
    }

    @Test
    public void primitivesTestsOutputStream() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IntPrimitiveClass i = new IntPrimitiveClass();
        i.i = 1;
        JsonTools.serialize(i, out);
        Assert.assertEquals("{\"i\":1}", new String(out.toByteArray()));
        out.reset();

        LongPrimitiveClass l = new LongPrimitiveClass();
        l.l = 1;
        JsonTools.serialize(l, out);
        Assert.assertEquals("{\"l\":1}", out.toString());
        out.reset();

        BooleanPrimitiveClass b = new BooleanPrimitiveClass();
        b.b = true;
        JsonTools.serialize(b, out);
        Assert.assertEquals("{\"b\":true}", out.toString());
        out.reset();

        FloatPrimitiveClass f = new FloatPrimitiveClass();
        f.f = 1.0f;
        JsonTools.serialize(f, out);
        Assert.assertEquals("{\"f\":1.0}", out.toString());
        out.reset();

        DoublePrimitiveClass d = new DoublePrimitiveClass();
        d.d = 1.0;
        JsonTools.serialize(d, out);
        Assert.assertEquals("{\"d\":1.0}", out.toString());
        out.reset();
    }


    static class FloatBoxedClass {
        Float f;
    }

    static class DoubleBoxedClass {
        Double d;
    }

    static class CharBoxedClass {
        Character c;
    }

    @Test
    public void boxedTests() {
        FloatBoxedClass f = new FloatBoxedClass();
        f.f = 1.0f;
        Assert.assertEquals("{\"f\":1.0}", JsonTools.serialize(f));

        DoubleBoxedClass d = new DoubleBoxedClass();
        d.d = 1.0;
        Assert.assertEquals("{\"d\":1.0}", JsonTools.serialize(d));

        CharBoxedClass c = new CharBoxedClass();
        c.c = '1';
        Assert.assertEquals("{\"c\":\"1\"}", JsonTools.serialize(c));
    }

    @Test
    public void appendersTests() {
        Appender appender;

        appender = Appender.create(new StringBuilder());
        Assert.assertEquals(appendData(appender), appender.toString());

        appender = Appender.create(new ExceptionDrivenStringBuilder());
        Assert.assertEquals(appendData(appender), appender.toString());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        appender = Appender.create(outputStream);
        Assert.assertEquals(appendData(appender), new String(outputStream.toByteArray()));

        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                while (true)
                    appender.append(' ');
            }
        }, WrappedException.class, "Pipe not connected");
        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                while (true)
                    appender.append("string");
            }
        }, WrappedException.class, "Pipe not connected");
        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                while (true)
                    appender.append("string", 0, 6);
            }
        }, WrappedException.class, "Pipe not connected");
        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                while (true)
                    appender.append("string_".toCharArray());
            }
        }, WrappedException.class, "Pipe not connected");
        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                while (true)
                    appender.append("string_".toCharArray(), 0, 6);
            }
        }, WrappedException.class, "Pipe not connected");
        testException(new Runnable() {
            @Override
            public void run() {
                Appender appender = Appender.create(new PipedOutputStream());
                appender.append(' ');
                appender.flush();
            }
        }, WrappedException.class, "Pipe not connected");
    }

    private String appendData(Appender appender) {
        appender.append("string_");
        appender.append("string_", 3, 6);
        appender.append('_');
        appender.append("string_".toCharArray());
        appender.append("string_".toCharArray(), 3, 6);
        appender.flush();

        return "string_ing_string_ing";
    }

    static class CustomList extends ArrayList {
        private CustomList() {
        }
    }

    static interface NoSuchMethodExceptionTest {
    }

    static abstract class InstantiationExceptionTest {
    }

    static class ExceptionInConstructor {
        public ExceptionInConstructor() {
            throw new IllegalStateException("because of reasons");
        }
    }

    @Test
    public void testCreatingCollections() {
        Assert.assertEquals(ArrayList.class, Binder.createCollection(List.class).getClass());
        Assert.assertEquals(LinkedList.class, Binder.createCollection(LinkedList.class).getClass());

        Assert.assertEquals(HashSet.class, Binder.createCollection(Set.class).getClass());
        Assert.assertEquals(TreeSet.class, Binder.createCollection(TreeSet.class).getClass());


        Assert.assertEquals(HashMap.class, Binder.createMap(Map.class).getClass());
        Assert.assertEquals(TreeMap.class, Binder.createMap(TreeMap.class).getClass());


        testException(new Runnable() {
            @Override
            public void run() {
                Binder.createCollection(CustomList.class);
            }
        }, WrappedException.class, "Class com.wizzardo.tools.json.Binder can not access a member of class " +
                "com.wizzardo.tools.json.JsonTest$CustomList with modifiers \"private\"");
        testException(new Runnable() {
            @Override
            public void run() {
                Binder.createCollection(NoSuchMethodExceptionTest.class);
            }
        }, WrappedException.class, "com.wizzardo.tools.json.JsonTest$NoSuchMethodExceptionTest.<init>()");
        testException(new Runnable() {
            @Override
            public void run() {
                Binder.createCollection(ExceptionInConstructor.class);
            }
        }, WrappedException.class, null);
        testException(new Runnable() {
            @Override
            public void run() {
                Binder.createCollection(InstantiationExceptionTest.class);
            }
        }, WrappedException.class, null);
    }

    private void testException(Runnable closure, Class exceptionClass, String message) {
        boolean exception = false;
        try {
            closure.run();
        } catch (Exception e) {
            Assert.assertEquals(exceptionClass, e.getClass());
            Assert.assertEquals(message, e.getMessage());
            exception = true;
        }
        assert exception;
    }
}
