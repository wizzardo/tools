package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.Fields;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by wizzardo on 14/07/16.
 */
public class JsonFields extends Fields<JsonFieldInfo> {
    public JsonFields(Map<String, JsonFieldInfo> map) {
        super(map);
    }

    public JsonFields(Class clazz) {
        super(clazz);
    }

    public JsonFields(Class clazz, Mapper<Field, JsonFieldInfo> mapper) {
        super(clazz, mapper);
    }

    JsonFieldInfo[] fields(){
        return array;
    }

    @Override
    protected JsonFieldInfo[] createArray(int size) {
        return new JsonFieldInfo[size];
    }
}
