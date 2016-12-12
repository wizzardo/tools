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
        Assert.assertEquals("I say: '${hello}'", new XmlParser().parse(s).textOwn());

        s = "<xml><xml>";
        Assert.assertEquals("xml", new XmlParser().parse(s).name());
        s = "<xml/>";
        Assert.assertEquals("xml", new XmlParser().parse(s).name());

        s = "<xml attr><xml>";
        Assert.assertEquals(true, new XmlParser().parse(s).hasAttr("attr"));
        s = "<xml attr ><xml>";
        Assert.assertEquals(true, new XmlParser().parse(s).hasAttr("attr"));
        s = "<xml attr />";
        Assert.assertEquals(true, new XmlParser().parse(s).hasAttr("attr"));
        s = "<xml attr/>";
        Assert.assertEquals(true, new XmlParser().parse(s).hasAttr("attr"));
        s = "<xml attr attr2/>";
        Assert.assertEquals(true, new XmlParser().parse(s).hasAttr("attr"));

        s = "<xml attr=\"qwerty\"/>";
        Assert.assertEquals("qwerty", new XmlParser().parse(s).attr("attr"));
        s = "<xml attr=\"qwerty\" attr2/>";
        Assert.assertEquals("qwerty", new XmlParser().parse(s).attr("attr"));
        s = "<xml attr2 attr=\"qwerty\"/>";
        Assert.assertEquals("qwerty", new XmlParser().parse(s).attr("attr"));

        s = "<xml><child></child></xml>";
        Assert.assertEquals(1, new XmlParser().parse(s).size());
        s = "<xml><child/></xml>";
        Assert.assertEquals(1, new XmlParser().parse(s).size());
        s = "<xml><child attr=\"qwerty\"/></xml>";
        Assert.assertEquals(1, new XmlParser().parse(s).size());
        Assert.assertEquals("qwerty", new XmlParser().parse(s).first().attr("attr"));


        s = "<xml><child/><child/><child/>ololo</xml>";
        Assert.assertEquals(4, new XmlParser().parse(s).size());
        Assert.assertEquals("ololo", new XmlParser().parse(s).text());
        Assert.assertEquals("ololo", new XmlParser().parse(s).textOwn());
        s = "<xml><child/><child/><child>ololo</child></xml>";
        Assert.assertEquals(3, new XmlParser().parse(s).size());
        Assert.assertEquals("ololo", new XmlParser().parse(s).text());
        Assert.assertEquals("", new XmlParser().parse(s).textOwn());
        s = "<xml><child/><child/><child>ololo</child>lo</xml>";
        Assert.assertEquals(4, new XmlParser().parse(s).size());
        Assert.assertEquals("ololo lo", new XmlParser().parse(s).text());
        Assert.assertEquals("lo", new XmlParser().parse(s).textOwn());


        s = "<xml>\n\t\tololo\n\t\t</xml>";
        Assert.assertEquals("ololo", new XmlParser().parse(s).text());
    }

    @Test
    public void escape() {
        Assert.assertEquals("&quot;", Node.escape("\""));
        Assert.assertEquals("&apos;", Node.escape("'"));
        Assert.assertEquals("&lt;", Node.escape("<"));
        Assert.assertEquals("&gt;", Node.escape(">"));
        Assert.assertEquals("&amp;", Node.escape("&"));
    }

    @Test
    public void unescape() {
        Assert.assertEquals("\"", Node.unescape("&quot;"));
        Assert.assertEquals("'", Node.unescape("&apos;"));
        Assert.assertEquals("<", Node.unescape("&lt;"));
        Assert.assertEquals(">", Node.unescape("&gt;"));
        Assert.assertEquals("&", Node.unescape("&amp;"));
    }

    @Test
    public void xml_comment_1() throws IOException {
        String s = "<div><!--   <comment>   --></div>";
        Node div = new XmlParser().parse(s);
        Assert.assertEquals(0, div.attributes().size());
        Assert.assertEquals(1, div.children().size());
        Assert.assertEquals(true, div.children().get(0).isComment());
        Assert.assertEquals("<!-- <comment> -->", div.children().get(0).ownText());
    }

    @Test
    public void xml_comment_2() throws IOException {
        String s = "" +
                "<div>\n" +
                "    <!--[if IE]>\n" +
                "        According to the conditional comment this is IE<br />\n" +
                "    <![endif]-->\n" +
                "</div>\n";
        Node div = new XmlParser().parse(s);
        Assert.assertEquals(0, div.attributes().size());
        Assert.assertEquals(1, div.children().size());
        Assert.assertEquals(true, div.children().get(0).isComment());
        Assert.assertEquals("" +
                "<!-- [if IE]>\n" +
                "        According to the conditional comment this is IE<br />\n" +
                "    <![endif] -->", div.children().get(0).ownText());
    }

    @Test
    public void xml_line_number_1() throws IOException {
        String s = "" +
                "<div>\n" +
                "    <b>\n" +
                "        test\n" +
                "    </b>\n" +
                "    <b>\n" +
                "\n" +
                "\n" +
                "        test_2\n" +
                "    </b>\n" +
                "    <!--   <comment>   -->\n" +
                "</div>";
        Node div = new XmlParser().parse(s);
        Assert.assertEquals(0, div.attributes().size());
        Assert.assertEquals(3, div.children().size());
        Assert.assertEquals(1, div.getLineNumber());

        Node b;
        b = div.children().get(0);
        Assert.assertEquals(1, b.children().size());
        Assert.assertEquals(2, b.getLineNumber());
        Assert.assertEquals(3, b.children().get(0).getLineNumber());

        b = div.children().get(1);
        Assert.assertEquals(1, b.children().size());
        Assert.assertEquals(5, b.getLineNumber());
        Assert.assertEquals(8, b.children().get(0).getLineNumber());

        b = div.children().get(2);
        Assert.assertEquals(true, b.isComment());
        Assert.assertEquals(10, b.getLineNumber());
    }

    @Test
    public void html_1() throws IOException {
        String s = "";
        for (File f : new File("src/test/resources/xml").listFiles()) {
            System.out.println("parsing: " + f);
            new HtmlParser().parse(f);
        }
    }

    @Test
    public void html_2() throws IOException {
        String s = "<div width=100px></div>";
        Node root = new HtmlParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals(1, div.attributes().size());
        Assert.assertEquals(0, div.children().size());
        Assert.assertEquals("100px", div.attr("width"));

        s = "<div width=100px height=50px></div>";
        root = new HtmlParser().parse(s);
        div = root.children().get(0);
        Assert.assertEquals(2, div.attributes().size());
        Assert.assertEquals(0, div.children().size());
        Assert.assertEquals("100px", div.attr("width"));
        Assert.assertEquals("50px", div.attr("height"));
    }

    @Test
    public void html_3() throws IOException {
        String s = "<div><script>\n" +
                "   var a\n" +
                "   for(var i=0; i<10;i++) {\n" +
                "       a+=i;" +
                "   }\n" +
                "</script></div>";
        Node root = new HtmlParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals(1, div.children().size());

        Node script = div.children.get(0);
        Assert.assertEquals("var a\n" +
                "   for(var i=0; i<10;i++) {\n" +
                "       a+=i;" +
                "   }\n", script.text());

    }

    @Test
    public void html_doctype() throws IOException {
        String s = "<!DOCTYPE html>";
        Node doc = new HtmlParser().parse(s);
        Assert.assertEquals(1, doc.children().size());
        Assert.assertEquals("!DOCTYPE", doc.get(0).name());
        Assert.assertEquals(1, doc.get(0).attributes().size());
        Assert.assertEquals(true, doc.get(0).attributes().containsKey("html"));
        Assert.assertEquals(null, doc.get(0).attributes().get("html"));
    }

    @Test
    public void html_doctype2() throws IOException {
        String s = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n";
        Node doc = new HtmlParser().parse(s);
        Assert.assertEquals(1, doc.children().size());
        Assert.assertEquals("!DOCTYPE", doc.get(0).name());
        Assert.assertEquals(4, doc.get(0).attributes().size());
        Assert.assertEquals(true, doc.get(0).attributes().containsKey("HTML"));
        Assert.assertEquals(null, doc.get(0).attributes().get("HTML"));
        Assert.assertEquals(true, doc.get(0).attributes().containsKey("PUBLIC"));
        Assert.assertEquals(null, doc.get(0).attributes().get("PUBLIC"));
        Assert.assertEquals(true, doc.get(0).attributes().containsKey("\"-//W3C//DTD HTML 4.01//EN\""));
        Assert.assertEquals(null, doc.get(0).attributes().get("\"-//W3C//DTD HTML 4.01//EN\""));
        Assert.assertEquals(true, doc.get(0).attributes().containsKey("\"http://www.w3.org/TR/html4/strict.dtd\""));
        Assert.assertEquals(null, doc.get(0).attributes().get("\"http://www.w3.org/TR/html4/strict.dtd\""));
    }

    @Test
    public void gsp_1() throws IOException {
        String s = "<div><g:textField name=\"${it.key}\" placeholder=\"${[].collect({it})}\"/></div>";
        Node root = new GspParser().parse(s);
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
        Node root = new GspParser().parse(s);
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

    @Test
    public void gsp_3() throws IOException {
        String s = "<div id=\"${id}\"><span>foo:</span>${foo}</div>";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals(1, div.attributes().size());
        Assert.assertEquals("${id}", div.attr("id"));

        Node span = div.children().get(0);
        Assert.assertEquals("span", span.name());
        Assert.assertEquals(1, span.children().size());
        Assert.assertEquals("foo:", span.text());

        Node foo = div.children().get(1);
        Assert.assertEquals("${foo}", foo.text());
    }

    @Test
    public void gsp_4() throws IOException {
        String s = "<div><script>\n" +
                "   var a\n" +
                "   for(var i=0; i<10;i++) {\n" +
                "       a+=i;" +
                "   }\n" +
                "</script></div>";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals(1, div.children().size());

        Node script = div.children.get(0);
        Assert.assertEquals("var a\n" +
                "   for(var i=0; i<10;i++) {\n" +
                "       a+=i;" +
                "   }\n", script.text());

    }

    @Test
    public void gsp_comment_1() throws IOException {
        String s = "" +
                "<div>\n" +
                "    %{--<p>$test</p>--}%\n" +
                "</div>\n";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(1, div.children().size());
        Assert.assertEquals(0, div.attributes().size());
        Assert.assertEquals("%{--<p>$test</p>--}%", div.children().get(0).textOwn());
    }

    @Test
    public void gsp_comment_2() throws IOException {
        String s = "" +
                "<div %{--foo=\"${bar}\"--}%>\n" +
                "    \n" +
                "</div>\n";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(0, div.children().size());
        Assert.assertEquals(0, div.attributes().size());
    }

    @Test
    public void gsp_comment_3() throws IOException {
        String s = "" +
                "<div foo=\"bar%{--_$id--}%_0\">\n" +
                "    \n" +
                "</div>\n";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(0, div.children().size());
        Assert.assertEquals(1, div.attributes().size());
        Assert.assertEquals("bar_0", div.attr("foo"));
    }

    @Test
    public void gsp_comment_4() throws IOException {
        String s = "<div>\n" +
                "    before\n" +
                "    %{--<p>text</p>--}%\n" +
                "    after\n" +
                "</div>";
        Node root = new GspParser().parse(s);
        Node div = root.children().get(0);
        Assert.assertEquals("div", div.name());
        Assert.assertEquals(3, div.children().size());
        Assert.assertEquals("before", div.get(0).text());
        Assert.assertEquals("%{--<p>text</p>--}%", div.get(1).text());
        Assert.assertEquals("after", div.get(2).text());
    }

    @Test
    public void gsp_page() throws IOException {
        String s = "<%@ page contentType=\"text/html;charset=UTF-8\" %>\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>title</title>\n" +
                "    %{-- comment! --}%\n" +
                "</head>" +
                "</html>";
        Node doc = new GspParser().parse(s);
        Assert.assertEquals(3, doc.children().size());
        Assert.assertEquals("%@", doc.get(0).name());
        Assert.assertEquals("!DOCTYPE", doc.get(1).name());
        Assert.assertEquals("html", doc.get(2).name());
    }

    @Test
    public void gsp_page2() throws IOException {
        String s = "<%@ page contentType=\"text/html;charset=UTF-8\" %>\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>title</title>\n" +
                "</head>" +
                "</html>";
        Node doc = new GspParser().parse(s);
        Assert.assertEquals(3, doc.children().size());
        Assert.assertEquals("%@", doc.get(0).name());
        Assert.assertEquals("!DOCTYPE", doc.get(1).name());
        Assert.assertEquals("html", doc.get(2).name());
    }
}
