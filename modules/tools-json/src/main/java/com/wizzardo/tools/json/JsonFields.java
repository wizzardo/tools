package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.Fields;
import com.wizzardo.tools.reflection.Generic;

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

    public JsonFields(Class clazz, FieldMapper<JsonFieldInfo, Generic> mapper) {
        super(clazz, mapper);
    }

    public JsonFields(JsonGeneric clazz, FieldMapper<JsonFieldInfo, JsonGeneric> mapper) {
        super(clazz, mapper);
    }

    JsonFieldInfo[] fields() {
        return array;
    }

    @Override
    protected JsonFieldInfo[] createArray(int size) {
        return new JsonFieldInfo[size];
    }
}
