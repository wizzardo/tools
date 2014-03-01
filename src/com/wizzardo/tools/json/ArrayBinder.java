package com.wizzardo.tools.json;

import com.wizzardo.tools.*;
import com.wizzardo.tools.collections.Pair;

import java.lang.reflect.Type;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
interface ArrayBinder {
    public void add(Object value);

    public void add(JsonItem value);

    public Object getObject();

    public Pair<Class, Type> getGeneric();

    public ObjectBinder getObjectBinder();

    public ArrayBinder getArrayBinder();
}
