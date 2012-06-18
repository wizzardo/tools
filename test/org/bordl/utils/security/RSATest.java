package org.bordl.utils.security;

import java.util.Arrays;
import java.util.Random;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Moxa
 */
public class RSATest {

    @Test
    public void simpeTest() {
        for (int i = 0; i < 100; i++) {
            byte[] data1 = new byte[117];
            Random random = new Random();
            random.nextBytes(data1);
            String md5 = MD5.getMD5AsString(data1);

            RSA rsa = new RSA(1024);
            byte[] enc = rsa.encrypt(data1);
            Assert.assertNotSame(md5, MD5.getMD5AsString(enc));
            byte[] dec = rsa.decrypt(enc, data1.length);
            System.out.println(Arrays.toString(data1));
            System.out.println(Arrays.toString(dec));
            Assert.assertEquals(md5, MD5.getMD5AsString(dec));
        }
    }
}
