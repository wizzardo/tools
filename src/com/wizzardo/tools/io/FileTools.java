package com.wizzardo.tools.io;

import com.wizzardo.tools.WrappedException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: moxa
 * Date: 3/24/13
 */
public class FileTools {

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

    public static void bytes(String file, InputStream stream) {
        bytes(new File(file), stream);
    }

    public static void bytes(File file, InputStream stream) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }

        try {
            IOTools.copy(stream, out);
        } catch (IOException e) {
            throw new WrappedException(e);
        } finally {
            IOTools.close(out);
        }
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
            IOTools.close(out);
        }
    }

    public static byte[] bytes(File file) {
        InputStream in;

        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new WrappedException(e);
        }

        byte[] b = new byte[(int) file.length()];
        int r, total = 0;
        try {
            while (b.length - total > 0 && (r = in.read(b, total, b.length - total)) != -1) {
                total += r;
            }
        } catch (IOException e) {
            throw new WrappedException(e);
        } finally {
            IOTools.close(in);
        }

        return b;
    }

    public static List<File> listRecursive(File f) {
        return listRecursive(f, new ArrayList<File>(), null);
    }

    public static List<File> listRecursive(File f, FileFilter filter) {
        return listRecursive(f, new ArrayList<File>(), filter);
    }

    public static List<File> listRecursive(File f, List<File> into, FileFilter filter) {
        if (f == null)
            return into;

        if (!f.isDirectory()) {
            into.add(f);
        } else {
            File[] l = f.listFiles(filter);
            if (l != null)
                for (File file : l)
                    listRecursive(file, into, filter);
        }
        return into;
    }

}
