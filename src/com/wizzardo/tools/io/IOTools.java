package com.wizzardo.tools.io;

import java.io.*;

/**
 * @author: moxa
 * Date: 12/5/13
 */
public class IOTools {

    public static void close(Closeable c) {
        if (c == null)
            return;

        try {
            c.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * copy one stream into another with default buffer 10 KB
     *
     * @return total length of copied data
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        return copy(in, out, new byte[10240]);
    }

    /**
     * @return total length of copied data
     */
    public static long copy(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int r = 0;
        long l = 0;
        while ((r = in.read(buffer)) != -1) {
            out.write(buffer, 0, r);
            l += r;
        }
        return l;
    }

    public static byte[] bytes(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOTools.copy(in, out);
            return out.toByteArray();
        } finally {
            IOTools.close(in);
        }
    }
}
