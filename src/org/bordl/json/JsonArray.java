package org.bordl.json;

import java.util.ArrayList;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonArray extends ArrayList<JsonItem> {

    static int parse(char[] s, int from, JsonArray json) {
        int i = from + 1;
        StringBuilder sb = new StringBuilder();
        String value;
        while (i < s.length && s[i] != ']') {
            switch (s[i]) {
                case ',': {
                    if (sb.length() == 0) {
                        break;
                    }
                    value = sb.toString().trim();
                    sb.setLength(0);
                    if (value.equals("null")) {
                        json.add(null);
                        break;
                    }
                    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                        json.add(new JsonItem(Boolean.valueOf(value)));
                        break;
                    }
                    if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                        value = value.substring(1, value.length() - 1);
                    }
                    json.add(new JsonItem(value));
                    break;
                }
                case '{': {
                    JsonObject obj = new JsonObject();
                    i = JsonObject.parse(s, i, obj);
                    json.add(new JsonItem(obj));
                    break;
                }
                default: {
                    sb.append(s[i]);
                    break;
                }
            }
            i++;
        }
        if (sb.length() > 0) {
            if (sb.charAt(0) == '"' && sb.charAt(sb.length() - 1) == '"') {
                sb.setLength(sb.length() - 1);
                sb.deleteCharAt(0);
            }
            value = sb.toString();
            sb.setLength(0);
            json.add(new JsonItem(value));
        }
//            System.out.println(s[i]);
//            System.out.println(new String(s).substring(0,i));
        return i;
    }

    public void put(Object ob) {
        add(new JsonItem(ob));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public String toString(StringBuilder sb) {
        sb.append('[');
        boolean comma = false;
        for (JsonItem item : this) {
            if (comma)
                sb.append(',');
            comma = true;
            item.toJson(sb);
        }
        sb.append(']');
        return sb.toString();
    }
}