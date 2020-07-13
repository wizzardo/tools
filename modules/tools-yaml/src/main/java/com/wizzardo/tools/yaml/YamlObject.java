package com.wizzardo.tools.yaml;


import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class YamlObject extends LinkedHashMap<String, YamlItem> {

    @Override
    public String toString() {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder builder) {
                Appender sb = Appender.create(builder);
                toYaml(sb);
                return sb.toString();
            }
        });
    }

    void toYaml(Appender sb) {
        sb.append('{');
        boolean comma = false;
        for (Map.Entry<String, YamlItem> entry : entrySet()) {
            if (comma)
                sb.append(',');
            else
                comma = true;

            sb.append('"');
            sb.append(entry.getKey());
            sb.append('"');
            sb.append(':');

            if (entry.getValue() == null)
                sb.append("null");
            else
                entry.getValue().toYaml(sb);
        }
        sb.append('}');
    }


    public boolean isNull(String key) {
        return get(key).isNull();
    }

    public boolean isYamlArray(String key) {
        return get(key).isYamlArray();
    }

    public boolean isYamlObject(String key) {
        return get(key).isYamlObject();
    }

    public String getAsString(String key) {
        return getAsString(key, null);
    }

    public String getAsString(String key, String def) {
        YamlItem item = get(key);
        return item == null ? def : item.asString();
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(String key, Class<T> cl) {
        return getAsEnum(key, cl, null);
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(String key, Class<T> cl, T def) {
        YamlItem item = get(key);
        return item == null ? def : item.asEnum(cl);
    }

    public Long getAsLong(String key) {
        return getAsLong(key, null);
    }

    public Long getAsLong(String key, Long def) {
        YamlItem item = get(key);
        return item == null ? def : item.asLong(def);
    }

    public Integer getAsInteger(String key) {
        return getAsInteger(key, null);
    }

    public Integer getAsInteger(String key, Integer def) {
        YamlItem item = get(key);
        return item == null ? def : item.asInteger(def);
    }

    public Double getAsDouble(String key) {
        return getAsDouble(key, null);
    }

    public Double getAsDouble(String key, Double def) {
        YamlItem item = get(key);
        return item == null ? def : item.asDouble(def);
    }

    public Float getAsFloat(String key) {
        return getAsFloat(key, null);
    }

    public Float getAsFloat(String key, Float def) {
        YamlItem item = get(key);
        return item == null ? def : item.asFloat(def);
    }

    public Boolean getAsBoolean(String key) {
        return getAsBoolean(key, null);
    }

    public Boolean getAsBoolean(String key, Boolean def) {
        YamlItem item = get(key);
        return item == null ? def : item.asBoolean(def);
    }


    public YamlObject getAsYamlObject(String key) {
        YamlItem item = get(key);
        return item == null ? null : item.asYamlObject();
    }

    public YamlArray getAsYamlArray(String key) {
        YamlItem item = get(key);
        return item == null ? null : item.asYamlArray();
    }

    public YamlObject append(String key, Object ob) {
        if (ob instanceof YamlItem) {
            put(key, (YamlItem) ob);
        } else
            put(key, new YamlItem(ob));
        return this;
    }

    public YamlObject append(String key, Collection ob) {
        if (ob instanceof YamlArray) {
            return append(key, ((YamlArray) ob));
        } else
            return append(key, new YamlArray().appendAll(ob));
    }

    public YamlObject append(String key, YamlArray ob) {
        put(key, new YamlItem(ob));
        return this;
    }

}