/*
 * $Id: JSONObject.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-10
 */
package org.bordl.json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bordl.json.parser.JSONParser;
import org.bordl.json.parser.ParseException;

/**
 * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONObject extends HashMap<String, JSONItem> implements JSONAware, JSONStreamAware {

    private static final long serialVersionUID = -503443796854799292L;

    public JSONObject put(String key, Object value) {
        put(key, new JSONItem(value));
        return this;
    }


    /**
     * Encode a map into JSON text and write it to out.
     * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
     * 
     * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
     * 
     * @param map
     * @param out
     */
    public static void writeJSONString(Map map, Writer out) throws IOException {
        if (map == null) {
            out.write("null");
            return;
        }

        boolean first = true;
        Iterator iter = map.entrySet().iterator();

        out.write('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(',');
            }
            Map.Entry entry = (Map.Entry) iter.next();
            out.write('\"');
            out.write(escape(String.valueOf(entry.getKey())));
            out.write('\"');
            out.write(':');
            JSONValue.writeJSONString(entry.getValue(), out);
        }
        out.write('}');
    }

    public void writeJSONString(Writer out) throws IOException {
        writeJSONString(this, out);
    }

    /**
     * Convert a map to JSON text. The result is a JSON object. 
     * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
     * 
     * @see org.json.simple.JSONValue#toJSONString(Object)
     * 
     * @param map
     * @return JSON text, or "null" if map is null.
     */
    public static String toJSONString(Map map) {
        if (map == null) {
            return "null";
        }

        StringBuffer sb = new StringBuffer();
        boolean first = true;
        Iterator iter = map.entrySet().iterator();

        sb.append('{');
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }

            Map.Entry entry = (Map.Entry) iter.next();
            toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
        }
        sb.append('}');
        return sb.toString();
    }

    public String toJSONString() {
        return toJSONString(this);
    }

    private static String toJSONString(String key, Object value, StringBuffer sb) {
        sb.append('\"');
        if (key == null) {
            sb.append("null");
        } else {
            JSONValue.escape(key, sb);
        }
        sb.append('\"').append(':');

        sb.append(JSONValue.toJSONString(value));

        return sb.toString();
    }

    public String toString() {
        return toJSONString();
    }

    public static String toString(String key, Object value) {
        StringBuffer sb = new StringBuffer();
        toJSONString(key, value, sb);
        return sb.toString();
    }

    /**
     * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
     * It's the same as JSONValue.escape() only for compatibility here.
     * 
     * @see org.json.simple.JSONValue#escape(String)
     * 
     * @param s
     * @return
     */
    public static String escape(String s) {
        return JSONValue.escape(s);
    }

    public String getString(String key) {
        return get(key).s();
    }

    public Long getLong(String key) {
        return get(key).l();
    }

    public Integer getInteger(String key) {
        return get(key).i();
    }

    public Double getDouble(String key) {
        return get(key).d();
    }

    public Float getFloat(String key) {
        return get(key).f();
    }

    public Boolean getBoolean(String key) {
        return get(key).b();
    }

    public JSONObject getJsonObject(String key) {
        return get(key).json();
    }

    public JSONArray getJsonArray(String key) {
        return get(key).array();
    }

    public String s(String key) {
        return getString(key);
    }

    public Long l(String key) {
        return getLong(key);
    }

    public Integer i(String key) {
        return getInteger(key);
    }

    public Double d(String key) {
        return getDouble(key);
    }

    public Boolean b(String key) {
        return getBoolean(key);
    }

    public Float f(String key) {
        return getFloat(key);
    }

    public JSONObject json(String key) {
        return getJsonObject(key);
    }

    public JSONArray array(String key) {
        return getJsonArray(key);
    }

    public static JSONObject parse(String json) throws ParseException {
        return (JSONObject) new JSONParser().parse(json);
    }

    public static JSONObject parse(Reader json) throws ParseException, IOException {
        return (JSONObject) new JSONParser().parse(json);
    }
}
