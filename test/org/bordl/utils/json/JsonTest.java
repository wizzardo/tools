package org.bordl.utils.json;

import org.bordl.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author: moxa
 * Date: 1/18/13
 */
public class JsonTest {
    @Test
    public void parse() {
        String s;


        s = "{}";
        assertEquals(0, JsonObject.parse(s).asJsonObject().size());

        s = "{qwe:qwe}";
        assertEquals(1, JsonObject.parse(s).asJsonObject().size());
        s = "{qwe:qwe, qwee:qweqw}";
        assertEquals(2, JsonObject.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\"}";
        assertEquals(1, JsonObject.parse(s).asJsonObject().size());
        s = "{\"qwe\":\"qwe\", \"qwe\"e:\"qweqw\"}";
        assertEquals(2, JsonObject.parse(s).asJsonObject().size());


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
    }
}
