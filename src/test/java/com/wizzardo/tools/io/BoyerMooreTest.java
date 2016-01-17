package com.wizzardo.tools.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by wizzardo on 18.01.16.
 */
public class BoyerMooreTest {

    @Test
    public void test() {
        byte[] needle = new byte[2];
        byte[] data = new byte[15];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                if (i == 0 && j == 0)
                    continue;

                needle[0] = (byte) i;
                needle[1] = (byte) j;

                System.arraycopy(needle, 0, data, 0, 2);
                System.arraycopy(needle, 0, data, 3, 2);
                System.arraycopy(needle, 0, data, 7, 2);
                System.arraycopy(needle, 0, data, 13, 2);

                BoyerMoore boyerMoore = new BoyerMoore(needle);

                Assert.assertEquals(0, boyerMoore.search(data, 0, data.length));
                Assert.assertEquals(3, boyerMoore.search(data, 2, data.length));
                Assert.assertEquals(7, boyerMoore.search(data, 5, data.length - 5));
                Assert.assertEquals(13, boyerMoore.search(data, 9, data.length - 9));
            }
        }
    }
}
