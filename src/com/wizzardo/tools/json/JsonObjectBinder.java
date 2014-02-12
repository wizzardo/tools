package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
public class JsonObjectBinder implements ObjectBinder {
    private JsonObject json;

    public JsonObjectBinder(JsonObject json) {
        this.json = json;
    }

    public JsonObjectBinder() {
        this.json = new JsonObject();
    }

    public void set(String key, Object value) {
        json.put(key, new JsonItem(value));
    }

    public void set(String key, JsonItem value) {
        json.put(key, value);
    }

    @Override
    public Object getObject() {
        return new JsonItem(json);
    }

    @Override
    public Field getField(String key) {
        return null;
    }
}
