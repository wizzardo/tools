package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.collections.CollectionTools;
import com.wizzardo.tools.misc.Supplier;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.reflection.FieldInfo;
import com.wizzardo.tools.reflection.FieldReflection;
import com.wizzardo.tools.reflection.Fields;

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

    public Config config(String key) {
        return (Config) get(key);
    }

    public <T> T get(String key, T def) {
        Object value = super.get(key);
        if (value == null)
            return def;

        if (value instanceof Config && ((Config) value).isEmpty() && !(def instanceof Config))
            return def;

        return (T) value;
    }

    public <T> T get(String key, Supplier<T> def) {
        Object value = super.get(key);
        if (value == null)
            return def.supply();

        if (value instanceof Config && ((Config) value).isEmpty() && !(def instanceof Config))
            return def.supply();

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

    public <T> T bind(Class<T> clazz) {
        return bind(creteInstance(clazz));
    }

    public <T> T bind(T t) {
        Fields<FieldInfo> fields = new Fields<FieldInfo>(t.getClass());
        for (FieldInfo fieldInfo : fields) {
            FieldReflection reflection = fieldInfo.reflection;
            String name = fieldInfo.field.getName();
            switch (reflection.getType()) {
                case BOOLEAN:
                    reflection.setBoolean(t, get(name, Boolean.FALSE));
                    break;
                case BYTE:
                    reflection.setByte(t, get(name, (byte) 0));
                    break;
                case CHAR:
                    reflection.setChar(t, get(name, (char) 0));
                    break;
                case DOUBLE:
                    reflection.setDouble(t, get(name, 0.0));
                    break;
                case FLOAT:
                    reflection.setFloat(t, get(name, 0.0f));
                    break;
                case INTEGER:
                    reflection.setInteger(t, get(name, 0));
                    break;
                case LONG:
                    reflection.setLong(t, get(name, 0l));
                    break;
                case SHORT:
                    reflection.setShort(t, get(name, (short) 0));
                    break;
                case OBJECT: {
                    Object o = get(name);

                    if (fieldInfo.generic.clazz.isAssignableFrom(o.getClass())) {
                        reflection.setObject(t, o);
                        break;
                    }
                    Config config = (Config) o;
                    if (config.isEmpty()) {
                        reflection.setObject(t, null);
                        break;
                    }

                    try {
                        reflection.setObject(t, config.bind(fieldInfo.generic.clazz));
                    } catch (ClassCastException e) {
                        throw new IllegalStateException("Cannot bind " + o.getClass() + " to " + fieldInfo.field);
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unknown type of field " + fieldInfo.field);
            }
        }
        return t;
    }

    protected <T> T creteInstance(Class<T> clazz) {
        T t;
        try {
            t = clazz.newInstance();
        } catch (InstantiationException e) {
            throw Unchecked.rethrow(e);
        } catch (IllegalAccessException e) {
            throw Unchecked.rethrow(e);
        }
        return t;
    }
}
