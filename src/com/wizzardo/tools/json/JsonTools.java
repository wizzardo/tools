package com.wizzardo.tools.json;

import com.wizzardo.tools.io.FileTools;
import com.wizzardo.tools.misc.SoftThreadLocal;
import com.wizzardo.tools.reflection.StringReflection;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author: wizzardo
 * Date: 8/11/14
 */
public class JsonTools {

    private static SoftThreadLocal<StringBuilder> stringBuilderThreadLocal = new SoftThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder init() {
            return new StringBuilder();
        }

        @Override
        public StringBuilder getValue() {
            StringBuilder sb = super.getValue();
            sb.setLength(0);
            return sb;
        }
    };

    public static JsonItem parse(File file) throws IOException {
        return parse(FileTools.text(file, FileTools.UTF_8));
    }

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
            offset = StringReflection.offset(s);
        return parse(data, offset, s.length(), generic);
    }

    public static JsonItem parse(char[] s) {
        return new JsonItem(parse(s, null, (Generic<Object>[]) null));
    }

    public static <T> T parse(char[] s, Class<T> clazz, Class... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, Class<T> clazz, Generic... generic) {
        return parse(s, 0, s.length, new Generic<T>(clazz, generic));
    }

    public static <T> T parse(char[] s, int from, int to, Generic<T> generic) {
        // check first char
        if (s[0] == '{') {
            JsonBinder binder = Binder.getObjectBinder(generic);
            JsonObject.parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        if (s[0] == '[') {
            JsonBinder binder = Binder.getArrayBinder(generic);
            JsonArray.parse(s, from, to, binder);
            return (T) binder.getObject();
        }
        return null;
    }

    public static String serialize(Object src) {
        Binder.StringBuilderAppender sb = new Binder.StringBuilderAppender(stringBuilderThreadLocal.getValue());
        Binder.toJSON(src, sb);
        return sb.toString();
    }

    public static void serialize(Object src, OutputStream out) {
        Binder.toJSON(src, new Binder.StreamAppender(out));
    }

    public static void serialize(Object src, StringBuilder out) {
        Binder.toJSON(src, new Binder.StringBuilderAppender(out));
    }

    public static String unescape(char[] s, int from, int to) {
        StringBuilder sb = new StringBuilder(to - from);
        byte ch, prev = 0;
        for (int i = from; i < to; i++) {
            ch = (byte) s[i];
            if (prev == '\\') {
                sb.append(s, from, i - from - 1);

                switch (ch) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case '/':
                        sb.append('/');
                        break;
                    case 'u':
                        if (to <= i + 5)
                            throw new IndexOutOfBoundsException("can't decode unicode character");
                        int hexVal = Integer.parseInt(new String(s, i + 1, 4), 16);
                        sb.append((char) hexVal);
                        i += 4;
                        break;
                }

                from = i + 1;
                prev = 0;
            } else
                prev = ch;
        }
        if (from < to) {
            sb.append(s, from, to - from);
        }
        return sb.toString();
    }


    public static String escape(String s) {
        Binder.StringBuilderAppender sb = new Binder.StringBuilderAppender();
        escape(s, sb);
        return sb.toString();
    }

    static void escape(String s, Binder.Appender sb) {
        int from = StringReflection.offset(s);
        int to = from + s.length();
        char[] chars = StringReflection.chars(s);
        for (int i = from; i < to; i++) {
            char ch = chars[i];
            if (ch < 127) {
                switch ((byte) ch) {
                    case '"':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('"');
                        break;
                    case '\\':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('\\');
                        break;
                    case '\b':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('b');
                        break;
                    case '\f':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('f');
                        break;
                    case '\n':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('n');
                        break;
                    case '\r':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('r');
                        break;
                    case '\t':
                        from = append(chars, from, i, sb);
                        sb.append('\\').append('t');
                        break;
//                    case '/':
//                        from = append(chars, from, i, sb);
////                      sb.append('\\').append('/');
//                        break;
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 11:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
//                    default:
                        //Reference: http://www.unicode.org/versions/Unicode5.1.0/
//                        if ((ch >= '\u0000' && ch <= '\u001F')) {
                        from = append(chars, from, i, sb);
                        appendUnicodeChar(ch, sb);
//                        }
                }
            } else if ((ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                from = append(chars, from, i, sb);
                appendUnicodeChar(ch, sb);
            }
        }//for
        if (from < to)
            append(chars, from, to, sb);
    }

    private static void appendUnicodeChar(char ch, Binder.Appender sb) {
        String ss = Integer.toHexString(ch);
        sb.append("\\u");
        for (int k = 0; k < 4 - ss.length(); k++) {
            sb.append('0');
        }
        sb.append(ss.toUpperCase());
    }

    private static int append(char[] s, int from, int to, Binder.Appender sb) {
        sb.append(s, from, to);
        return to + 1;
    }

}
