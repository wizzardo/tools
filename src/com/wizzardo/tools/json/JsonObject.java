package com.wizzardo.tools.json;


import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.wizzardo.tools.json.JsonUtils.*;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonObject extends LinkedHashMap<String, JsonItem> {

    public static JsonItem parse(File file) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        char[] buffer = new char[8192];
        StringWriter writer = new StringWriter();
        int count;
        while ((count = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, count);
        }
        reader.close();
        return parse(writer.toString());
    }

    public static JsonItem parse(String s) {
        return new JsonItem(parse(s, (Generic<Object>) null));
    }

    public static <T> T parse(String s, Class<T> clazz) {
        return parse(s, clazz, (Generic[]) null);
    }

    public static <T> T parse(String s, Class<T> clazz, Class... generic) {
        return parse(s, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(String s, Class<T> clazz, Generic... generic) {
        return parse(s, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(String s, Generic<T> generic) {
        s = s.trim();
        char[] data = toCharArray(s);
        int offset = 0;
        if (data.length != s.length())
            offset = getCharArrayOffset(s);
        return parse(data, offset, s.length(), generic);
    }

    public static JsonItem parse(char[] s) {
        return new JsonItem(parse(s, null, (Generic<Object>[]) null));
    }

    public static <T> T parse(char[] s, Class<T> clazz, Class... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, Class<T> clazz, Generic... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, int from, int to, Generic<T> generic) {
        // check first char
        if (s[0] == '{') {
            ObjectBinder binder = Binder.getObjectBinder(generic);
            parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        if (s[0] == '[') {
            ArrayBinder binder = Binder.getArrayBinder(generic);
            JsonArray.parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        return null;
    }

    static int parse(char[] s, int from, int to, ObjectBinder json) {
        int i = ++from;
        char current;
        outer:
        for (; i < to; i++) {
            current = s[i];

            if (current <= ' ')
                continue;

            if (current == '}')
                break;

            JsonItem key = new JsonItem();
            i = parseKey(key, s, i, to);

            i = skipSpaces(s, i, to);

            current = s[i];

//            if (current > 256)
//                i = parseValue2(json, s, i, s.length);
//            else
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
                    JsonItem holder = new JsonItem();
                    i = parseNumber(holder, s, i, to);
                    json.put(key.asString(), holder);
                    break;
                }
                case '{': {
                    ObjectBinder ob = json.getObjectBinder(key.asString());
                    i = JsonObject.parse(s, i, to, ob);
                    json.put(key.asString(), ob.getObject());
                    break;
                }
                case '[': {
                    ArrayBinder ob = json.getArrayBinder(key.asString());
                    i = JsonArray.parse(s, i, to, ob);
                    json.put(key.asString(), ob.getObject());
                    break;
                }
                case '}': {
                    break outer;
                }

                default: {
                    JsonItem holder = new JsonItem();
                    i = parseValue(holder, s, i, to, '}');
                    json.put(key.asString(), holder);
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

    public static String escape(String s) {
        Binder.StringBuilderAppender sb = new Binder.StringBuilderAppender();
        escape(s, sb);
        return sb.toString();
    }

    static void escape(String s, Binder.Appender sb) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < 256) {
                switch ((byte) ch) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '/':
                        sb.append("\\/");
                        break;
                    default:
                        //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                        if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                            String ss = Integer.toHexString(ch);
                            sb.append("\\u");
                            for (int k = 0; k < 4 - ss.length(); k++) {
                                sb.append('0');
                            }
                            sb.append(ss.toUpperCase());
                        } else {
                            sb.append(ch);
                        }
                }
            } else {
                sb.append(ch);
            }
        }//for
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toJson(sb);
        return sb.toString();
    }

    void toJson(StringBuilder sb) {
        sb.append('{');
        boolean comma = false;
        for (Map.Entry<String, JsonItem> entry : entrySet()) {
            if (comma)
                sb.append(',');
            comma = true;
            if (entry.getValue() == null)
                sb.append('"').append(entry.getKey()).append('"').append(":null");
            else {
                sb.append('"').append(entry.getKey()).append('"').append(':');
                entry.getValue().toJson(sb);
            }
        }
        sb.append('}');
    }

    public static String unescape(char[] s, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        byte ch, prev = 0;
        for (int i = from; i < to; i++) {
            ch = (byte) s[i];
            if (prev == '\\') {
                sb.append(s, from, i - from - 1);

                switch (ch) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'u':
                        if (to <= i + 5)
                            throw new IndexOutOfBoundsException("can't decode unicode character");
                        int hexVal = Integer.parseInt(new String(s, i + 1, 4), 16);
                        sb.append((char) hexVal);
                        i += 4;
                        break;
                }

                from = i + 1;
                prev = 0;
            } else
                prev = ch;
        }
        if (from < to) {
            sb.append(s, from, to - from);
        }
        return sb.toString();
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

}