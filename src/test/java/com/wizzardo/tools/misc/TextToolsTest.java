package com.wizzardo.tools.misc;

import org.junit.Assert;
import org.junit.Test;

import static com.wizzardo.tools.misc.TextTools.*;

public class TextToolsTest {

    @Test
    public void test_substrings(){
        String s = "one.two.three";

        Assert.assertEquals("one", substringBefore(s, "."));
        Assert.assertEquals("one.two", substringBeforeLast(s, "."));
        Assert.assertEquals("two.three", substringAfter(s, "."));
        Assert.assertEquals("three", substringAfterLast(s, "."));

        Assert.assertEquals(s, substringBefore(s, "="));
        Assert.assertEquals(s, substringBeforeLast(s, "="));
        Assert.assertEquals(s, substringAfter(s, "="));
        Assert.assertEquals(s, substringAfterLast(s, "="));

        Assert.assertEquals("", substringBefore(s, "=", ""));
        Assert.assertEquals("", substringBeforeLast(s, "=", ""));
        Assert.assertEquals("", substringAfter(s, "=", ""));
        Assert.assertEquals("", substringAfterLast(s, "=", ""));
    }
}
