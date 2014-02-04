/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.tools;

import com.wizzardo.tools.io.BlockReader;
import com.wizzardo.tools.io.ProgressListener;
import com.wizzardo.tools.security.MD5;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Moxa
 */
public class BlockReaderTest {

    public BlockReaderTest() {
    }

    @Test
    public void testGetInput() {
        BlockReader br = new BlockReader(new ByteArrayInputStream("test1\ntest2\ntest3".getBytes()), "\n".getBytes());
        br.next();
        byte[] b = new byte[10];
        int r;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            while ((r = br.read(b)) != -1) {
                data.write(b, 0, r);
            }
            assertEquals("test1", new String(data.toByteArray()));
            data.reset();
            InputStream in = br.getInputStream();
            while ((r = in.read(b)) != -1) {
                data.write(b, 0, r);
            }
            assertEquals("test2\ntest3", new String(data.toByteArray()));
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testAppending() {
        BlockReader br = new BlockReader(new ByteArrayInputStream("test1\ntest2\ntest3\ntest4\ntest5\ntest6".getBytes()), "\n".getBytes());
        byte[] b = new byte[1024];
        int r, t = 0;
        try {
            while (br.hasNext()) {
                br.next();
                while ((r = br.read(b, t, b.length - t)) != -1) {
                    System.out.println("readed: " + new String(b, t, r) + " \t" + r);
                    t += r;
                }
            }
            assertEquals("test1test2test3test4test5test6", new String(b, 0, t));
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test1() {
        int n = 0;
        try {
            BlockReader bl = new BlockReader(new ByteArrayInputStream("test1\ntest2\ntest3".getBytes()), "\n".getBytes());
            byte[] b = new byte[2];
            int r = 0;
            while (bl.hasNext()) {
                n++;
                bl.next();
                System.out.println("next");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((r = bl.read(b)) != -1) {
                    out.write(b, 0, r);
                    System.out.println(new String(b, 0, r) + "\t" + Arrays.toString(b) + "\t" + r);
                }
                switch (n) {
                    case 1: {
                        assertEquals("test1", new String(out.toByteArray()));
                        break;
                    }
                    case 2: {
                        assertEquals("test2", new String(out.toByteArray()));
                        break;
                    }
                    case 3: {
                        assertEquals("test3", new String(out.toByteArray()));
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(n, 3);
    }

    @Test
    public void test2() {
        int n = 0;
        try {
            BlockReader bl = new BlockReader(new ByteArrayInputStream("test1{sep}test2{sep}test3".getBytes()), "{sep}".getBytes());
            byte[] b = new byte[500];
            int r = 0;
            while (bl.hasNext()) {
                n++;
                bl.next();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((r = bl.read(b)) != -1) {
                    out.write(b, 0, r);
//                    System.out.println(new String(b, 0, r));
                }
                switch (n) {
                    case 1: {
                        assertEquals("test1", new String(out.toByteArray()));
                        break;
                    }
                    case 2: {
                        assertEquals("test2", new String(out.toByteArray()));
                        break;
                    }
                    case 3: {
                        assertEquals("test3", new String(out.toByteArray()));
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(3, n);
    }

    @Test
    public void test6() {
        int n = 0;
        try {
            BlockReader bl = new BlockReader(new ByteArrayInputStream("test1{separator ololo}[]{separator ololo}test3".getBytes()), "{separator ololo}".getBytes());
            byte[] b = new byte[500];
            int r = 0;
            while (bl.hasNext()) {
                n++;
                bl.next();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((r = bl.read(b)) != -1) {
                    out.write(b, 0, r);
//                    System.out.println(new String(b, 0, r));
                }
                switch (n) {
                    case 1: {
                        assertEquals("test1", new String(out.toByteArray()));
                        break;
                    }
                    case 2: {
                        assertEquals("[]", new String(out.toByteArray()));
                        break;
                    }
                    case 3: {
                        assertEquals("test3", new String(out.toByteArray()));
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(3, n);
    }

    @Test
    public void testEnding() {
        assertEquals(5, BlockReader.isEnding("123;456".getBytes(), 7, "56".getBytes()));
        assertEquals(6, BlockReader.isEnding("123;456".getBytes(), 7, "6".getBytes()));
        assertEquals(0, BlockReader.isEnding("123;456".getBytes(), 7, "123;456".getBytes()));
        assertEquals(-1, BlockReader.isEnding("123;456".getBytes(), 7, ";".getBytes()));
    }

    @Test
    public void test3() throws IOException {
        byte[] separator = "!!!separator!!!".getBytes();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data1 = new byte[10240], data2 = new byte[10240], data3 = new byte[10240];
        Random random = new Random();
        random.nextBytes(data1);
        random.nextBytes(data2);
        random.nextBytes(data3);

        out.write(data1);
        out.write(separator);
        out.write(data2);
        out.write(separator);
        out.write(data3);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        BlockReader bl = new BlockReader(in, separator);

        MD5 md5 = new MD5();
        String[] hashes = new String[3];
        hashes[0] = MD5.getMD5AsString(data1);
        hashes[1] = MD5.getMD5AsString(data2);
        hashes[2] = MD5.getMD5AsString(data3);

        md5.update(data1, 0, data1.length);
        assertEquals(hashes[0], md5.toString());

        int n = 0;
        while (bl.hasNext()) {
            md5.reset();
            bl.next();
            int r = 0;
            byte[] b = new byte[102400];
            while ((r = bl.read(b)) != -1) {
                md5.update(b, 0, r);
            }
            assertEquals(hashes[n], md5.toString());
            n++;
        }
    }

    @Test
    public void test4() throws IOException {
        final int partLength = 1024;
        final byte[][] data = new byte[100][];
        final Random random = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = new byte[partLength];
            random.nextBytes(data[i]);
        }
        final HashMap<Integer, String> hashMapIn = new HashMap<Integer, String>();
        final HashMap<Integer, String> hashMapOut = new HashMap<Integer, String>();
        final MD5 md5In = new MD5();
        final MD5 md5Out = new MD5();
        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(out, 1024 * 1024);
        final byte[] separator = "!!!!separator!!!!".getBytes();
        long time = System.currentTimeMillis();
        final int k = 10;
        final int parts = 10240;

        ProgressListener pl = new ProgressListener() {
            @Override
            protected void onProgressChanged(int progress) {
                System.out.println(progress);
            }
        };
        int length = (separator.length + partLength * parts) * (k - 1);
        final AtomicLong writed = new AtomicLong();
        final BlockReader br = new BlockReader(in, separator, length, pl);
        new Thread(new Runnable() {
            public void run() {
                int i = 1;
                while (i < k) {
                    try {
                        for (int j = 0; j < parts; j++) {
                            int n = random.nextInt(data.length);
                            out.write(data[n]);
                            writed.addAndGet(data[n].length);
                            md5Out.update(data[n], 0, data[n].length);
                        }
                        hashMapOut.put(i, md5Out.toString());
                        i++;
                        md5Out.reset();
                        out.write(separator);
                        writed.addAndGet(separator.length);
                    } catch (IOException ex) {
                        Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        int r = 0;
        byte[] b = new byte[1024 * 50];
        int n = 1;
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            long t = 0;
            while (br.hasNext()) {
                br.next();
//                System.out.println("next block: " + n);
                md5In.reset();
                while ((r = br.read(b)) != -1) {
                    md5In.update(b, 0, r);
                    t += r;
                }
                hashMapIn.put(n, md5In.toString());
                n++;
            }
            time = System.currentTimeMillis() - time;
            System.out.println("end reading: " + time + "ms for " + t + " bytes");
            System.out.println("speed: " + (t / 1024f / 1024f) / (time / 1000f) + " MB/s");
            assertEquals(k, n);
            LinkedList<Integer> list = new LinkedList<Integer>(hashMapOut.keySet());
            Collections.sort(list);
            for (Integer i : list) {
//                System.out.println(i + "\t" + hashMapOut.get(i).equals(hashMapIn.get(i)) + "\t" + hashMapIn.get(i) + "\t" + hashMapOut.get(i));
                assertEquals(hashMapOut.get(i), hashMapIn.get(i));
            }
        } catch (IOException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test5() throws IOException {
        PipedInputStream in1 = new PipedInputStream();
        PipedInputStream in2 = new PipedInputStream();
        final PipedOutputStream out1 = new PipedOutputStream(in2);
        final PipedOutputStream out2 = new PipedOutputStream(in1);

        final byte[] sep = "!!!!separator!!!!".getBytes();
        final BlockReader br1 = new BlockReader(in1, sep);
        final BlockReader br2 = new BlockReader(in2, sep);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        out1.write(("" + i).getBytes());
                        out1.write(sep);
                    } catch (IOException ex) {
                        Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        String s = getAnswer(br1);
                        System.out.println("br1: " + s);
                        if (!("" + i).equals(s)) {
                            System.out.println("fuckup!!!!!!!!  " + s);
                            break;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        for (int i = 0; i < 10; i++) {
            try {
                String s = getAnswer(br2);
                System.out.println("br2: " + s);
                if (!("" + i).equals(s)) {
                    System.out.println("fuckup!!!!!!!!  " + s);
                    break;
                }
            } catch (IOException ex) {
                Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                out2.write(("" + i).getBytes());
                out2.write(sep);
            } catch (IOException ex) {
                Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String getAnswer(BlockReader br) throws IOException {
        br.next();
        StringBuilder sb = new StringBuilder();
        int r = 0;
        byte[] b = new byte[1024];
        while ((r = br.read(b)) != -1) {
            sb.append(new String(b, 0, r));
        }
//        System.out.println("read: " + sb.toString());
        return sb.toString();
    }
}
