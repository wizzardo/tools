package com.wizzardo.tools.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.google.gson.Gson;
import com.wizzardo.tools.io.FileTools;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author: wizzardo
 * Date: 2/3/14
 */
public class JsonParserTest {

    private JsonItem parse(String s) {
        return JsonObject.parse(s);
    }

    @Test
    public void parse() {
        String s;

        s = "{}";

        Assert.assertEquals(0, parse(s).asJsonObject().size());

        s = "{qwe:qwe}";
        assertEquals(1, parse(s).asJsonObject().size());
        s = "{qwe:1}";
        assertEquals(new Integer(1), parse(s).asJsonObject().getAsInteger("qwe"));
        s = "{qwe:null}";
        assertEquals(new Integer(1), parse(s).asJsonObject().getAsInteger("qwe", 1));
        assertEquals(true, parse(s).asJsonObject().get("qwe").isNull());
        assertEquals(true, parse(s).asJsonObject().isNull("qwe"));

        s = "{qwe:qwe, qwee:qweqw}";
        assertEquals(2, parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\"}";
        assertEquals(1, parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\", \"qwe\"e:\"qweqw\"}";
        assertEquals(2, parse(s).asJsonObject().size());


        s = "{\"qwe\":\"q\\\"we\\n\"}";
        assertEquals(1, parse(s).asJsonObject().size());
        assertEquals("q\"we\n", parse(s).asJsonObject().get("qwe").asString());

        s = "{qwe:[1,2,3], qwee:qweqw}";
        assertEquals(3, parse(s).asJsonObject().getAsJsonArray("qwe").size());
        s = "{qwe:qwe, qwee:[1,2,3]}";
        assertEquals(3, parse(s).asJsonObject().getAsJsonArray("qwee").size());
        s = "{qwe:qwe, qwee:[1,2,3],ewq:qwe}";
        assertEquals(3, parse(s).asJsonObject().size());

        s = "[{},{},{}]";
        assertEquals(3, parse(s).asJsonArray().size());
        s = "[{qwe:qwe},{},{}]";
        assertEquals(1, parse(s).asJsonArray().get(0).asJsonObject().size());

        s = "[{qwe:qwe},{},{qwe: true \n},werwr]";
        assertEquals(4, parse(s).asJsonArray().size());
        assertEquals(true, parse(s).asJsonArray().get(2).asJsonObject().getAsBoolean("qwe"));
        s = "{qwe:{qwe:qwe},rew:{},qw:{qwe: true \n},werwr:rew}";
        assertEquals(4, parse(s).asJsonObject().size());
        assertEquals(true, parse(s).asJsonObject().getAsJsonObject("qw").getAsBoolean("qwe"));

        s = "{qwe:\"qw\\\"e\"}";
        assertEquals("qw\"e", parse(s).asJsonObject().getAsString("qwe"));

        s = "{'1':{'2':{'3':'value'}}}";
        assertEquals("value", parse(s).asJsonObject().getAsJsonObject("1").getAsJsonObject("2").getAsString("3"));

        s = "[[[value]]]";
        assertEquals("value", parse(s).asJsonArray().get(0).asJsonArray().get(0).asJsonArray().get(0).asString());
    }

//    @Test
    public void benchmark() throws InterruptedException {
        byte[] bytes = FileTools.bytes("/home/moxa/test.json");
        long time;
        int n = 100, k = 0;
        String json = new String(bytes);

        for (int i = 0; i < 1000; i++) {

            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
                com.google.gson.JsonParser gson = new com.google.gson.JsonParser();
//                k += gson.parse(new InputStreamReader(new ByteArrayInputStream(bytes))).getAsJsonArray().size();
                k += gson.parse(json).getAsJsonArray().size();
            }

            time = System.currentTimeMillis() - time;
            System.out.println("gson:\t" + time);
            Thread.sleep(1000);

            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
//                JsonParser wizzardo = new JsonParser();
//                wizzardo.parse(bytes);
//                k += wizzardo.getResult().asJsonArray().size();
//                k += JsonObject.parse(new String(bytes)).asJsonArray().size();
                k += JsonObject.parse(json).asJsonArray().size();
            }

            time = System.currentTimeMillis() - time;
            System.out.println("wizzardo:\t" + time);
            Thread.sleep(1000);


            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
                k += ((JSONArray) new DefaultJSONParser(json).parse()).size();
            }

            time = System.currentTimeMillis() - time;
            System.out.println("fastjson:\t" + time);
            Thread.sleep(1000);
            System.out.println();
        }

        System.out.println(k);
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
        AnotherWrapper wrapped;
        int i = 1;
        public Integer integer = 2;
        public float f = 3;
        public double d = 4;
        private final long l = 5;
        public byte b = 6;
        public String s = "foo bar";
        public boolean flag = true;
        public int[] array = new int[]{1, 2, 3};
        public ArrayList<Integer> list;
        private SomeEnum anEnum = SomeEnum.TWO;

//        {
//            list = new ArrayList<Integer>();
//            list.add(4);
//            list.add(5);
//            list.add(6);
//
//            wrapped = new AnotherWrapper();
//            wrapped.value = new ArrayList<Wrapper<String>>();
//            wrapped.value.add(new Wrapper<String>() {{
//                value = "wrapped!";
//            }});
//            wrapped.value.add(new Wrapper<String>() {{
//                value = "ololo";
//            }});
//        }
    }

    private static class Child extends SimpleClass {
        private int value = 25;
    }

    private static class Parent {
        private ArrayList<Child> children;
    }

//    @Test
    public void benchmarkBind() throws InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

//        ListWrapper<Integer> w = JsonObject.parse("[{\"value\":13},{\"value\":7}]",
//                new TypeReference<ListWrapper<Integer>>() { } );

//        String child = Binder.toJSON(new Child() {{
//            s = "ололо пыщпыщ qweqwe qwe qwe qwe qwe qwe qwe qwe qwe qwe qwe qw eqw eqw eqw eq we";
//        }});
        String child = "{\"f\":3.0,\"d\":4.0,\"wrapped\":{\"value\":[{\"value\":\"wrapped!\"},{\"value\":\"ololo\"}]},\"b\":6,\"integer\":2,\"l\":5,\"list\":[4,5,6],\"i\":1,\"flag\":true,\"s\":\"ололо пыщпыщ qweqwe qwe qwe qwe qwe qwe qwe qwe qwe qwe qwe qw eqw eqw eqw eq we\",\"value\":25,\"anEnum\":\"TWO\",\"array\":[1,2,3]}";
        System.out.println(child);

        StringBuilder sb = new StringBuilder();
        sb.append("{children:[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(child);
        }
        sb.append("]}");

        long time;
        int n = 1000, k = 0;
//        String json = sb.toString();
        String json = child;

        for (int i = 0; i < 1000; i++) {

            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
                com.google.gson.JsonParser gson = new com.google.gson.JsonParser();
//                k += gson.parse(new InputStreamReader(new ByteArrayInputStream(bytes))).getAsJsonArray().size();
//                k += gson.parse(json).getAsJsonObject().entrySet().size();
//                k += new Gson().fromJson(json, Parent.class).children.size();
                k += new Gson().fromJson(json, Child.class).array.length;
            }

            time = System.currentTimeMillis() - time;
            System.out.println("gson:\t" + time);
            Thread.sleep(1000);

            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
//                JsonParser wizzardo = new JsonParser();
//                wizzardo.parse(bytes);
//                k += wizzardo.getResult().asJsonArray().size();
//                k += JsonObject.parse(new String(bytes)).asJsonArray().size();
//                k += JsonObject.parse(json).asJsonObject().size();
//                k += JsonObject.parse(json, Parent.class).children.size();
                k += JsonObject.parse(json, Child.class).array.length;
//                k += Binder.fromJSON(Parent.class,JsonObject.parse(json)).children.size();
            }

            time = System.currentTimeMillis() - time;
            System.out.println("wizzardo:\t" + time);
            Thread.sleep(1000);


            time = System.currentTimeMillis();
            for (int j = 0; j < n; j++) {
                k += ((JSONObject) new DefaultJSONParser(json).parse()).size();
//                Parent p = new Parent();
//                new DefaultJSONParser(json).parseObject(p);
//                k += new DefaultJSONParser(json).parseObject(Parent.class).children.size();
//               Parent p= JSON.parseObject(json, Parent.class);
                Child p = JSON.parseObject(json, Child.class);
                k += p.array.length;
            }

            time = System.currentTimeMillis() - time;
            System.out.println("fastjson:\t" + time);
            Thread.sleep(1000);
            System.out.println();
        }

        System.out.println(k);
    }
}
