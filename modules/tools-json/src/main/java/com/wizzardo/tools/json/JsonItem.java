package com.wizzardo.tools.json;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;
import com.wizzardo.tools.misc.StringConverter;
import com.wizzardo.tools.misc.TextTools;

import java.util.Date;

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

    public Character asChar() {
        return asChar(null);
    }

    public Character asChar(Character def) {
        if (ob == null)
            return def;

        if (ob.getClass() == Character.class)
            return (Character) ob;

        if (ob instanceof Number)
            return (char) ((Number) ob).intValue();

        String s = ob.toString();
        Character ch;
        if (s.length() > 1) {
            int i = asInteger(-1);
            if (i == -1)
                return def;
            ch = (char) i;
        } else
            ch = s.charAt(0);
        ob = ch;
        return ch;
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

    public <T extends Enum<T>> T asEnum(Class<T> cl) {
        return TextTools.asEnum(cl, asString());
    }

    public JsonObject asJsonObject() {
        return (JsonObject) ob;
    }

    public JsonArray asJsonArray() {
        return (JsonArray) ob;
    }

    public boolean isJsonArray() {
        return ob instanceof JsonArray;
    }

    public boolean isJsonObject() {
        return ob instanceof JsonObject;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAs(Object value, Class<T> clazz) {
        if (clazz.isAssignableFrom(value.getClass()))
            return (T) value;

        return new JsonItem(value).getAs(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Class<T> clazz) {
        if (ob == null)
            return null;

        if (clazz.isAssignableFrom(ob.getClass())) {
            return (T) ob;
        } else if (String.class == clazz) {
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
        } else if (Character.class == clazz || char.class == clazz) {
            return (T) asChar();
        } else if (Date.class == clazz) {
            return (T) StringConverter.toDate(asString());
        }
        return null;
    }

    public String toString() {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder builder) {
                Appender sb = Appender.create(builder);
                toJson(sb);
                return sb.toString();
            }
        });
    }

    void toJson(Appender sb) {
        if (ob == null)
            sb.append("null");
        else if (ob instanceof JsonObject)
            ((JsonObject) ob).toJson(sb);
        else if (ob instanceof JsonArray)
            ((JsonArray) ob).toJson(sb);
        else if (ob.getClass() == String.class) {
            sb.append('"');
            JsonTools.escape(ob.toString(), sb);
            sb.append('"');
        } else
            Binder.toJSON(ob, sb);
    }

    protected void set(Object value) {
        ob = value;
    }
}
