package com.wizzardo.tools.security;

import org.junit.Assert;
import org.junit.Test;

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
}
