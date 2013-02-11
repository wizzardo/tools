package org.bordl.utils.xml;

import junit.framework.Assert;
import org.bordl.xml.Node;
import org.junit.Test;

/**
 * @author: moxa
 * Date: 2/11/13
 */
public class NodeTest {

    @Test
    public void parse() {
        String s;
        Node xml;

        s = "<xml><xml>";
        Assert.assertEquals("xml", Node.parse(s).name());
        s = "<xml/>";
        Assert.assertEquals("xml", Node.parse(s).name());

        s = "<xml attr><xml>";
        Assert.assertEquals(true, Node.parse(s).hasAttr("attr"));
        s = "<xml attr ><xml>";
        Assert.assertEquals(true, Node.parse(s).hasAttr("attr"));
        s = "<xml attr />";
        Assert.assertEquals(true, Node.parse(s).hasAttr("attr"));
        s = "<xml attr/>";
        Assert.assertEquals(true, Node.parse(s).hasAttr("attr"));
        s = "<xml attr attr2/>";
        Assert.assertEquals(true, Node.parse(s).hasAttr("attr"));

        s = "<xml attr=\"qwerty\"/>";
        Assert.assertEquals("qwerty", Node.parse(s).attr("attr"));
        s = "<xml attr=\"qwerty\" attr2/>";
        Assert.assertEquals("qwerty", Node.parse(s).attr("attr"));
        s = "<xml attr2 attr=\"qwerty\"/>";
        Assert.assertEquals("qwerty", Node.parse(s).attr("attr"));

        s = "<xml><child></child></xml>";
        Assert.assertEquals(1, Node.parse(s).size());
        s = "<xml><child/></xml>";
        Assert.assertEquals(1, Node.parse(s).size());
        s = "<xml><child attr=\"qwerty\"/></xml>";
        Assert.assertEquals(1, Node.parse(s).size());
        Assert.assertEquals("qwerty", Node.parse(s).first().attr("attr"));


        s = "<xml><child/><child/><child/>ololo</xml>";
        Assert.assertEquals(4, Node.parse(s).size());
        Assert.assertEquals("ololo", Node.parse(s).text());
        Assert.assertEquals("ololo", Node.parse(s).textOwn());
        s = "<xml><child/><child/><child>ololo</child></xml>";
        Assert.assertEquals(3, Node.parse(s).size());
        Assert.assertEquals("ololo", Node.parse(s).text());
        Assert.assertEquals("", Node.parse(s).textOwn());
        s = "<xml><child/><child/><child>ololo</child>lo</xml>";
        Assert.assertEquals(4, Node.parse(s).size());
        Assert.assertEquals("ololo lo", Node.parse(s).text());
        Assert.assertEquals("lo", Node.parse(s).textOwn());


    }
}
