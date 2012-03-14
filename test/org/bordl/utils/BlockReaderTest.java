/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Moxa
 */
public class BlockReaderTest {

    public BlockReaderTest() {
    }

    @Test
    public void test1() {
        int n = 0;
        try {
            BlockReader bl = new BlockReader(new ByteArrayInputStream("test1\ntest2\ntest3".getBytes()), "\n".getBytes());
            byte[] b = new byte[2];
            int r = 0;
            while (bl.hashNext()) {
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
        assertEquals(n, 3);
    }

    @Test
    public void test2() {
        int n = 0;
        try {
            BlockReader bl = new BlockReader(new ByteArrayInputStream("test1{sep}test2{sep}test3".getBytes()), "{sep}".getBytes());
            byte[] b = new byte[500];
            int r = 0;
            while (bl.hashNext()) {
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
    public void testEnding() {
        assertEquals(5, BlockReader.isEnding("123;456".getBytes(), "56".getBytes()));
        assertEquals(6, BlockReader.isEnding("123;456".getBytes(), "6".getBytes()));
        assertEquals(0, BlockReader.isEnding("123;456".getBytes(), "123;456".getBytes()));
        assertEquals(-1, BlockReader.isEnding("123;456".getBytes(), ";".getBytes()));
    }
}
