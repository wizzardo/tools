package com.wizzardo.tools.json;

import com.wizzardo.tools.*;

import java.lang.reflect.Type;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
public class JsonArrayBinder implements ArrayBinder {
    private JsonArray json;

    public JsonArrayBinder(JsonArray json) {
        this.json = json;
    }

    public JsonArrayBinder() {
        this.json = new JsonArray();
    }

    public void add(Object value) {
        json.add(new JsonItem(value));
    }

    public void add(JsonItem value) {
        json.add(value);
    }

    @Override
    public Object getObject() {
        return json;
    }

    @Override
    public Pair<Class, Type> getGeneric() {
        return new Pair(null, null);
    }

    @Override
    public ObjectBinder getObjectBinder() {
        return new JsonObjectBinder();
    }

    @Override
    public ArrayBinder getArrayBinder() {
        return new JsonArrayBinder();
    }
}
