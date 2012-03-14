/*
 * $Id: JSONArray.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.bordl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.bordl.json.parser.JSONParser;
import org.bordl.json.parser.ParseException;

/**
 * A JSON array. JSONObject supports java.util.List interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONArray extends LinkedList<JSONItem> implements JSONAware, JSONStreamAware {

    private static final long serialVersionUID = 3957988303675231981L;

    /**
     * Encode a list into JSON text and write it to out. 
     * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
     * 
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     * 
     * @param list
     * @param out
     */
    public static void writeJSONString(List list, Writer out) throws IOException {
        if (list == null) {
            out.write("null");
            return;
        }

        boolean first = true;
        Iterator iter = list.iterator();

        out.write('[');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(',');
            }

            Object value = iter.next();
            if (value == null) {
                out.write("null");
                continue;
            }

            JSONValue.writeJSONString(value, out);
        }
        out.write(']');
    }

    public JSONArray append(Object ob) {
        add(new JSONItem(ob));
        return this;
    }

    public void writeJSONString(Writer out) throws IOException {
        writeJSONString((List) this, out);
    }

    /**
     * Convert a list to JSON text. The result is a JSON array. 
     * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
     * 
     * @see org.json.simple.JSONValue#toJSONString(Object)
     * 
     * @param list
     * @return JSON text, or "null" if list is null.
     */
    public static String toJSONString(List list) {
        if (list == null) {
            return "null";
        }

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        Iterator iter = list.iterator();

        sb.append('[');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }

            Object value = iter.next();
            if (value == null) {
                sb.append("null");
                continue;
            }
            sb.append(JSONValue.toJSONString(value));
        }
        sb.append(']');
        return sb.toString();
    }

    public String toJSONString() {
        return toJSONString((List) this);
    }

    public String toString() {
        return toJSONString();
    }

    public static JSONArray parse(String json) throws ParseException {
        return (JSONArray) new JSONParser().parse(json);
    }

    public static JSONArray parse(Reader json) throws ParseException, IOException {
        return (JSONArray) new JSONParser().parse(json);
    }

    public String getString(int key) {
        return get(key).s();
    }

    public Long getLong(int key) {
        return get(key).l();
    }

    public Integer getInteger(int key) {
        return get(key).i();
    }

    public Double getDouble(int key) {
        return get(key).d();
    }

    public Float getFloat(int key) {
        return get(key).f();
    }

    public Boolean getBoolean(int key) {
        return get(key).b();
    }

    public JSONObject getJsonObject(int key) {
        return get(key).json();
    }

    public JSONArray getJsonArray(int key) {
        return get(key).array();
    }

    public String s(int key) {
        return getString(key);
    }

    public Long l(int key) {
        return getLong(key);
    }

    public Integer i(int key) {
        return getInteger(key);
    }

    public Double d(int key) {
        return getDouble(key);
    }

    public Boolean b(int key) {
        return getBoolean(key);
    }

    public Float f(int key) {
        return getFloat(key);
    }

    public JSONObject json(int key) {
        return getJsonObject(key);
    }

    public JSONArray array(int key) {
        return getJsonArray(key);
    }
}
