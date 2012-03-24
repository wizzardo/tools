package org.bordl.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author moxa
 */
public class XmlParser {

    private boolean parsing = false;
    private Node node;

    public static Node parse(String xml, boolean ignoreCase) throws XmlParseException {
        return new Node(xml, ignoreCase);
    }

    public static Node parse(File f, boolean ignoreCase) throws IOException {
        return parse(new FileInputStream(f), ignoreCase);
    }

    public static Node parse(InputStream in, boolean ignoreCase) throws IOException {
        byte[] b = new byte[102400], buffer = new byte[0];
        int r = 0;
        while ((r = in.read(b)) != -1) {
            byte[] temp = new byte[buffer.length + r];
            System.arraycopy(buffer, 0, temp, 0, buffer.length);
            System.arraycopy(b, 0, temp, buffer.length, r);
            buffer = temp;
        }
        in.close();
        return new Node(new String(buffer, getEncoding(buffer)), ignoreCase);
    }

    private static String getEncoding(byte[] b) {
        String s = null;
        try {
            s = new String(b, 0, b.length < 200 ? b.length : 200, "utf-8");
            int temp = s.toLowerCase().indexOf("encoding") + 10;
            s = s.substring(temp, s.indexOf("\"", temp));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    /**
    use only with createInputStream();
     * @return Node or null, if error occurrence or time is out (10 sec)
     */
    public Node getNode() {
        long wait = 0;
        long start = System.currentTimeMillis();
        while (parsing) {
            if (wait > 10000) {
                return null;
            }
            try {
                synchronized (this) {
                    this.wait(10000 - wait);
                }
            } catch (InterruptedException ex) {
            }
            if (parsing) {
                wait = System.currentTimeMillis() - start;
            }
        }
        return node;
    }

    /**
    Create new thread to read XML
     * @return OutputStream to write data
     */
    public OutputStream createInputStream() throws IOException {
        final PipedInputStream in = new PipedInputStream(10240);
        PipedOutputStream out = new PipedOutputStream(in);
        parsing = true;
        final XmlParser instance = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    node = parse(in, false);
                } catch (Exception ex) {
                    Logger.getLogger(XmlParser.class.getName()).log(Level.SEVERE, null, ex);
                }
                parsing = false;
                synchronized (instance) {
                    instance.notifyAll();
                }
            }
        }).start();
        return out;
    }
}
