package org.bordl.utils.io;

import org.bordl.utils.WrappedException;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author: moxa
 * Date: 3/24/13
 */
public class FileUtils {

    public static byte[] bytes(String file) {
        return bytes(new File(file));
    }

    public static String text(String file) {
        return text(new File(file), Charset.forName("utf-8"));
    }

    public static String text(File file) {
        return text(file, Charset.forName("utf-8"));
    }

    public static void text(String file, String text) {
        text(file, text, Charset.forName("utf-8"));
    }

    public static void text(File file, String text) {
        text(file, text, Charset.forName("utf-8"));
    }

    public static void text(String file, String text, Charset charset) {
        text(new File(file), text, charset);
    }

    public static void text(File file, String text, Charset charset) {
        bytes(file, text.getBytes(charset));
    }

    public static String text(String file, Charset charset) {
        return text(new File(file), charset);
    }

    public static String text(File file, Charset charset) {
        return new String(bytes(file), charset);
    }

    public static void bytes(String file, byte[] bytes) {
        bytes(new File(file), bytes, 0, bytes.length);
    }

    public static void bytes(File file, byte[] bytes) {
        bytes(file, bytes, 0, bytes.length);
    }

    public static void bytes(String file, byte[] bytes, int offset, int length) {
        bytes(new File(file), bytes, offset, length);
    }

    public static void bytes(File file, byte[] bytes, int offset, int length) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }
        try {
            out.write(bytes, offset, length);
        } catch (IOException e) {
            throw new WrappedException(e);
        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static byte[] bytes(File file) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }

        byte[] b = new byte[1024 * 50];
        int r;
        try {
            while ((r = in.read(b)) != -1) {
                out.write(b, 0, r);
            }
        } catch (IOException e) {
            throw new WrappedException(e);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }

        return out.toByteArray();
    }

}
