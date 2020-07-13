package com.wizzardo.tools.yaml;

import com.wizzardo.tools.reflection.StringReflection;

public class YamlTools {
    private static final char[] UNESCAPES = new char[128];
    private static final char[] ESCAPES = new char[128];
    private static final int[] INT_VALUES = new int[128];

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

    public static YamlItem parse(String s) {
        char[] data = StringReflection.chars(s);
        int offset = 0;
        if (data.length != s.length())
            offset = StringReflection.offset(s); // for java 6
        return parse(data, offset, offset + s.length());
    }

    public static YamlItem parse(char[] data, int from, int to) {
        int indent = getIndent(data, from, to);
        return parse(data, from, to, indent);
    }

    public static YamlItem parse(char[] data, int from, int to, int indent) {
        if (isArray(data, from + indent))
            return null; //todo

        YamlObject object = new YamlObject();
        parse(data, from, to, indent, object);
        return new YamlItem(object);
    }

    public static int parse(char[] data, int from, int to, int indent, YamlObject into) {
        do {
            from += indent;
            int lineEnd = skipUntilLineEnd(data, from, to);
            int commentStart = find(data, from, lineEnd, '#');
            int kvEnd = commentStart == -1 ? lineEnd : commentStart;
            int kvSeparator = find(data, from, kvEnd, ':');

            if (kvSeparator == -1 && skipSpaces(data, from, kvEnd) == kvEnd) {
                from = skipUntilNextLine(data, lineEnd, to);

                int nextIndent = getIndent(data, from, to);
                if (nextIndent == indent) {
                    continue;
                } else if (nextIndent > indent) {
                    throw new IllegalStateException();
                } else {
                    return from;
                }
            }

            String key = readKey(data, from, kvSeparator);
            String value = readValue(data, kvSeparator + 1, kvEnd);

            from = skipUntilNextLine(data, lineEnd, to);
            int nextIndent = getIndent(data, from, to);
            if (nextIndent == indent) {
                into.put(key, new YamlItem(value));
            } else if (nextIndent > indent) {
                if (isArray(data, from + nextIndent)) {
                    //todo
                } else {
                    YamlObject next = new YamlObject();
                    from = parse(data, from, to, nextIndent, next);
                    into.put(key, new YamlItem(next));

                    nextIndent = getIndent(data, from, to);
                    if (nextIndent < indent)
                        return from;
                }
            } else {
                into.put(key, new YamlItem(value));
                return from;
            }
        } while (from < to);
        return from;
    }

    static boolean isArray(char[] s, int i) {
        return s[i] == '-';
    }

    static String readKey(char[] s, int from, int to) {
        to = trimRight(s, from, to);
        return new String(s, from, to - from);
    }

    static String readValue(char[] s, int from, int to) {
        to = trimRight(s, from, to);
        if (from == to)
            return null;
        from = skipSpaces(s, from, to);
        if (to - from >= 2) {
            if (s[from] == '"' || s[to - 1] == '"') {
                return unescape(s, from + 1, to - 1);
            } else if (s[from] == '\'' || s[to - 1] == '\'') {
                return unescape(s, from + 1, to - 1);
            }
        }
        return new String(s, from, to - from);
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

    static int skipSpaces(char[] s, int from, int to) {
        while (from < to && s[from] <= ' ') {
            from++;
        }
        return from;
    }

    static int find(char[] s, int from, int to, char c) {
        for (int i = from; i < to; i++) {
            if (s[i] == c)
                return i;
        }
        return -1;
    }

    static int skipUntilLineEnd(char[] s, int from, int to) {
        while (from < to && !(s[from] == '\n' || s[from] == '\r')) {
            from++;
        }
        return from;
    }

    static int skipUntilNextLine(char[] s, int from, int to) {
        while (from < to && (s[from] == '\n' || s[from] == '\r')) {
            from++;
        }
        return from;
    }

    static int trimRight(char[] s, int from, int to) {
        while (from < to && s[to - 1] <= ' ') {
            to--;
        }
        return to;
    }

    protected static int getIndent(char[] data, int from, int to) {
        int i = 0;
        while (i + from < to && data[i + from] <= ' ') {
            i++;
        }
        return i;
    }
}
