package com.wizzardo.tools.xml;

/**
 * Created by wizzardo on 20.07.15.
 */
public class TextNode extends Node {
    protected String text;

    public TextNode(String text) {
        this.text = text;
    }

    public String toString() {
        return "textNode: " + text;
    }

    protected String text(boolean recursive) {
        return ownText();
    }

    @Override
    protected void text(boolean recursive, StringBuilder sb) {
        sb.append(ownText());
    }

    protected String ownText() {
        return text;
    }
}
