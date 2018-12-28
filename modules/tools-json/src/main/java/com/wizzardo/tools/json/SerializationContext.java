package com.wizzardo.tools.json;

import com.wizzardo.tools.reflection.Fields;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationContext {
    private Map<JsonGeneric, JsonFields> cachedFields = new ConcurrentHashMap<JsonGeneric, JsonFields>();
    private Map<Class, JsonGeneric> cachedJsonGenerics = new ConcurrentHashMap<Class, JsonGeneric>();
    private Fields.FieldMapper<JsonFieldInfo, JsonGeneric> jsonFieldInfoMapper = Binder.JSON_FIELD_INFO_MAPPER;

    public <T> JsonGeneric<T> getGeneric(Class<T> clazz) {
        JsonGeneric<T> jsonGeneric = cachedJsonGenerics.get(clazz);
        if (jsonGeneric != null)
            return jsonGeneric;

        cachedJsonGenerics.put(clazz, jsonGeneric = new JsonGeneric<T>(this, clazz));
        return jsonGeneric;
    }

    public <T> JsonGeneric<T> getGeneric(Class<T> clazz, Class... generic) {
        JsonGeneric[] generics = new JsonGeneric[generic.length];
        for (int i = 0; i < generic.length; i++) {
            generics[i] = getGeneric(generic[i]);
        }
        return getGeneric(clazz, generics);
    }

    public <T> JsonGeneric<T> getGeneric(Class<T> clazz, JsonGeneric... generic) {
        return JsonGeneric.copyWithoutTypesAndInterfaces(getGeneric(clazz), generic);
    }

    public JsonFields getFields(Class clazz) {
        return getGeneric(clazz).getFields();
    }

    public JsonFields getFields(JsonGeneric generic) {
        JsonFields fields = cachedFields.get(generic);
        if (fields == null) {
            synchronized (generic.clazz) {
                fields = cachedFields.get(generic);
                if (fields != null)
                    return fields;

                fields = new JsonFields(generic, jsonFieldInfoMapper);
                cachedFields.put(generic, fields);
            }
        }
        return fields;
    }

    public void setJsonFieldInfoMapper(Fields.FieldMapper<JsonFieldInfo, JsonGeneric> mapper) {
        jsonFieldInfoMapper = mapper;
    }
}
