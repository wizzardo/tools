package com.wizzardo.tools.json;

/**
 * @author: moxa
 * Date: 12/26/12
 */
public class JsonItem {
    Object ob;

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

        if (ob instanceof Number) {
            return ((Number) ob).longValue();
        }
        try {
            Long l;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            if (s.startsWith("0x"))
                l = Long.parseLong(s.substring(2), 16);
            else
                l = Long.parseLong(s);
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

        if (ob instanceof Number)
            return ((Number) ob).intValue();

        try {
            Integer i;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            if (s.startsWith("0x"))
                i = (int) Long.parseLong(s.substring(2), 16);
            else
                i = Integer.parseInt(s);
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

        if (ob instanceof Number) {
            return ((Number) ob).byteValue();
        }
        try {
            Byte i;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            if (s.startsWith("0x"))
                i = (byte) Long.parseLong(s.substring(2), 16);
            else
                i = Byte.parseByte(s);
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

        if (ob instanceof Number) {
            return ((Number) ob).shortValue();
        }
        try {
            Short i;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            if (s.startsWith("0x"))
                i = (short) Long.parseLong(s.substring(2), 16);
            else
                i = Short.parseShort(s);
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

        if (ob instanceof Number) {
            return ((Number) ob).doubleValue();
        }
        try {
            Double d;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            d = Double.parseDouble(s);
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

        if (ob instanceof Number) {
            return ((Number) ob).floatValue();
        }
        try {
            Float f;
            String s = ob.toString();
            if (s.startsWith("+"))
                s = s.substring(1);
            f = Float.parseFloat(s);
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
    public static <T> T getAs(Object value, Class<T> clazz) {
        return new JsonItem(value).getAs(clazz);
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

    static JsonItem parse(char[] s, int from, int to) {
        if (from == to) {
            return null;
        }
        while ((from < to) && (s[from] <= ' ')) {
            from++;
        }
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        if (from == to) {
            return null;
        }

        JsonItem item;
        if ((s[from] == '"' && s[to - 1] == '"') || (s[from] == '\'' && s[to - 1] == '\'')) {
            from++;
            to--;
            return new JsonItem(JsonObject.unescape(s, from, to));
        } else if (to - from == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l') {
            return new JsonItem(null);
        } else if (to - from == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e') {
            return new JsonItem(true);
        } else if (to - from == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e') {
            return new JsonItem(false);
        } else if ((item = createInteger(s, from, to)) != null) {
            return item;
        } else {
            return new JsonItem(JsonObject.unescape(s, from, to));
        }
    }

    static JsonItem createInteger(char[] s, int from, int to) {
        if (to - from > 20) //9223372036854775807 - max long - 19 characters
            return null;

        boolean minus = s[from] == '-';
        if (minus)
            from++;

        if (from == to)
            return null;

        if (to - from > 10) {
            long l = 0;
            for (int i = from; i < to; i++) {
                if (s[i] < '0' || s[i] > '9')
                    return null;
                l = l * 10 + s[i] - 48;
            }
            return minus ? new JsonItem(-l) : new JsonItem(l);
        } else {
            int l = 0;
            for (int i = from; i < to; i++) {
                if (s[i] < '0' || s[i] > '9')
                    return null;
                l = l * 10 + s[i] - 48;
            }
            return minus ? new JsonItem(-l) : new JsonItem(l);
        }
    }

}
