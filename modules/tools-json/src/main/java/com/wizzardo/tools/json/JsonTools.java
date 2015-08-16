package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.Consumer;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;
import com.wizzardo.tools.misc.Supplier;
import com.wizzardo.tools.misc.pool.*;
import com.wizzardo.tools.reflection.StringReflection;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author: wizzardo
 * Date: 8/11/14
 */
public class JsonTools {

    private static final char[] INT_VALUES = new char[128];
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

    static Pool<ExceptionDrivenStringBuilder> builderPool = new PoolBuilder<ExceptionDrivenStringBuilder>()
            .supplier(new Supplier<ExceptionDrivenStringBuilder>() {
                @Override
                public ExceptionDrivenStringBuilder supply() {
                    return new ExceptionDrivenStringBuilder();
                }
            }).resetter(new Consumer<ExceptionDrivenStringBuilder>() {
                @Override
                public void consume(ExceptionDrivenStringBuilder sb) {
                    sb.setLength(0);
                }
            }).build();

    public static JsonItem parse(String s) {
        return new JsonItem(parse(s, (Generic<Object>) null));
    }

    public static <T> T parse(String s, Class<T> clazz) {
        return parse(s, new Generic<T>(clazz));
    }

    public static <T> T parse(String s, Class<T> clazz, Class... generic) {
        return parse(s, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(String s, Class<T> clazz, Generic... generic) {
        return parse(s, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(String s, Generic<T> generic) {
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

    public static <T> T parse(char[] s, Class<T> clazz, Class... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, Class<T> clazz, Generic... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, int from, int to, Generic<T> generic) {
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
    public byte[] serializeToBytes(Object src) {
        Holder<ExceptionDrivenStringBuilder> holder = builderPool.holder();
        try {
            ExceptionDrivenStringBuilder builder = holder.get();
            Binder.toJSON(src, Appender.create(builder));
            return builder.toUtf8Bytes();
        } finally {
            holder.close();
        }
    }

    public static String serialize(Object src) {
        Holder<ExceptionDrivenStringBuilder> holder = builderPool.holder();
        try {
            ExceptionDrivenStringBuilder builder = holder.get();
            Appender sb = Appender.create(builder);
            Binder.toJSON(src, sb);
            return sb.toString();
        } finally {
            holder.close();
        }
    }

    public static void serialize(Object src, OutputStream out) {
        Appender appender = Appender.create(out);
        Binder.toJSON(src, appender);
        appender.flush();
    }

    public static void serialize(Object src, Writer out) {
        Appender appender = Appender.create(out);
        Binder.toJSON(src, appender);
        appender.flush();
    }

    public static void serialize(Object src, StringBuilder out) {
        Binder.toJSON(src, Appender.create(out));
    }

    public static String unescape(char[] chars, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        char ch;
        int i = from;
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
        char value = 0;
        value += getHexValue(chars[last]);
        value += getHexValue(chars[last - 1]) * 16;
        value += getHexValue(chars[last - 2]) * 256;
        value += getHexValue(chars[last - 3]) * 4096;
        return value;
    }

    static char getHexValue(char c) {
        if (c >= 128)
            throw new IllegalStateException("unexpected char for hex value: " + c);

        c = INT_VALUES[c];
        if (c == 128)
            throw new IllegalStateException("unexpected char for hex value");

        return c;
    }
}
