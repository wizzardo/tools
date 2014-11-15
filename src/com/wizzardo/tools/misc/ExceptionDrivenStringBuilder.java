package com.wizzardo.tools.misc;

/**
 * @author: wizzardo
 * Date: 8/24/14
 */
public class ExceptionDrivenStringBuilder implements Appendable {
    private static final char[] CHARS_TRUE = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] CHARS_FALSE = new char[]{'f', 'a', 'l', 's', 'e'};

    private int limit = 16;
    private char[] buffer = new char[limit];
    private int length = 0;

    private void ensureCapacity(int length) {
        if (length >= limit) {
            char[] temp = new char[Math.max((int) (this.limit * 1.5), length)];
            System.arraycopy(buffer, 0, temp, 0, this.length > buffer.length ? buffer.length : this.length);
            buffer = temp;
            limit = length;
        }
    }

    @Override
    public ExceptionDrivenStringBuilder append(CharSequence csq) {
        return append(csq, 0, csq == null ? 4 : csq.length());
    }

    @Override
    public ExceptionDrivenStringBuilder append(CharSequence csq, int start, int end) {
        if (csq == null)
            return append("null", start, end);

        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(char ch) {
        try {
            buffer[length++] = ch;
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length);
            buffer[length - 1] = ch;
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(String s) {
        return append(s, 0, s.length());
    }

    public ExceptionDrivenStringBuilder append(String s, int from, int to) {
        int l = to - from;
        try {
            s.getChars(from, to, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + l);
            s.getChars(from, to, buffer, length);
        }
        length += l;
        return this;
    }

    public ExceptionDrivenStringBuilder append(char[] chars) {
        return append(chars, 0, chars.length);
    }

    public ExceptionDrivenStringBuilder append(char[] chars, int from, int length) {
        try {
            System.arraycopy(chars, from, buffer, this.length, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + this.length);
            System.arraycopy(chars, from, buffer, this.length, length);
        }
        this.length += length;
        return this;
    }

    public void setLength(int length) {
        ensureCapacity(length);
        this.length = length;
    }

    public ExceptionDrivenStringBuilder append(Object ob) {
        return append(String.valueOf(ob));
    }

    public ExceptionDrivenStringBuilder append(int i) {
        try {
            length = NumberToChars.toChars(i, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + 11);
            length = NumberToChars.toChars(i, buffer, length);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(long i) {
        try {
            length = NumberToChars.toChars(i, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + 20);
            length = NumberToChars.toChars(i, buffer, length);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(boolean b) {
        if (b)
            append(CHARS_TRUE);
        else
            append(CHARS_FALSE);
        return this;
    }

    public ExceptionDrivenStringBuilder append(float i) {
        return append(String.valueOf(i));
    }

    public ExceptionDrivenStringBuilder append(double i) {
        return append(String.valueOf(i));
    }

    @Override
    public String toString() {
        return new String(buffer, 0, length);
    }
}
