package com.wizzardo.tools.misc;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author: wizzardo
 * Date: 7/30/14
 */
public class CharTreeTest {

    @Test
    public void test1() {
        String[] strings = new String[]{
                "Accept", "Accept-Encoding", "Accept-Language",
                "Cache-Control", "Connection", "Cookie", "Host",
                "Pragma", "User-Agent", "Content-Type", "Content-Length",
                "Close", "Keep-Alive"
        };
        CharTree<String> tree = new CharTree<String>();
        for (String string : strings) {
            tree.append(string, string);
        }

        for (String string : strings) {
            Assert.assertEquals(string, tree.get(string.toCharArray()));
        }
    }

    @Test
    public void test2() {
        CharTree<String> tree = new CharTree<String>()
                .append("foo", "foo")
                .append("bar", "bar")
                .append("foobar", "foobar")
                .append("foo", "foo");

        Assert.assertEquals("foo", tree.get("foo".toCharArray()));
        Assert.assertEquals("bar", tree.get("bar".toCharArray()));
        Assert.assertEquals("foobar", tree.get("foobar".toCharArray()));
    }
}
