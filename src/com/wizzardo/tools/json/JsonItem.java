package com.wizzardo.tools.json;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonItem {
    private Object ob;

    public JsonItem(Object ob) {
        this.ob = ob;
    }

    public boolean isNull() {
        return ob == null;
    }

    public String asString() {
        return String.valueOf(ob);
    }

    public Object get() {
        return ob;
    }

    public Long asLong() {
        return asLong(null);
    }

    public Long asLong(Long def) {
        if (ob == null)
            return def;

        if (ob instanceof Long) {
            return (Long) ob;
        }
        try {
            Long l = Long.parseLong(ob.toString());
            ob = l;
            return l;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Integer asInteger() {
        return asInteger(null);
    }

    public Integer asInteger(Integer def) {
        if (ob == null)
            return def;

        if (ob instanceof Integer)
            return (Integer) ob;

        try {
            Integer i = Integer.parseInt(ob.toString());
            ob = i;
            return i;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Byte asByte() {
        return asByte(null);
    }

    public Byte asByte(Byte def) {
        if (ob == null)
            return def;

        if (ob instanceof Byte) {
            return (Byte) ob;
        }
        try {
            Byte i = Byte.parseByte(ob.toString());
            ob = i;
            return i;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Short asShort() {
        return asShort(null);
    }

    public Short asShort(Short def) {
        if (ob == null)
            return def;

        if (ob instanceof Short) {
            return (Short) ob;
        }
        try {
            Short i = Short.parseShort(ob.toString());
            ob = i;
            return i;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Double asDouble() {
        return asDouble(null);
    }

    public Double asDouble(Double def) {
        if (ob == null)
            return def;

        if (ob instanceof Double) {
            return (Double) ob;
        }
        try {
            Double d = Double.parseDouble(ob.toString());
            ob = d;
            return d;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Float asFloat() {
        return asFloat(null);
    }

    public Float asFloat(Float def) {
        if (ob == null)
            return def;

        if (ob instanceof Float) {
            return (Float) ob;
        }
        try {
            Float f = Float.parseFloat(ob.toString());
            ob = f;
            return f;
        } catch (NumberFormatException ex) {
        }
        return def;
    }

    public Boolean asBoolean() {
        return asBoolean(null);
    }

    public Boolean asBoolean(Boolean def) {
        if (ob == null)
            return def;

        if (ob instanceof Boolean) {
            return (Boolean) ob;
        }
        if (ob instanceof String) {
            Boolean b = Boolean.parseBoolean(ob.toString());
            ob = b;
            return b;
        }
        return def;
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

    @SuppressWarnings("unchecked")
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
        } else if (Byte.class == clazz || byte.class == clazz) {
            return (T) asByte();
        } else if (Short.class == clazz || short.class == clazz) {
            return (T) asShort();
        }
        return null;
    }

    public String toString() {
        return toJson();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        toJson(sb);
        return sb.toString();
    }

    void toJson(StringBuilder sb) {
        if (ob instanceof JsonObject) {
            ((JsonObject) ob).toJson(sb);
        } else if (ob instanceof JsonArray) {
            ((JsonArray) ob).toJson(sb);
        } else if (ob.getClass() == String.class) {
            sb.append('"').append(JsonObject.escape(ob.toString())).append('"');
        } else
            sb.append(ob);
    }
}
