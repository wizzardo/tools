package com.wizzardo.tools.misc;

import com.wizzardo.tools.reflection.StringReflection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by wizzardo on 31.08.15.
 */
public class UTF8Writer extends Writer {
    private static final byte[] CHARS_TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static final byte[] CHARS_FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};
    private static final byte[] CHARS_NULL = new byte[]{'n', 'u', 'l', 'l'};

    protected OutputStream out;
    protected int batchSize = 1024;
    protected byte[] bytes = new byte[batchSize * 4];

    public UTF8Writer(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(char[] chars, int off, int len) throws IOException {
        int limit = len + off;
        for (int i = off; i < limit; i += batchSize) {
            int l = UTF8.encode(chars, off, Math.min(off - limit, batchSize), bytes);
            out.write(bytes, 0, l);
        }
    }

    public void write(int i) throws IOException {
        int l = NumberToChars.toChars(i, bytes, 0);
        out.write(bytes, 0, l);
    }

    public void write(long i) throws IOException {
        int l = NumberToChars.toChars(i, bytes, 0);
        out.write(bytes, 0, l);
    }

    public void write(String s) throws IOException {
        if (s == null)
            out.write(CHARS_NULL);
        else
            write(s, 0, s.length());
    }

    public void write(String s, int off, int l) throws IOException {
        int offset = StringReflection.offset(s) + off;
        char[] chars = StringReflection.chars(s);
        write(chars, offset, l);
    }

    public void write(boolean b) throws IOException {
        if (b)
            out.write(CHARS_TRUE, 0, 4);
        else
            out.write(CHARS_FALSE, 0, 5);
    }


    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
    }
}
