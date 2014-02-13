package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
interface ObjectBinder {
    public void put(String key, Object value);

    public void put(String key, JsonItem value);

    public Object getObject();

    public ObjectBinder getObjectBinder(String key);

    public ArrayBinder getArrayBinder(String key);
}
