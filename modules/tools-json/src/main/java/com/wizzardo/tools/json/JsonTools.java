package com.wizzardo.tools.json;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.interfaces.Supplier;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;
import com.wizzardo.tools.misc.UTF8;
import com.wizzardo.tools.reflection.StringReflection;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author: wizzardo
 * Date: 8/11/14
 */
public class JsonTools {
    public static final Charset UTF_8 = Charset.forName("utf-8");

    private static final int[] INT_VALUES = new int[128];
    private static final char[] UNESCAPES = new char[128];
    private static final char[] ESCAPES = new char[128];

    static {
        for (int i = 0; i < 32; i++) {
            ESCAPES[i] = 128;
        }

        ESCAPES['"'] = '"';
        ESCAPES['\\'] = '\\';
        ESCAPES['\n'] = 'n';
        ESCAPES['\r'] = 'r';
        ESCAPES['\b'] = 'b';
        ESCAPES['\t'] = 't';
        ESCAPES['\f'] = 'f';

        for (int i = 0; i < INT_VALUES.length; i++) {
            INT_VALUES[i] = 128;
        }
        INT_VALUES['0'] = 0;
        INT_VALUES['1'] = 1;
        INT_VALUES['2'] = 2;
        INT_VALUES['3'] = 3;
        INT_VALUES['4'] = 4;
        INT_VALUES['5'] = 5;
        INT_VALUES['6'] = 6;
        INT_VALUES['7'] = 7;
        INT_VALUES['8'] = 8;
        INT_VALUES['9'] = 9;

        INT_VALUES['a'] = 10;
        INT_VALUES['b'] = 11;
        INT_VALUES['c'] = 12;
        INT_VALUES['d'] = 13;
        INT_VALUES['e'] = 14;
        INT_VALUES['f'] = 15;

        INT_VALUES['A'] = 10;
        INT_VALUES['B'] = 11;
        INT_VALUES['C'] = 12;
        INT_VALUES['D'] = 13;
        INT_VALUES['E'] = 14;
        INT_VALUES['F'] = 15;


        UNESCAPES['"'] = '"';
        UNESCAPES['\\'] = '\\';
        UNESCAPES['b'] = '\b';
        UNESCAPES['f'] = '\f';
        UNESCAPES['n'] = '\n';
        UNESCAPES['r'] = '\r';
        UNESCAPES['t'] = '\t';
        UNESCAPES['/'] = '/';
        UNESCAPES['u'] = 128;
    }

    public static JsonItem parse(String s) {
        return new JsonItem(parse(s, (JsonGeneric<Object>) null));
    }

    public static <T> T parse(String s, Class<T> clazz) {
        return parse(s, Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz));
    }

    public static <T> T parse(String s, Class<T> clazz, Class... generic) {
        return parse(s, Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static <T> T parse(String s, Class<T> clazz, JsonGeneric... generic) {
        return parse(s, Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static JsonItem parse(byte[] bytes) {
        return parse(bytes, 0, bytes.length);
    }

    public static JsonItem parse(byte[] bytes, int from, int length) {
        return parse(new String(bytes, from, length, UTF_8));
    }

    public static <T> T parse(byte[] bytes, int from, int length, Class<T> clazz) {
        return parse(new String(bytes, from, length, UTF_8), Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz));
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz) {
        return parse(bytes, 0, bytes.length, clazz);
    }

    public static <T> T parse(byte[] bytes, int from, int length, Class<T> clazz, Class... generic) {
        return parse(new String(bytes, from, length, UTF_8), Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz, Class... generic) {
        return parse(bytes, 0, bytes.length, clazz, generic);
    }

    public static <T> T parse(byte[] bytes, int from, int length, Class<T> clazz, JsonGeneric... generic) {
        return parse(new String(bytes, from, length, UTF_8), Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static <T> T parse(byte[] bytes, Class<T> clazz, JsonGeneric... generic) {
        return parse(bytes, 0, bytes.length, clazz, generic);
    }

    public static <T> T parse(char[] s, Class<T> clazz, Class... generic) {
        return parse(s, 0, s.length, Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static <T> T parse(char[] s, Class<T> clazz, JsonGeneric... generic) {
        return parse(s, 0, s.length, Binder.DEFAULT_SERIALIZATION_CONTEXT.getGeneric(clazz, generic));
    }

    public static <T> T parse(String s, JsonGeneric<T> generic) {
        s = s.trim();
        char[] data = StringReflection.chars(s);
        int offset = 0;
        if (data.length != s.length())
            offset = StringReflection.offset(s); // for java 6
        return parse(data, offset, offset + s.length(), generic);
    }

    public static JsonItem parse(char[] s) {
        return new JsonItem(parse(s, 0, s.length, null));
    }

    public static <T> T parse(char[] s, int from, int to, JsonGeneric<T> generic) {
        // check first char
        if (s[from] == '{') {
            JsonBinder binder = Binder.getObjectBinder(generic);
            JsonObject.parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        if (s[from] == '[') {
            JsonBinder binder = Binder.getArrayBinder(generic);
            JsonArray.parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        return null;
    }

    /**
     * @return bytes array with UTF-8 json representation of the object
     */
    public static byte[] serializeToBytes(final Object src) {
        return serializeToBytes(src, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static void serialize(final Object src, final Supplier<byte[]> bytesSupplier, final UTF8.BytesConsumer bytesConsumer) {
        serialize(src, bytesSupplier, bytesConsumer, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static String serialize(final Object src) {
        return serialize(src, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static void serialize(Object src, ExceptionDrivenStringBuilder out) {
        serialize(src, out, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static void serialize(Object src, OutputStream out) {
        serialize(src, out, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static void serialize(Object src, Writer out) {
        serialize(src, out, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    public static void serialize(Object src, StringBuilder out) {
        serialize(src, out, Binder.DEFAULT_SERIALIZATION_CONTEXT);
    }

    /**
     * @return bytes array with UTF-8 json representation of the object
     */
    public static byte[] serializeToBytes(final Object src, final SerializationContext context) {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, byte[]>() {
            @Override
            public byte[] map(ExceptionDrivenStringBuilder builder) {
                serialize(src, builder, context);
                return builder.toBytes();
            }
        });
    }

    public static void serialize(final Object src, final Supplier<byte[]> bytesSupplier, final UTF8.BytesConsumer bytesConsumer, final SerializationContext context) {
        ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, Void>() {
            @Override
            public Void map(ExceptionDrivenStringBuilder builder) {
                serialize(src, builder, context);
                builder.toBytes(bytesSupplier, bytesConsumer);
                return null;
            }
        });
    }

    public static String serialize(final Object src, final SerializationContext context) {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder builder) {
                serialize(src, builder, context);
                return builder.toString();
            }
        });
    }

    public static void serialize(Object src, ExceptionDrivenStringBuilder out, SerializationContext context) {
        Binder.toJSON(src, Appender.create(out), context);
    }

    public static void serialize(Object src, OutputStream out, SerializationContext context) {
        Appender appender = Appender.create(out);
        Binder.toJSON(src, appender, context);
        appender.flush();
    }

    public static void serialize(Object src, Writer out, SerializationContext context) {
        Appender appender = Appender.create(out);
        Binder.toJSON(src, appender, context);
        appender.flush();
    }

    public static void serialize(Object src, StringBuilder out, SerializationContext context) {
        Binder.toJSON(src, Appender.create(out), context);
    }

    public static String unescape(final char[] chars, final int offset, final int to) {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder sb) {
                char ch;
                int i = offset;
                int from = offset;

                while (i < to) {
                    ch = chars[i];
                    if (ch == '\\') {
                        sb.append(chars, from, i - from);
                        i++;
                        if (to <= i)
                            throw new IndexOutOfBoundsException("unexpected end");

                        ch = UNESCAPES[chars[i]];
                        if (ch == 0) {
                            throw new IllegalStateException("unexpected escaped char: " + chars[i]);
                        } else if (ch == 128) {
                            if (to < i + 5)
                                throw new IndexOutOfBoundsException("can't decode unicode character");
                            i += 4;
                            sb.append(decodeUtf(chars, i));
                        } else {
                            sb.append(ch);
                        }
                        from = i + 1;
                    }
                    i++;
                }

                if (from < to) {
                    sb.append(chars, from, to - from);
                }
                return sb.toString();
            }
        });
    }

    public static String escape(String s) {
        Appender sb = Appender.create();
        escape(s, sb);
        return sb.toString();
    }

    static void escape(String s, Appender sb) {
        char[] chars = StringReflection.chars(s);
        int to = s.length();
        int offset = chars.length == to ? 0 : StringReflection.offset(s);
        int from = offset;
        to += from;
        int l = to - 1;
        for (int i = from; i < l; i += 2) {
            from = check(from, i, chars, sb);
            from = check(from, i + 1, chars, sb);  //about 10% faster
        }
        if ((l + offset) % 2 == 0)
            from = check(from, l, chars, sb);

        if (from < to)
            append(chars, from, to, sb);
    }

    private static int check(int from, int i, char[] chars, Appender sb) {
        char ch = chars[i];
        if (ch < 127) {
            if (ESCAPES[ch] != 0) {
                from = append(chars, from, i, sb);
                escapeChar(ESCAPES[ch], ch, sb);
            }
        } else if ((ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
            from = append(chars, from, i, sb);
            appendUnicodeChar(ch, sb);
        }
        return from;
    }

    static void escape(char ch, Appender sb) {
        if (ch < 127) {
            if (ESCAPES[ch] != 0)
                escapeChar(ESCAPES[ch], ch, sb);
            else
                sb.append(ch);
        } else if ((ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF'))
            appendUnicodeChar(ch, sb);
        else
            sb.append(ch);
    }

    private static void appendUnicodeChar(char ch, Appender sb) {
        String ss = Integer.toHexString(ch);
        sb.append("\\u");
        for (int k = 0; k < 4 - ss.length(); k++) {
            sb.append('0');
        }
        sb.append(ss.toUpperCase());
    }

    private static int append(char[] s, int from, int to, Appender sb) {
        sb.append(s, from, to);
        return to + 1;
    }

    private static void escapeChar(char escaped, char src, Appender sb) {
        if (escaped == 128) {
            appendUnicodeChar(src, sb);
        } else {
            sb.append('\\');
            sb.append(escaped);
        }
    }

    static char decodeUtf(char[] chars, int last) {
        int value = 0;
        value += getHexValue(chars[last]);
        value += getHexValue(chars[last - 1]) * 16;
        value += getHexValue(chars[last - 2]) * 256;
        value += getHexValue(chars[last - 3]) * 4096;
        return (char) value;
    }

    static int getHexValue(int c) {
        if (c >= 128)
            throw new IllegalStateException("unexpected char for hex value: " + (char) c);

        c = INT_VALUES[c];
        if (c == 128)
            throw new IllegalStateException("unexpected char for hex value");

        return c;
    }
}
