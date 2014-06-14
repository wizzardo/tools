package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JsonObjectBinder implements JsonBinder {
    private JsonObject json;
    private String tempKey;

    public JsonObjectBinder() {
        this.json = new JsonObject();
    }

    public void add(Object value) {
        json.put(tempKey, new JsonItem(value));
    }

    public void add(JsonItem value) {
        json.put(tempKey, value);
    }

    @Override
    public Object getObject() {
        return json;
    }

    @Override
    public JsonBinder getObjectBinder() {
        return new JsonObjectBinder();
    }

    @Override
    public JsonBinder getArrayBinder() {
        return new JsonArrayBinder();
    }

    @Override
    public void setTemporaryKey(String key) {
        tempKey = key;
    }

    @Override
    public Generic getGeneric() {
        return null;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return null;
    }
}
