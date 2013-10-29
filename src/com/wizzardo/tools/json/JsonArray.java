package com.wizzardo.tools.json;

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

        if ((s[from] == '"' && s[to - 1] == '"') || (s[from] == '\'' && s[to - 1] == '\'')) {
            from++;
            to--;
            String value = JsonObject.unescape(s, from, to);
            json.add(new JsonItem(value));
        } else if (to - from == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l') {
            json.add(new JsonItem(null));
        } else if (to - from == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e') {
            json.add(new JsonItem(true));
        } else if (to - from == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e') {
            json.add(new JsonItem(false));
        } else {
            String value = JsonObject.unescape(s, from, to);
            json.add(new JsonItem(value));
        }
    }

    static int parse(char[] s, int from, JsonArray json) {
        int i = ++from;
        boolean inString = false;
        char ch;
        char quote = 0;
        outer:
        while (i < s.length) {
            ch = s[i];
            if (inString) {
                if (ch == quote && s[i - 1] != '\\') {
                    inString = false;
                }
                i++;
                continue;
            }
            switch (ch) {
                case '"': {
                    inString = s[i - 1] != '\\';
                    quote = '"';
                    break;
                }
                case '\'': {
                    inString = s[i - 1] != '\\';
                    quote = '\'';
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
        toJson(sb);
        return sb.toString();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        toJson(sb);
        return sb.toString();
    }

    void toJson(StringBuilder sb) {
        sb.append('[');
        boolean comma = false;
        for (JsonItem item : this) {
            if (comma)
                sb.append(',');
            comma = true;
            item.toJson(sb);
        }
        sb.append(']');
    }

    public JsonArray append(Object ob) {
        if (ob instanceof JsonItem) {
            add((JsonItem) ob);
        } else
            add(new JsonItem(ob));
        return this;
    }
}