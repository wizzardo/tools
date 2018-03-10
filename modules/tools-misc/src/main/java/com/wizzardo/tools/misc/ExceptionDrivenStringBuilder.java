package com.wizzardo.tools.misc;

import com.wizzardo.tools.interfaces.Consumer;
import com.wizzardo.tools.interfaces.Supplier;

import java.util.Date;

/**
 * @author: wizzardo
 * Date: 8/24/14
 */
public class ExceptionDrivenStringBuilder implements Appendable {
    private static final char[] CHARS_TRUE = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] CHARS_FALSE = new char[]{'f', 'a', 'l', 's', 'e'};
    private static final char[] CHARS_NULL = new char[]{'n', 'u', 'l', 'l'};

    protected int limit = 16;
    protected char[] buffer = new char[limit];
    protected int length = 0;
    protected UTF8 utf8Buffer;

    private void ensureCapacity(int length) {
        if (length >= limit) {
            char[] temp = new char[Math.max((int) (this.limit * 1.5), length)];
            System.arraycopy(buffer, 0, temp, 0, this.length > buffer.length ? buffer.length : this.length);
            buffer = temp;
            limit = length;
        }
    }

    public ExceptionDrivenStringBuilder replace(char a, char b) {
        return replace(a, b, 0, length);
    }

    public ExceptionDrivenStringBuilder replace(char a, char b, int from, int to) {
        from = Math.max(0, from);
        to = Math.min(to, length);
        char[] buffer = this.buffer;
        for (int i = from; i < to; i++) {
            if (buffer[i] == a)
                buffer[i] = b;
        }
        return this;
    }

    @Override
    public ExceptionDrivenStringBuilder append(CharSequence csq) {
        return append(csq, 0, csq == null ? 4 : csq.length());
    }

    @Override
    public ExceptionDrivenStringBuilder append(CharSequence csq, int start, int end) {
        if (csq == null)
            return append(CHARS_NULL, 0, 4);

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
        } catch (IndexOutOfBoundsException ex) {
            buffer[length - 1] = ch;
            return append(ch);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(String s) {
        return append(s, 0, s == null ? 4 : s.length());
    }

    public ExceptionDrivenStringBuilder append(String s, int from, int to) {
        if (s == null)
            return append(CHARS_NULL, 0, 4);

        int l = to - from;
        try {
            s.getChars(from, to, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + l);
            return append(s, from, to);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + l);
            return append(s, from, to);
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
            return append(chars, from, length);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + this.length);
            return append(chars, from, length);
        }
        this.length += length;
        return this;
    }

    public void setLength(int length) {
        ensureCapacity(length);
        this.length = length;
    }

    public ExceptionDrivenStringBuilder clear() {
        setLength(0);
        return this;
    }

    public ExceptionDrivenStringBuilder append(Object ob) {
        return append(String.valueOf(ob));
    }

    public ExceptionDrivenStringBuilder append(Date date) {
        try {
            length = DateIso8601.formatToChars(date, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + 28);
            return append(date);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + 28);
            return append(date);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(int i) {
        try {
            length = NumberToChars.toChars(i, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + 11);
            return append(i);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + 11);
            return append(i);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(long i) {
        try {
            length = NumberToChars.toChars(i, buffer, length);
        } catch (ArrayIndexOutOfBoundsException ex) {
            ensureCapacity(length + 20);
            return append(i);
        } catch (IndexOutOfBoundsException ex) {
            ensureCapacity(length + 20);
            return append(i);
        }
        return this;
    }

    public ExceptionDrivenStringBuilder append(boolean b) {
        if (b)
            append(CHARS_TRUE, 0, 4);
        else
            append(CHARS_FALSE, 0, 5);
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

    public int length() {
        return length;
    }

    /**
     * @return byte array with UTF-8 encoded string
     **/
    public byte[] toBytes() {
        if (utf8Buffer == null)
            utf8Buffer = new UTF8();

        return utf8Buffer.toBytes(buffer, 0, length);
    }

    public void toBytes(Supplier<byte[]> bytesSupplier, UTF8.BytesConsumer bytesConsumer) {
        UTF8.encode(buffer, 0, length, bytesSupplier, bytesConsumer);
    }

    public ExceptionDrivenStringBuilder append(Consumer<ExceptionDrivenStringBuilder> consumer) {
        consumer.consume(this);
        return this;
    }

    public ExceptionDrivenStringBuilder append(Supplier<? extends CharSequence> supplier) {
        return append(supplier.supply());
    }

}
