package org.bordl.json;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonItem {
    private Object ob;

    public JsonItem(Object ob) {
        this.ob = ob;
    }

    public String asString() {
        return ob.toString();
    }

    public Object get() {
        return ob;
    }

    public Long asLong() {
        if (ob instanceof Long) {
            return (Long) ob;
        }
        try {
            Long l = Long.parseLong(ob.toString());
            ob = l;
            return l;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Integer asInteger() {
        if (ob instanceof Integer) {
            return (Integer) ob;
        }
        try {
            Integer i = Integer.parseInt(ob.toString());
            ob = i;
            return i;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Double asDouble() {
        if (ob instanceof Double) {
            return (Double) ob;
        }
        try {
            Double d = Double.parseDouble(ob.toString());
            ob = d;
            return d;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Float asFloat() {
        if (ob instanceof Float) {
            return (Float) ob;
        }
        try {
            Float f = Float.parseFloat(ob.toString());
            ob = f;
            return f;
        } catch (NumberFormatException ex) {
        }
        return null;
    }

    public Boolean asBoolean() {
        if (ob instanceof Boolean) {
            return (Boolean) ob;
        }
        if (ob instanceof String) {
            Boolean b = Boolean.parseBoolean(ob.toString());
            ob = b;
            return b;
        }
        return null;
    }

    public JsonObject asJsonObject() {
        if (ob instanceof JsonObject) {
            return (JsonObject) ob;
        }
        return null;
    }

    public JsonArray asJsonArray() {
        if (ob instanceof JsonArray) {
            return (JsonArray) ob;
        }
        return null;
    }

    public <T> T getAs(Class<T> clazz) {
        if (String.class == clazz) {
            return (T) asString();
        } else if (Integer.class == clazz || int.class == clazz) {
            return (T) asInteger();
        } else if (Double.class == clazz || double.class == clazz) {
            return (T) asDouble();
        } else if (Long.class == clazz || long.class == clazz) {
            return (T) asLong();
        } else if (Boolean.class == clazz || boolean.class == clazz) {
            return (T) asBoolean();
        } else if (Float.class == clazz || float.class == clazz) {
            return (T) asFloat();
        }
        return null;
    }

    public String toString() {
        return String.valueOf(ob);
    }

    public String toJson() {
        if (ob instanceof JsonObject) {
            return ob.toString();
        }
        if (ob instanceof JsonArray) {
            return ob.toString();
        }
        if (ob.getClass() == String.class) {
            return "\"" + JsonObject.escape(ob.toString()) + "\"";
        }
        return String.valueOf(ob);
    }


    public void toJson(StringBuilder sb) {
        if (ob instanceof JsonObject) {
            ((JsonObject) ob).toString(sb);
        } else if (ob instanceof JsonArray) {
            ((JsonArray) ob).toString(sb);
        } else if (ob.getClass() == String.class) {
            sb.append('"').append(JsonObject.escape(ob.toString())).append('"');
        } else
            sb.append(String.valueOf(ob));
    }
}
