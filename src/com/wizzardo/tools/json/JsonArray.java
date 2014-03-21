package com.wizzardo.tools.json;

import java.util.ArrayList;

import static com.wizzardo.tools.json.JsonUtils.*;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonArray extends ArrayList<JsonItem> {

    static int parse(char[] s, int from, ArrayBinder json) {
        int i = ++from;
        char current;
        outer:
        for (; i < s.length; i++) {
//            i = skipSpaces(s, i);

            current = s[i];

            if (current <= ' ')
                continue;

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
                    i = parseNumber(holder, s, i, s.length);
                    json.add(holder);
                    break;
                }
                case '{': {
                    ObjectBinder ob = json.getObjectBinder();
                    i = JsonObject.parse(s, i, ob);
                    json.add(ob.getObject());
                    break;
                }
                case '[': {
                    ArrayBinder ob = json.getArrayBinder();
                    i = parse(s, i, ob);
                    json.add(ob.getObject());
                    break;
                }
                case ']': {
                    break outer;
                }
                default: {
                    JsonItem holder = new JsonItem();
                    i = parseValue(holder, s, i, s.length, ']');
                    json.add(holder);
                }
            }


            while ((current = s[i]) <= ' ') {
                i++;
            }

            if (current == ',')
                continue;

            if (current == ']')
                break;

            throw new IllegalStateException("here must be ',' or ']' , but found: " + current);

        }
        return i + 1;
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