package com.wizzardo.tools.json;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        Assert.assertEquals(0, JsonObject.parse(s).asJsonObject().size());

        s = "{qwe:qwe}";
        assertEquals(1, JsonObject.parse(s).asJsonObject().size());
        s = "{qwe:1}";
        assertEquals(new Integer(1), JsonObject.parse(s).asJsonObject().getAsInteger("qwe"));
        s = "{qwe:null}";
        assertEquals(new Integer(1), JsonObject.parse(s).asJsonObject().getAsInteger("qwe", 1));
        assertEquals(true, JsonObject.parse(s).asJsonObject().get("qwe").isNull());
        assertEquals(true, JsonObject.parse(s).asJsonObject().isNull("qwe"));

        s = "{qwe:qwe, qwee:qweqw}";
        assertEquals(2, JsonObject.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\"}";
        assertEquals(1, JsonObject.parse(s).asJsonObject().size());

        s = "{\"qwe\":\"q\\\"we\\n\"}";
        assertEquals(1, JsonObject.parse(s).asJsonObject().size());
        assertEquals("q\"we\n", JsonObject.parse(s).asJsonObject().get("qwe").asString());

        s = "{qwe:[1,2,3], qwee:qweqw}";
        assertEquals(3, JsonObject.parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "{qwe:qwe, qwee:[1,2,3]}";
        assertEquals(3, JsonObject.parse(s).asJsonObject().getAsJsonArray("qwee").size());
        s = "{qwe:qwe, qwee:[1,2,3],ewq:qwe}";
        assertEquals(3, JsonObject.parse(s).asJsonObject().size());

        s = "[{},{},{}]";
        assertEquals(3, JsonObject.parse(s).asJsonArray().size());
        s = "{qwe:[]}";
        assertEquals(0, JsonObject.parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "[{qwe:qwe},{},{}]";
        assertEquals(1, JsonObject.parse(s).asJsonArray().get(0).asJsonObject().size());

        s = "[{qwe:qwe},{},{qwe: true \n},werwr]";
        assertEquals(4, JsonObject.parse(s).asJsonArray().size());
        assertEquals(true, JsonObject.parse(s).asJsonArray().get(2).asJsonObject().getAsBoolean("qwe"));
        s = "{qwe:{qwe:qwe},rew:{},qw:{qwe: true \n},werwr:rew}";
        assertEquals(4, JsonObject.parse(s).asJsonObject().size());
        assertEquals(true, JsonObject.parse(s).asJsonObject().getAsJsonObject("qw").getAsBoolean("qwe"));


        s = "{qwe:\"qw\\\"e\"}";
        assertEquals("qw\"e", JsonObject.parse(s).asJsonObject().getAsString("qwe"));

        s = "{'1':{'2':{'3':'value'}}}";
        assertEquals("value", JsonObject.parse(s).asJsonObject().getAsJsonObject("1").getAsJsonObject("2").getAsString("3"));

        s = "[[[value]]]";
        assertEquals("value", JsonObject.parse(s).asJsonArray().get(0).asJsonArray().get(0).asJsonArray().get(0).asString());
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
        public byte b;
        public String s;
        public boolean flag;
        public int[] array;
        public ArrayList<Integer> list;
        private AnotherWrapper wrapped;
        private final SomeEnum anEnum = SomeEnum.ONE;
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
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        SimpleClass r = JsonObject.parse(s, SimpleClass.class);
        assertEquals(r.i, 1);
        assertEquals(r.integer, new Integer(2));
        assertTrue(r.f > 3.f && r.f < 3.2f);
        assertTrue(r.d > 4.d && r.d < 4.2);
        assertEquals(r.l, 5l);
        assertEquals(r.b, 6);
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
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "value:3," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        Child child = JsonObject.parse(s, Child.class);
        assertEquals(child.value, 3);
        assertEquals(child.i, 1);
        assertEquals(child.integer, new Integer(2));
        assertTrue(child.f > 3.f && child.f < 3.2f);
        assertTrue(child.d > 4.d && child.d < 4.2);
        assertEquals(child.l, 5l);
        assertEquals(child.b, 6);
        assertEquals(child.s, "ololo lo lo");
        assertEquals(child.flag, true);
        assertEquals(r.wrapped.value.size(), 2);
        assertEquals(r.wrapped.value.get(0).value, "wrapped!");
        assertEquals(r.wrapped.value.get(1).value, "ololo");

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
                "s:\"ololo lo lo\"," +
                "flag:true," +
                "array:[1,2,3]," +
                "list:[1,2,3]," +
                "value:3," +
                "anEnum:\"THREE\"," +
                "wrapped:{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]}" +
                "}";
        s = "{children:[" + s + "," + s + "," + s + "]}";
        Parent parent = JsonObject.parse(s, Parent.class);
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
            assertEquals(child.s, "ololo lo lo");
            assertEquals(child.flag, true);
            assertEquals(r.wrapped.value.size(), 2);
            assertEquals(r.wrapped.value.get(0).value, "wrapped!");
            assertEquals(r.wrapped.value.get(1).value, "ololo");

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
        assertEquals(s, JsonObject.escape(s));
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

        JsonObject json = JsonObject.parse(s).asJsonObject();
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
        Assert.assertEquals("Hello World", JsonObject.unescape(myString.toCharArray(), 0, myString.toCharArray().length));
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
        MapTest mapTest = JsonObject.parse(data, MapTest.class);

        assertEquals("value", mapTest.integerStringMap.get(1));
        assertArrayEquals(new String[]{"foo", "bar"}, mapTest.integerStringArrayMap.get(2));
        assertEquals("object", mapTest.map.get("key"));
        assertEquals((Integer) 1, mapTest.listMaps.get(0).get("foo"));
        assertEquals((Integer) 2, mapTest.listMaps.get(0).get("bar"));
        assertEquals((Integer) 3, mapTest.listMaps.get(1).get("foo"));
        assertEquals((Integer) 4, mapTest.listMaps.get(1).get("bar"));
    }
}
