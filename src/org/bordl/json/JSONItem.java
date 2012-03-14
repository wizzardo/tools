/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.json;

/**
 *
 * @author Moxa
 */
public class JSONItem {

    private Object ob;

    public JSONItem() {
    }

    public JSONItem(Object ob) {
        this.ob = ob;
    }

    @Override
    public String toString() {
        return ob.toString();
    }

    public String getAsString() {
        return ob.toString();
    }

    public Long getAsLong() {
        if (ob instanceof Long) {
            return (Long) ob;
        }
        try {
            return Long.parseLong(ob.toString());
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Integer getAsInteger() {
        if (ob instanceof Integer) {
            return (Integer) ob;
        }
        try {
            return Integer.parseInt(ob.toString());
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Double getAsDouble() {
        if (ob instanceof Double) {
            return (Double) ob;
        }
        try {
            return Double.parseDouble(ob.toString());
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Float getAsFloat() {
        if (ob instanceof Float) {
            return (Float) ob;
        }
        try {
            return Float.parseFloat(ob.toString());
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Boolean getAsBoolean() {
        if (ob instanceof Boolean) {
            return (Boolean) ob;
        }
        if (ob instanceof String) {
            return Boolean.parseBoolean(ob.toString());
        }
        return null;
    }

    public JSONObject getAsJsonObject() {
        if (ob instanceof JSONObject) {
            return (JSONObject) ob;
        }
        return null;
    }

    public JSONArray getAsJsonArray() {
        if (ob instanceof JSONArray) {
            return (JSONArray) ob;
        }
        return null;
    }

    public String s() {
        return getAsString();
    }

    public Long l() {
        return getAsLong();
    }

    public Integer i() {
        return getAsInteger();
    }

    public Double d() {
        return getAsDouble();
    }

    public Boolean b() {
        return getAsBoolean();
    }

    public Float f() {
        return getAsFloat();
    }

    public JSONObject json() {
        return getAsJsonObject();
    }

    public JSONArray array() {
        return getAsJsonArray();
    }
}
