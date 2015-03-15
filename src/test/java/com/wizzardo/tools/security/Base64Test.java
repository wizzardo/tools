package com.wizzardo.tools.security;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

/**
 * Created by wizzardo on 15.03.15.
 */
public class Base64Test {

    @Test
    public void simple_test() throws UnsupportedEncodingException {
        String data = "foo bar\n";
        Assert.assertEquals("Zm9vIGJhcgo=", Base64.encodeToString(data.getBytes("utf-8")));
    }
}
