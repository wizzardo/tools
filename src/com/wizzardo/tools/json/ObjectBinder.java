package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
public interface ObjectBinder {
    public void put(String key, Object value);

    public void put(String key, JsonItem value);

    public Object getObject();

    public Field getField(String key);

    public ObjectBinder getObjectBinder(String key);

    public ArrayBinder getArrayBinder(String key);
}
