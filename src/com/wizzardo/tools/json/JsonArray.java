package com.wizzardo.tools.json;

import java.util.ArrayList;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonArray extends ArrayList<JsonItem> {

    private static void parseValue(ArrayBinder json, char[] s, int from, int to) {
        JsonItem item = JsonItem.parse(s, from, to);
        if (item != null)
            json.add(item);
    }

    static int parse(char[] s, int from, ArrayBinder json) {
        int i = ++from;
        boolean inString = false;
        byte ch;
        char quote = 0;
        outer:
        while (i < s.length) {
            ch = (byte) s[i];
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
                    ObjectBinder ob = json.getObjectBinder();
                    i = JsonObject.parse(s, i, ob);
                    from = i + 1;
                    json.add(ob.getObject());
                    break;
                }
                case '[': {
                    ArrayBinder ob = json.getArrayBinder();
                    i = JsonArray.parse(s, i, ob);
                    from = i + 1;
                    json.add(ob.getObject());
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