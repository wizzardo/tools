package com.wizzardo.tools.json;


import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.wizzardo.tools.json.JsonUtils.*;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonObject extends LinkedHashMap<String, JsonItem> {

    static int parse(char[] s, int from, int to, JsonBinder json) {
        int i = ++from;
        char current;
        outer:
        for (; i < to; i++) {
            current = s[i];

            if (current <= ' ')
                continue;

            if (current == '}')
                break;

            i = parseKey(json, s, i, to);

            i = skipSpaces(s, i, to);

            current = s[i];

            switch (current) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '-': {
                    i = parseNumber(json, s, i, to);
                    break;
                }
                case '{': {
                    if (json == null) {
                        i = JsonObject.parse(s, i, to, null);
                        break;
                    }
                    JsonBinder ob = json.getObjectBinder();
                    i = JsonObject.parse(s, i, to, ob);
                    if (ob != null)
                        json.add(ob.getObject());
                    break;
                }
                case '[': {
                    if (json == null) {
                        i = JsonArray.parse(s, i, to, null);
                        break;
                    }
                    JsonBinder ob = json.getArrayBinder();
                    i = JsonArray.parse(s, i, to, ob);
                    if (ob != null)
                        json.add(ob.getObject());
                    break;
                }
                case '}': {
                    break outer;
                }

                default: {
                    i = parseValue(json, s, i, to, '}');
                }
            }


            i = skipSpaces(s, i, to);
            current = s[i];

            if (current == ',')
                continue;

            if (current == '}')
                break;

            throw new IllegalStateException("here must be ',' or '}' , but found: " + current);

        }
        return i + 1;
    }

    @Override
    public String toString() {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder builder) {
                Appender sb = Appender.create(builder);
                toJson(sb);
                return sb.toString();
            }
        });
    }

    void toJson(Appender sb) {
        sb.append('{');
        boolean comma = false;
        for (Map.Entry<String, JsonItem> entry : entrySet()) {
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
                entry.getValue().toJson(sb);
        }
        sb.append('}');
    }


    public boolean isNull(String key) {
        return get(key).isNull();
    }

    public boolean isJsonArray(String key) {
        return get(key).isJsonArray();
    }

    public boolean isJsonObject(String key) {
        return get(key).isJsonObject();
    }

    public String getAsString(String key) {
        return getAsString(key, null);
    }

    public String getAsString(String key, String def) {
        JsonItem item = get(key);
        return item == null ? def : item.asString();
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(String key, Class<T> cl) {
        return getAsEnum(key, cl, null);
    }

    public <T extends Enum<T>> Enum<T> getAsEnum(String key, Class<T> cl, T def) {
        JsonItem item = get(key);
        return item == null ? def : item.asEnum(cl);
    }

    public Long getAsLong(String key) {
        return getAsLong(key, null);
    }

    public Long getAsLong(String key, Long def) {
        JsonItem item = get(key);
        return item == null ? def : item.asLong(def);
    }

    public Integer getAsInteger(String key) {
        return getAsInteger(key, null);
    }

    public Integer getAsInteger(String key, Integer def) {
        JsonItem item = get(key);
        return item == null ? def : item.asInteger(def);
    }

    public Double getAsDouble(String key) {
        return getAsDouble(key, null);
    }

    public Double getAsDouble(String key, Double def) {
        JsonItem item = get(key);
        return item == null ? def : item.asDouble(def);
    }

    public Float getAsFloat(String key) {
        return getAsFloat(key, null);
    }

    public Float getAsFloat(String key, Float def) {
        JsonItem item = get(key);
        return item == null ? def : item.asFloat(def);
    }

    public Boolean getAsBoolean(String key) {
        return getAsBoolean(key, null);
    }

    public Boolean getAsBoolean(String key, Boolean def) {
        JsonItem item = get(key);
        return item == null ? def : item.asBoolean(def);
    }


    public JsonObject getAsJsonObject(String key) {
        JsonItem item = get(key);
        return item == null ? null : item.asJsonObject();
    }

    public JsonArray getAsJsonArray(String key) {
        JsonItem item = get(key);
        return item == null ? null : item.asJsonArray();
    }

    public JsonObject append(String key, Object ob) {
        if (ob instanceof JsonItem) {
            put(key, (JsonItem) ob);
        } else
            put(key, new JsonItem(ob));
        return this;
    }

    public JsonObject append(String key, Collection ob) {
        if (ob instanceof JsonArray) {
            return append(key, ((JsonArray) ob));
        } else
            return append(key, new JsonArray().appendAll(ob));
    }

    public JsonObject append(String key, JsonArray ob) {
        put(key, new JsonItem(ob));
        return this;
    }

}