package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
interface JsonBinder {
    public void add(Object value);

    public void add(JsonItem value);

    public Object getObject();

    public JsonBinder getObjectBinder();

    public JsonBinder getArrayBinder();

    public void setTemporaryKey(String key);

    public JsonFieldSetter getFieldSetter();
}
