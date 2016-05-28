package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.Consumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzardo on 31/05/16.
 */
public class Fields {
    final Map<String, FieldInfo> map;
    final FieldInfo[] array;

    public Fields(Map<String, FieldInfo> map) {
        this.map = new HashMap<String, FieldInfo>(map.size(), 1);
        this.map.putAll(map);
        array = new FieldInfo[map.size()];
        int i = 0;
        for (FieldInfo fieldInfo : map.values()) {
            array[i++] = fieldInfo;
        }
    }

    public void each(Consumer<FieldInfo> consumer) {
        for (FieldInfo fieldInfo : array) {
            consumer.consume(fieldInfo);
        }
    }

    public FieldInfo get(String name) {
        return map.get(name);
    }

    public int size() {
        return map.size();
    }

    public boolean containsKey(String name) {
        return map.containsKey(name);
    }
}
