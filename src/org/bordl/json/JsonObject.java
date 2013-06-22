package org.bordl.json;


import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

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
        s = s.trim();
        return parse(s.toCharArray());
    }

    public static JsonItem parse(char[] s) {
        // check first char
        if (s[0] == '{') {
            JsonObject json = new JsonObject();
            parse(s, 0, json);
            return new JsonItem(json);
        }
        if (s[0] == '[') {
            JsonArray json = new JsonArray();
            JsonArray.parse(s, 0, json);
            return new JsonItem(json);
        }
        return null;
    }

    public JsonObject add(String key, Object value) {
        put(key, new JsonItem(value));
        return this;
    }

    private static String parseKey(char[] s, int from, int to) {
        while ((from < to) && (s[from] <= ' ')) {
            from++;
        }
        while ((from < to) && (s[to] <= ' ')) {
            to--;
        }
        String key = new String(s, from, to - from);
        if (key.charAt(0) == '"' && key.charAt(key.length() - 1) == '"') {
            key = key.substring(1, key.length() - 1);
        }
        return key;
    }

    private static void parseValue(JsonObject json, String key, char[] s, int from, int to) {
        if (from == to) {
            return;
        }
        while ((from < to) && (s[from] <= ' ')) {
            from++;
        }
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        if (from == to) {
            return;
        }

        if (s[from] == '"' && s[to - 1] == '"') {
            from++;
            to--;
            String value = JsonObject.unescape(s, from, to);
            json.put(key, new JsonItem(value));
        } else if (to - from == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l') {
            json.put(key, new JsonItem(null));
        } else if (to - from == 4 && s[from] == 'e' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e') {
            json.put(key, new JsonItem(true));
        } else if (to - from == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e') {
            json.put(key, new JsonItem(false));
        } else {
            String value = JsonObject.unescape(s, from, to);
            json.put(key, new JsonItem(value));
        }
    }

    static int parse(char[] s, int from, JsonObject json) {
        int i = ++from;
        String key = null;
        boolean inString = false;
        char ch;
        outer:
        while (i < s.length) {
            ch = s[i];
            if (inString) {
                if (ch == '"' && s[i - 1] != '\\') {
                    inString = false;
                }
                i++;
                continue;
            }
            switch (ch) {
                case '"': {
                    inString = s[i - 1] != '\\';
                    break;
                }
                case ':': {
                    key = parseKey(s, from, i);
                    from = i + 1;
                    break;
                }
                case ',': {
                    parseValue(json, key, s, from, i);
                    from = i + 1;
                    break;
                }
                case '{': {
                    JsonObject obj = new JsonObject();
                    i = JsonObject.parse(s, i, obj);
                    from = i + 1;
                    json.put(key, new JsonItem(obj));
                    break;
                }
                case '}': {
                    break outer;
                }
                case '[': {
                    JsonArray obj = new JsonArray();
                    i = JsonArray.parse(s, i, obj);
                    from = i + 1;
                    json.put(key, new JsonItem(obj));
                    break;
                }
            }
            i++;
        }

        if (key != null && from != i) {
            parseValue(json, key, s, from, i);
        }
        return i;
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        escape(s, sb);
        return sb.toString();
    }

    public static void escape(String s, StringBuilder sb) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
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
        }//for
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toJson(sb);
        return sb.toString();
    }

    public String toJson() {
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
                sb.append(entry.getKey()).append(":null");
            else {
                sb.append(entry.getKey()).append(':');
                entry.getValue().toJson(sb);
            }
        }
        sb.append('}');
    }

    public static String unescape(char[] s, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        char ch, prev = 0;
        for (int i = from; i < to; i++) {
            ch = s[i];
            if (prev == '\\') {
                sb.append(s, from, i - from);

                switch (ch) {
                    case '"':
                        sb.setCharAt(sb.length() - 1, '"');
                        break;
                    case '\\':
                        sb.setCharAt(sb.length() - 1, '\\');
                        break;
                    case 'b':
                        sb.setCharAt(sb.length() - 1, '\b');
                        break;
                    case 'f':
                        sb.setCharAt(sb.length() - 1, '\f');
                        break;
                    case 'n':
                        sb.setCharAt(sb.length() - 1, '\n');
                        break;
                    case 'r':
                        sb.setCharAt(sb.length() - 1, '\r');
                        break;
                    case 't':
                        sb.setCharAt(sb.length() - 1, '\t');
                        break;
                    case '/':
                        sb.setCharAt(sb.length() - 1, '/');
                        break;
                }

                from = i + 1;
            }
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

    public static <T> T bind(String json, Class<T> clazz) {
        return Binder.fromJSON(clazz, parse(json).asJsonObject());
    }

    public JsonObject append(String key, Object ob) {
        if (ob instanceof JsonItem) {
            put(key, (JsonItem) ob);
        } else
            put(key, new JsonItem(ob));
        return this;
    }

}