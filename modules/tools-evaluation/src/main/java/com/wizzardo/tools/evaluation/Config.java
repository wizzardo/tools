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

    protected final Config parent;
    protected final String name;

    public Config() {
        this(null, null);
    }

    public Config(String name) {
        this(null, name);
    }

    public Config(Config parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public Config parent() {
        return parent;
    }

    public String name() {
        return name;
    }

    @Override
    public Object get(Object key) {
        Object value = superGet(key);
        if (value != null)
            return value;

        if (parent() != null && (value = root().superGet(key)) != null) {
            if (value instanceof Config) {
                return createProxyConfig(this, (String) key, this, (Config) value);
            } else {
                return value;
            }
        }

        put((String) key, value = createConfig(this, (String) key));
        return value;
    }

    protected Config createConfig(Config parent, String name) {
        return new Config(parent, name);
    }

    protected Config createProxyConfig(Config main, String name, Config parent, Config proxy) {
        return new ProxyConfig(main, name, parent, proxy);
    }

    public Config root() {
        Config config = this;
        while (config.parent() != null) {
            config = config.parent();
        }
        return config;
    }

    protected Object superGet(Object key) {
        return super.get(key);
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
        Object value = get(key);

        if (value instanceof Config && ((Config) value).isEmpty() && !(def instanceof Config))
            return def;

        return (T) value;
    }

    public <T> T get(String key, Supplier<T> def) {
        Object value = get(key);

        if (value instanceof Config && ((Config) value).isEmpty())
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

            try {
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

                        reflection.setObject(t, config.bind(fieldInfo.generic.clazz));
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unknown type of field " + fieldInfo.field);
                }
            } catch (ClassCastException e) {
                Object o = get(name);
                throw new IllegalStateException("Cannot bind '" + o + "' of class " + o.getClass() + " to " + fieldInfo.field);
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

    protected static class ProxyConfig extends Config {
        protected final Config main;
        protected final String key;
        protected final Config proxy;
        protected Config config;

        public ProxyConfig(Config main, String key, Config parent, Config proxy) {
            super(parent, key);
            this.main = main;
            this.key = key;
            this.proxy = proxy;
        }

        @Override
        public Object get(Object key) {
            Object o = proxy.get(key);
            if (o instanceof Config)
                return createProxyConfig(this, (String) key, this, (Config) o);
            else
                return o;
        }

        @Override
        public Object execute(ClosureExpression it) {
            it.get(getConfig());
            return config;
        }

        protected Config getConfig() {
            if (config != null)
                return config;

            Config config = createConfig(parent(), name());
            main.put(key, this.config = config);
            return config;
        }

        @Override
        public Object put(String key, Object value) {
            return getConfig().put(key, value);
        }
    }
}
