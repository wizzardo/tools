package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
interface ArrayBinder {
    public void add(Object value);

    public void add(JsonItem value);

    public Object getObject();

    public Generic getGeneric();

    public ObjectBinder getObjectBinder();

    public ArrayBinder getArrayBinder();
}
