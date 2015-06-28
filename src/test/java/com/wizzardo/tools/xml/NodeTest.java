package com.wizzardo.tools.xml;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author: moxa
 * Date: 2/11/13
 */
public class NodeTest {

    @Test
    public void parse() {
        String s;
        Node xml;

        s = "I say: '${hello}'";
        Assert.assertEquals("I say: '${hello}'", Node.parse(s, true).textOwn());

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


        s = "<xml>\n\t\tololo\n\t\t</xml>";
        Assert.assertEquals("ololo", Node.parse(s).text());
    }

    @Test
    public void html() throws IOException {
        String s = "";
        for (File f : new File("src/test/resources/xml").listFiles()) {
            System.out.println("parsing: " + f);
            Node.parse(f, true);
        }
    }

    @Test
    public void gsp_1() throws IOException {
        String s = "<div><g:textField name=\"${it.key}\" placeholder=\"${[].collect({it})}\"/></div>";
        Node root = Node.parse(s, true, true);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(1, div.children().size());

        Node textField = div.children().get(0);
        Assert.assertEquals("g:textField", textField.name());
        Assert.assertEquals(0, textField.children().size());
        Assert.assertEquals(2, textField.attributes().size());
        Assert.assertEquals("${it.key}", textField.attr("name"));
        Assert.assertEquals("${[].collect({it})}", textField.attr("placeholder"));
    }

    @Test
    public void gsp_2() throws IOException {
        String s = "<div><g:textField name=\"${it.key}\" placeholder=\"${String.valueOf(it.getValue()).replace(\"\\\"\", \"\")}\"/></div>";
        Node root = Node.parse(s, true, true);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(1, div.children().size());

        Node textField = div.children().get(0);
        Assert.assertEquals("g:textField", textField.name());
        Assert.assertEquals(0, textField.children().size());
        Assert.assertEquals(2, textField.attributes().size());
        Assert.assertEquals("${it.key}", textField.attr("name"));
        Assert.assertEquals("${String.valueOf(it.getValue()).replace(\"\\\"\", \"\")}", textField.attr("placeholder"));
    }
}
