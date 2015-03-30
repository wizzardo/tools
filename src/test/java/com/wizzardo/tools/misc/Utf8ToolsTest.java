package com.wizzardo.tools.misc;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Created by wizzardo on 30.03.15.
 */
public class Utf8ToolsTest {

    @Test
    public void encode() {
        char[] chars = new char[1];
        Charset utf8 = Charset.forName("UTF-8");
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            chars[0] = i;
            Assert.assertArrayEquals("fails on " + (int) i, String.valueOf(i).getBytes(utf8), Utf8Tools.encode(chars));
        }
    }

    @Test
    public void encode_surrogates() {
        char[] chars = new char[2];
        Charset utf8 = Charset.forName("UTF-8");
        for (char i = '\uD800'; i < '\uDC00'; i++) {
            chars[0] = i;
            for (char j = '\uDC00'; j < '\uE000'; j++) {
                chars[1] = j;
                Assert.assertArrayEquals("fails on " + (int) i + " " + (int) j, new String(chars).getBytes(utf8), Utf8Tools.encode(chars));
            }
        }
    }
}
