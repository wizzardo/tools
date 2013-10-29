package com.wizzardo.tools.utils.json;

import com.wizzardo.tools.json.JsonArray;
import com.wizzardo.tools.json.JsonObject;
import org.junit.Assert;
import com.wizzardo.tools.json.JsonBuilder;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        s = "{\"qwe\":\"qwe\", \"qwe\"e:\"qweqw\"}";
        assertEquals(2, JsonObject.parse(s).asJsonObject().size());


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
    }

    @Test
    public void parse2() {
        String s;

        s = "{a: true \n}";
        assertEquals(true, JsonBuilder.parse(s).asJsonObject().get("a").asBoolean());
        s = "{}";
        assertEquals(0, JsonBuilder.parse(s).asJsonObject().size());

        s = "{qwe:qwe}";
        assertEquals(1, JsonBuilder.parse(s).asJsonObject().size());
        s = "{qwe:qwe, qwee:qweqw}";
        assertEquals(2, JsonBuilder.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\"}";
        assertEquals(1, JsonBuilder.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\", \"qwe\"e:\"qweqw\"}";
        assertEquals(2, JsonBuilder.parse(s).asJsonObject().size());


        s = "{qwe:[1,2,3], qwee:qweqw}";
        assertEquals(3, JsonBuilder.parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "{qwe:qwe, qwee:[1,2,3]}";
        assertEquals(3, JsonBuilder.parse(s).asJsonObject().getAsJsonArray("qwee").size());
        s = "{qwe:qwe, qwee:[1,2,3],ewq:qwe}";
        assertEquals(3, JsonBuilder.parse(s).asJsonObject().size());

        s = "[{},{},{}]";
        assertEquals(3, JsonBuilder.parse(s).asJsonArray().size());
        s = "[{qwe:qwe},{},{}]";
        assertEquals(1, JsonBuilder.parse(s).asJsonArray().get(0).asJsonObject().size());

        s = "[{qwe:qwe},{},{qwe: true \n},werwr]";
        assertEquals(4, JsonBuilder.parse(s).asJsonArray().size());
        assertEquals(true, JsonBuilder.parse(s).asJsonArray().get(2).asJsonObject().getAsBoolean("qwe"));
        s = "{qwe:{qwe:qwe},rew:{},qw:{qwe: true \n},werwr:rew}";
        assertEquals(4, JsonBuilder.parse(s).asJsonObject().size());
    }

    public static class SimpleClass {
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
                "list:[1,2,3]" +
                "}";
        SimpleClass r = JsonObject.bind(s, SimpleClass.class);
        assertEquals(r.i, 1);
        assertEquals(r.integer, new Integer(2));
        assertTrue(r.f > 3.f && r.f < 3.2f);
        assertTrue(r.d > 4.d && r.d < 4.2);
        assertEquals(r.l, 5l);
        assertEquals(r.b, 6);
        assertEquals(r.s, "ololo lo lo");
        assertEquals(r.flag, true);

        assertEquals(r.array.length, 3);
        assertEquals(r.array[0], 1);
        assertEquals(r.array[1], 2);
        assertEquals(r.array[2], 3);

        assertEquals(r.list.size(), 3);
        assertEquals(new Integer(1), r.list.get(0));
        assertEquals(new Integer(2), r.list.get(1));
        assertEquals(new Integer(3), r.list.get(2));
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
                "    hex: 0xDEADbeef,\n" +
                "    half: .5,\n" +
                "    delta: +10,\n" +
            //    "    to: Infinity," +                 // also not supportes
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

        assertEquals(9, json.size());
        assertEquals("bar", json.getAsString("foo"));
        assertEquals(true, json.getAsBoolean("while"));
        assertEquals("is a \nmulti-line string", json.getAsString("this"));
        assertEquals("is another", json.getAsString("here"));
        assertEquals(0xDEADbeef, json.getAsInteger("hex").intValue());
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
}
