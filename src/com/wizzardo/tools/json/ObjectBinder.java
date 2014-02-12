package com.wizzardo.tools.json;

import java.lang.reflect.Field;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
public interface ObjectBinder {
    public void set(String key, Object value);

    public void set(String key, JsonItem value);

    public Object getObject();

    public Field getField(String key);
}
