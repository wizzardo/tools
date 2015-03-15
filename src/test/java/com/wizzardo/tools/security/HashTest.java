package com.wizzardo.tools.security;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wizzardo on 15.03.15.
 */
public class HashTest {

    @Test
    public void md5_test() {
        String data = "foo bar\n";
        Assert.assertEquals("5ceaa7ed396ccb8e959c02753cb4bd18", MD5.create().update(data).asString());
    }

    @Test
    public void sha1_test() {
        String data = "foo bar\n";
        Assert.assertEquals("d53a205a336e07cf9eac45471b3870f9489288ec", SHA1.create().update(data).asString());
    }

    @Test
    public void hash_test() throws IOException {
        String data = "foo bar\n";
        Assert.assertTrue(MD5.create().check(data, "5ceaa7ed396ccb8e959c02753cb4bd18"));

        byte[] hash = new byte[]{92, -22, -89, -19, 57, 108, -53, -114, -107, -100, 2, 117, 60, -76, -67, 24};
        Assert.assertTrue(MD5.create().check(data.getBytes("utf-8"), hash));


        MD5 md5 = MD5.create();
        md5.update(data);
        md5.update(data);
        Assert.assertNotEquals("5ceaa7ed396ccb8e959c02753cb4bd18", md5.toString());
        md5.reset();
        md5.update(data);
        Assert.assertEquals("5ceaa7ed396ccb8e959c02753cb4bd18", md5.toString());

        InputStream in = new ByteArrayInputStream(data.getBytes("utf-8"));
        Assert.assertEquals("5ceaa7ed396ccb8e959c02753cb4bd18", MD5.create().update(in).asString());

        byte[] bytes = data.getBytes("utf-8");
        md5 = MD5.create();
        for (byte b : bytes) {
            md5.update(b);
        }
        Assert.assertEquals("5ceaa7ed396ccb8e959c02753cb4bd18", md5.toString());

        boolean exception = false;
        try {
            new Hash("some hash") {
                @Override
                protected int hexStringLength() {
                    return 0;
                }
            };
        } catch (Exception e) {
            exception = true;
            Assert.assertEquals(NoSuchAlgorithmException.class, e.getClass());
        }
        Assert.assertTrue(exception);
    }
}
