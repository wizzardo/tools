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
                .append("foo2bar", "foo2bar");

        Assert.assertEquals("foo", tree.get("foo".toCharArray()));
        Assert.assertEquals("bar", tree.get("bar".toCharArray()));
        Assert.assertEquals("foobar", tree.get("foobar".toCharArray()));
        Assert.assertEquals("foo2bar", tree.get("foo2bar".toCharArray()));
    }

    @Test
    public void testStarts() {
        CharTree<String> tree = new CharTree<String>()
                .append("foo", "foo")
                .append("bar", "bar")
                .append("foobar", "foobar");

        Assert.assertEquals("foo", tree.findStarts("foo_".toCharArray()));
        Assert.assertEquals("bar", tree.findStarts("bar_".toCharArray()));
        Assert.assertEquals("foo", tree.findStarts("foobar_".toCharArray()));
        Assert.assertEquals("[foo, foobar]", tree.findAllStarts("foobar_".toCharArray()).toString());
    }

    @Test
    public void testEnds() {
        CharTree<String> tree = new CharTree<String>()
                .appendReverse("foo", "foo")
                .appendReverse("bar", "bar")
                .appendReverse("foobar", "foobar");

        Assert.assertEquals("foo", tree.findEnds("_foo".toCharArray()));
        Assert.assertEquals("bar", tree.findEnds("_bar".toCharArray()));
        Assert.assertEquals("bar", tree.findEnds("_foobar".toCharArray()));
        Assert.assertEquals("[bar, foobar]", tree.findAllEnds("_foobar".toCharArray()).toString());
    }
}
