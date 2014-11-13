package com.wizzardo.tools.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author: wizzardo
 * Date: 3/1/14
 */
public class BytesToolsTests {

    @Test
    public void test() {
        long l = 1234567890 * 1234l;
        byte[] bytes = BytesTools.toBytes(l);
        Assert.assertEquals(l, BytesTools.toLong(bytes));
    }
}
