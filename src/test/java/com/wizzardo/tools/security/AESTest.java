package com.wizzardo.tools.security;

import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Moxa
 */
public class AESTest {

    @Test
    public void simpeTest() {
        for (int i = 0; i < 100; i++) {
            byte[] data1 = new byte[1024 * 1024];
            Random random = new Random();
            random.nextBytes(data1);
            String md5 = MD5.create().update(data1).asString();

            AES aes = new AES("ololo key");
            byte[] enc = aes.encrypt(data1);
            Assert.assertNotSame(md5, MD5.create().update(enc).asString());
            aes = new AES("ololo key");
            byte[] dec = aes.decrypt(enc);
            Assert.assertEquals(md5, MD5.create().update(dec).asString());
        }
    }
}
