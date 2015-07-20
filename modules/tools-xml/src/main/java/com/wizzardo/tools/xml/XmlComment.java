package com.wizzardo.tools.xml;

/**
 * Created by wizzardo on 20.07.15.
 */
public class XmlComment extends TextNode {

    public XmlComment(String text) {
        super(text);
    }

    public String toString() {
        return "comment: " + text;
    }

    protected String text(boolean recursive) {
        return "<!-- " + text + " -->";
    }

    protected String ownText() {
        return "<!-- " + text + " -->";
    }
}
