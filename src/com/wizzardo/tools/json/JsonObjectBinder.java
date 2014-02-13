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

    public void put(String key, Object value) {
        json.put(key, new JsonItem(value));
    }

    public void put(String key, JsonItem value) {
        json.put(key, value);
    }

    @Override
    public Object getObject() {
        return json;
    }

    @Override
    public Field getField(String key) {
        return null;
    }

    @Override
    public ObjectBinder getObjectBinder(String key) {
        return new JsonObjectBinder();
    }

    @Override
    public ArrayBinder getArrayBinder(String key) {
        return new JsonArrayBinder();
    }
}
