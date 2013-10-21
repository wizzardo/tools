/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.utils;

import com.wizzardo.tools.security.MD5;
import com.wizzardo.tools.io.SizedBlockReader;
import com.wizzardo.tools.io.SizedBlockWriter;
import org.junit.Test;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Moxa
 */
public class SizedBlockReaderTest {

    public SizedBlockReaderTest() {
    }

    @Test
    public void test1() throws IOException {
        String data = "ололо пыщпыщ";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SizedBlockWriter writer = new SizedBlockWriter(out);

        writer.setBlockLength(data.getBytes().length);
        writer.write(data.getBytes());

        SizedBlockReader reader = new SizedBlockReader(new ByteArrayInputStream(out.toByteArray()));
        byte[] b = new byte[128];
        int r;
        while (reader.hasNext()) {
            assertEquals(data.getBytes().length, reader.lenght());
            r = reader.read(b);
            assertEquals(data, new String(b, 0, r));
        }
    }

    @Test
    public void test2() throws IOException {
        String data = "test1";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SizedBlockWriter writer = new SizedBlockWriter(out);

        writer.setBlockLength(data.getBytes().length);
        writer.write(data.getBytes());

        data = "test2";
        writer.setBlockLength(data.getBytes().length);
        writer.write(data.getBytes());

        data = "test3";
        writer.setBlockLength(data.getBytes().length);
        writer.write(data.getBytes());

        SizedBlockReader reader = new SizedBlockReader(new ByteArrayInputStream(out.toByteArray()));
        byte[] b = new byte[128];
        int r;
        int part = 1;
        while (reader.hasNext()) {
            assertEquals(data.getBytes().length, reader.lenght());
            r = reader.read(b);
            assertEquals("test" + part++, new String(b, 0, r));
        }
    }

    @Test
    public void test3() throws IOException {
        final byte[][] data = new byte[100][];
        final Random random = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = new byte[1024];
            random.nextBytes(data[i]);
        }
        final HashMap<Integer, String> hashMapIn = new HashMap<Integer, String>();
        final HashMap<Integer, String> hashMapOut = new HashMap<Integer, String>();
        final MD5 md5In = new MD5();
        final MD5 md5Out = new MD5();
        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(out, 1024 * 1024);
        final SizedBlockReader br = new SizedBlockReader(in);
        final SizedBlockWriter writer = new SizedBlockWriter(out);
        long time = System.currentTimeMillis();
        new Thread(new Runnable() {
            public void run() {
                int i = 1;
                while (i < 1000) {
                    try {
                        writer.setBlockLength(1024 * 10 * 1024);
                        for (int j = 0; j < 1024 * 10; j++) {
                            int n = random.nextInt(data.length);
                            writer.write(data[n]);
                            md5Out.update(data[n], 0, data[n].length);
                        }
                        hashMapOut.put(i, md5Out.toString());
                        i++;
                        md5Out.reset();
                    } catch (IOException ex) {
                        Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        int r = 0;
        byte[] b = new byte[10240];
        int n = 1;
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Logger.getLogger(BlockReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            long t = 0;
            while (br.hasNext()) {
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
            assertEquals(1000, n);
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
}
