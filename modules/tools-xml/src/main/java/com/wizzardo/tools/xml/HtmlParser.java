package com.wizzardo.tools.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wizzardo on 20.07.15.
 */
public class HtmlParser<T extends HtmlParser.HtmlParserContext> extends XmlParser<T> {
    protected static final Set<String> selfClosedTags = new HashSet<String>();
    protected static final Set<String> anotherLanguageTags = new HashSet<String>();

    static {
        anotherLanguageTags.add("script");
        anotherLanguageTags.add("style");

        selfClosedTags.add("area");
        selfClosedTags.add("base");
        selfClosedTags.add("br");
        selfClosedTags.add("col");
        selfClosedTags.add("command");
        selfClosedTags.add("embed");
        selfClosedTags.add("hr");
        selfClosedTags.add("img");
        selfClosedTags.add("input");
        selfClosedTags.add("keygen");
        selfClosedTags.add("link");
        selfClosedTags.add("meta");
        selfClosedTags.add("param");
        selfClosedTags.add("source");
        selfClosedTags.add("track");
        selfClosedTags.add("wbr");
        selfClosedTags.add("!doctype");
    }

    public class HtmlParserContext extends XmlParser.XmlParserContext {
        protected boolean inAnotherLanguageTag = false;

        @Override
        protected boolean onChar(char[] s, Node xml) {
            if (!inTag && inAnotherLanguageTag) {
                String t;
                if (ch == '>' && (t = trimRight(sb).toString().trim()).endsWith("</" + xml.name)) {
                    xml.add(new TextNode(t.substring(0, t.length() - 2 - xml.name.length())));
                    sb.setLength(0);
                    finished = true;
                    return true;
                }
                sb.append(ch);
                i++;
                return true;
            }

            if (checkClose && s[i] != '/' && inAnotherLanguageTag) {
                sb.append('<').append(s[i]);
                i++;
                checkClose = false;
                return true;
            }
            return false;
        }

        @Override
        protected boolean onGreaterThanSign(char[] s, Node xml) {
            if (inString || (inAnotherLanguageTag && !inTag && !sb.toString().equals(xml.name))) {
                sb.append('>');
                return false;
            }
            return super.onGreaterThanSign(s, xml) || selfClosedTags.contains(xml.name().toLowerCase());

        }

        @Override
        protected void onLessThanSign(char[] s, Node xml) {
            if (comment || inString) {
                sb.append(s[i]);
                return;
            }
            if (sb.length() > 0 && !inAnotherLanguageTag) {
                xml.add(addLineNumber(new TextNode(trimRight(sb).toString())));
                sb.setLength(0);
            }
            if (xml.name() != null)
                checkClose = true;
            else
                inTag = true;
            name = true;
        }

        protected void onTagName(String name) {
            inAnotherLanguageTag = anotherLanguageTags.contains(name);
        }

    }

    @Override
    protected T createContext() {
        return (T) new HtmlParserContext();
    }

    public Node parse(Node xml, String s) {
        s = s.trim();
        int i;
        T context = createContext();
        xml.name("document");
        Node child = new Node();
        while ((i = context.parse(s.toCharArray(), child) + 1) < s.length()) {
            if (child.name == null && child.children.size() == 1)
                child = child.children.get(0);
            xml.add(child);
            child = new Node();
            context = createContext();
            context.i = i;
        }
        if (child.name == null && child.children.size() == 1)
            child = child.children.get(0);
        xml.add(child);
        return xml;
    }

}
