package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.collections.CollectionTools;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wizzardo on 22.12.15.
 */
public class Config extends HashMap<String, Object> implements CollectionTools.Closure<Object, ClosureExpression> {
    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value == null) {
            put((String) key, value = new Config());
        }
        return value;
    }

    @Override
    public Object execute(ClosureExpression it) {
        it.get(this);
        return this;
    }

    public <T> T get(String key, T def) {
        Object value = super.get(key);
        if (value == null)
            return def;

        if (value instanceof Config && ((Config) value).isEmpty() && !(def instanceof Config))
            return def;

        return (T) value;
    }

    public void merge(Map<String, Object> scr) {
        merge(this, scr);
    }

    private void merge(Map into, Map<?, ?> from) {
        for (Map.Entry<?, ?> entry : from.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Object in = into.get(entry.getKey());
                if (in instanceof Map) {
                    merge(((Map) in), ((Map) entry.getValue()));
                } else {
                    into.put(entry.getKey(), entry.getValue());
                }
            } else {
                into.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
