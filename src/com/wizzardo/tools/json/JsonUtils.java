package com.wizzardo.tools.json;

/**
 * @author: wizzardo
 * Date: 3/21/14
 */
class JsonUtils {

    static int trimRight(char[] s, int from, int to) {
        while ((from < to) && (s[to - 1] <= ' ')) {
            to--;
        }
        return to;
    }

    static boolean isNull(char[] s, int from, int length) {
        return length == 4 && s[from] == 'n' && s[from + 1] == 'u' && s[from + 2] == 'l' && s[from + 3] == 'l';
    }

    static boolean isTrue(char[] s, int from, int length) {
        return length == 4 && s[from] == 't' && s[from + 1] == 'r' && s[from + 2] == 'u' && s[from + 3] == 'e';
    }

    static boolean isFalse(char[] s, int from, int length) {
        return length == 5 && s[from] == 'f' && s[from + 1] == 'a' && s[from + 2] == 'l' && s[from + 3] == 's' && s[from + 4] == 'e';
    }

    static int skipSpaces(char[] s, int from, int to) {
        while (from < to && s[from] <= ' ') {
            from++;
        }
        return from;
    }

    static int parseNumber(JsonItem holder, char[] s, int from, int to) {
        int i = from;

        boolean minus = s[from] == '-';
        if (minus)
            i++;

        if (i == to)
            throw new IllegalStateException("no number found, only '-'");

        boolean floatValue = false;
        long l = 0;
        char ch;
        for (; i < to; i++) {
            ch = s[i];
            if (ch >= '0' && ch <= '9')
                l = l * 10 + (ch - '0');
            else if (ch == '.' && !floatValue)
                floatValue = true;
            else
                break;

        }

        if (!floatValue)
            if (minus)
                holder.set(-l);
            else
                holder.set(l);
        else {
            if (minus)
                from++;
            double d = Double.parseDouble(new String(s, from, i - from));
            if (minus)
                d = -d;
            holder.set(d);
        }

        return i;
    }

    static int parseValue(JsonItem holder, char[] s, int from, int to, char end) {
        char ch = s[from];

        char quote = 0;
        if (ch == '"' || ch == '\'') {
            quote = ch;
            from++;
        }

        int i = from;
        boolean escape = false;
        if (quote == 0) {
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ',' || ch == end)
                    break;

                if (ch == '\\')
                    escape = true;
            }
        } else {
            for (; i < to; i++) {
                ch = s[i];
                if (ch == quote && s[i - 1] != '\\')
                    break;

                if (ch == '\\')
                    escape = true;
            }
        }

        int k = i;
        if (quote == 0)
            k = trimRight(s, from, k);
        else
            i++;

        String value;
        int l = k - from;
        if (!escape) {
            if (JsonUtils.isNull(s, from, l)) {
                return i;
            }
            if (isTrue(s, from, l)) {
                holder.set(Boolean.TRUE);
                return i;
            }
            if (isFalse(s, from, l)) {
                holder.set(Boolean.FALSE);
                return i;
            }

            value = new String(s, from, l);
        } else
            value = JsonObject.unescape(s, from, k);

        holder.set(value);

        return i;
    }

    static int parseKey(JsonItem holder, char[] s, int from, int to) {
        int i = from;
        char ch = s[i];

        char quote = 0;
        if (ch == '\'' || ch == '"') {
            quote = ch;
            i++;
            from++;
        }

        int rigthBorder;
        boolean escape = false;
        if (quote == 0) {
            for (; i < to; i++) {
                ch = s[i];
                if (ch <= ' ' || ch == ':' || ch == '}')
                    break;

                if (ch == '\\')
                    escape = true;
            }
            rigthBorder = trimRight(s, from, i);
        } else {
            for (; i < to; i++) {
                ch = s[i];
                if (ch == quote && s[i - 1] != '\\')
                    break;

                if (ch == '\\')
                    escape = true;
            }
            rigthBorder = i;
            i++;
        }

        i = skipSpaces(s, i, to);

        if (s[i] != ':')
            throw new IllegalStateException("here must be key-value separator ':', but found: " + s[i]);


        if (rigthBorder == from)
            throw new IllegalStateException("key not found");

        String value;
        if (!escape)
            value = new String(s, from, rigthBorder - from);
        else
            value = JsonObject.unescape(s, from, rigthBorder);

        holder.set(value);

        return i + 1;
    }
}
