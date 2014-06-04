package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JsonArrayBinder implements ArrayBinder {
    private JsonArray json;

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
    public Generic getGeneric() {
        return null;
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
