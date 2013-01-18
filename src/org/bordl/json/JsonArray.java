package org.bordl.json;

import java.util.ArrayList;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonArray extends ArrayList<JsonItem> {

    private static void parseValue(JsonArray json, char[] s, int from, int to) {
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
        String value = new String(s, from, to - from);

        if (s[from] == '"' && s[to - 1] == '"') {
            value = value.substring(1, value.length() - 1);
            json.add(new JsonItem(value));
        } else if (value.equals("null")) {
            json.add(null);
        } else if (value.equals("true")) {
            json.add(new JsonItem(true));
        } else if (value.equals("false")) {
            json.add(new JsonItem(false));
        } else {
            json.add(new JsonItem(value));
        }
    }

    static int parse(char[] s, int from, JsonArray json) {
        int i = ++from;
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
                    inString=s[i - 1] != '\\';
                    break;
                }
                case ',': {
                    parseValue(json, s, from, i);
                    from = i + 1;
                    break;
                }
                case '{': {
                    JsonObject obj = new JsonObject();
                    i = JsonObject.parse(s, i, obj);
                    from = i + 1;
                    json.add(new JsonItem(obj));
                    break;
                }
                case '[': {
                    JsonArray obj = new JsonArray();
                    i = JsonArray.parse(s, i, obj);
                    from = i + 1;
                    json.add(new JsonItem(obj));
                    break;
                }
                case ']': {
                    break outer;
                }
            }
            i++;
        }
        if (from != i) {
            parseValue(json, s, from, i);
        }
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