package org.bordl.json;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonObject extends LinkedHashMap<String, JsonItem> {

    public static JsonItem parse(String s) {
        // check first char
        s = s.trim();
        if (s.startsWith("{")) {
            JsonObject json = new JsonObject();
            parse(s.toCharArray(), 0, json);
            return new JsonItem(json);
        }
        if (s.startsWith("[")) {
            JsonArray json = new JsonArray();
            JsonArray.parse(s.toCharArray(), 0, json);
            return new JsonItem(json);
        }
        return null;
    }

    public JsonObject add(String key, Object value) {
        put(key, new JsonItem(value));
        return this;
    }

    static int parse(char[] s, int from, JsonObject json) {
        int i = from + 1;
        StringBuilder sb = new StringBuilder();
        String key = null, value;
        boolean inString = false;
        while (i < s.length && s[i] != '}') {
            switch (s[i]) {
                case ':': {
                    key = sb.toString().trim();
                    if (key.charAt(0) == '"' && key.charAt(sb.length() - 1) == '"') {
                        key = key.substring(1, key.length() - 1);
                    }
                    sb.setLength(0);
                    break;
                }
                case '"': {
                    inString = (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\\') || sb.toString().trim().length() == 0;
                    sb.append('"');
                    break;
                }
                case ',': {
                    if (inString) {
                        sb.append(',');
                        break;
                    }
                    if (sb.length() == 0) {
                        break;
                    }
                    value = sb.toString().trim();
                    sb.setLength(0);
                    if (value.equals("null")) {
                        json.put(key, null);
                        break;
                    }
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        json.put(key, new JsonItem(Boolean.valueOf(value)));
                        break;
                    }
                    if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                        value = value.substring(1, value.length() - 1);
                    }
                    json.put(key, new JsonItem(value));
                    break;
                }
                case '{': {
                    JsonObject obj = new JsonObject();
                    i = JsonObject.parse(s, i, obj);
                    json.put(key, new JsonItem(obj));
                    break;
                }
                case '[': {
                    JsonArray obj = new JsonArray();
                    i = JsonArray.parse(s, i, obj);
                    json.put(key, new JsonItem(obj));
                    break;
                }
                default: {
                    sb.append(s[i]);
                    break;
                }
            }
            i++;
        }
        if (key != null && sb.length() > 0) {
            if (sb.charAt(0) == '"' && sb.charAt(sb.length() - 1) == '"') {
                sb.setLength(sb.length() - 1);
                sb.deleteCharAt(0);
            }
            value = sb.toString();
            sb.setLength(0);
            json.put(key, new JsonItem(value));
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        sb.append('{');
        boolean comma = false;
        for (Map.Entry<String, JsonItem> entry : entrySet()) {
            if (comma)
                sb.append(',');
            comma = true;
            sb.append(entry.getKey()).append(':').append(entry.getValue().toJson());
        }
        sb.append('}');
    }
}