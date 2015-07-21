package com.wizzardo.tools.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wizzardo on 20.07.15.
 */
public class XmlParser<T extends XmlParser.XmlParserContext> {

    public class XmlParserContext {
        protected int i;
        protected StringBuilder sb = new StringBuilder();
        protected boolean inString = false;
        protected boolean inStringInGroovy = false;
        protected boolean name = false;
        protected boolean end = false;
        protected boolean attribute = false;
        protected String attributeName = null;
        protected boolean checkClose = false;
        protected boolean comment = false;
        protected boolean inTag = false;
        protected boolean inGroovy = false;
        protected boolean gsp = false;
        protected boolean html = false;
        protected int brackets = 0;
        protected boolean inAnotherLanguageTag = false;
        protected char quote = 0, ch;

        protected int parse(char[] s, Node xml) {
            outer:
            while (i < s.length) {
                ch = s[i];

                if (gsp && inGroovy) {
                    switch (ch) {
                        case '}': {
                            sb.append('}');
                            if (!inStringInGroovy) {
                                brackets--;
                                inGroovy = brackets != 0;
                                if (!inGroovy && !inString && inTag) {
                                    xml.attribute(sb.toString(), null);
                                    sb.setLength(0);
                                }
                            }
                            break;
                        }
                        case '\"':
                        case '\'': {
                            if (inStringInGroovy) {
                                if (quote == ch && s[i - 1] != '\\')
                                    inStringInGroovy = false;
                            } else {
                                quote = ch;
                                inStringInGroovy = true;
                            }
                            sb.append(ch);
                            break;
                        }
                        case '{': {
                            sb.append(ch);
                            if (!inStringInGroovy) {
                                brackets++;
                            }
                            break;
                        }
                        default:
                            sb.append(ch);
                    }
                    i++;
                    continue;
                }

                if (!inTag && inAnotherLanguageTag) {
                    String t;
                    if (ch == '>' && (t = trimRight(sb).toString().trim()).endsWith("</" + xml.name)) {
                        xml.add(new TextNode(t.substring(0, t.length() - 2 - xml.name.length())));
                        sb.setLength(0);
                        break outer;
                    }
                    sb.append(ch);
                    i++;
                    continue;
                }

                if (checkClose && html && s[i] != '/' && inAnotherLanguageTag) {
                    sb.append('<').append(s[i]);
                    i++;
                    checkClose = false;
                    continue;
                }

                switch (ch) {
                    case '"': {
                        if (comment || !inTag) {
                            sb.append(s[i]);
                            break;
                        }
                        boolean switchInString = (i == 0 || s[i - 1] != '\\');
                        if (!switchInString && inString) {
                            sb.append('"');
                        }
                        if (switchInString) {
                            inString = !inString;
                        }
                        if (!inString) {
                            xml.attribute(attributeName, sb.toString());
                            attributeName = null;
                            sb.setLength(0);
                            attribute = false;
                        }
                        break;
                    }
                    case '<': {
                        if (comment || inString) {
                            sb.append(s[i]);
                            break;
                        }
                        if (sb.length() > 0 && !(html && inAnotherLanguageTag)) {
                            xml.add(new TextNode(trimRight(sb).toString()));
                            sb.setLength(0);
                        }
                        if (xml.name() != null)
                            checkClose = true;
                        else
                            inTag = true;
                        name = true;
                        break;
                    }
                    case '\r':
                    case '\n':
                    case '\t':
                    case ' ': {
                        if (comment) {
                            sb.append(s[i]);
                            break;
                        }
                        if (name) {
                            name = false;
                            if (!end) {
                                xml.name(sb.toString());
                                inAnotherLanguageTag = anotherLanguageTags.contains(xml.name);
                                sb.setLength(0);
                                attribute = true;
                            }
                        } else if (attribute) {
                            attributeName = sb.toString().trim();
                            if (attributeName.length() > 0) {
                                xml.attribute(attributeName, null);
                            }
                            sb.setLength(0);
                            attribute = false;
                        } else if (inTag && !inString && sb.length() > 0 && attributeName != null && !attributeName.isEmpty()) {
                            xml.attribute(attributeName, sb.toString());
                            sb.setLength(0);
                            attributeName = null;
                            attribute = true;
                        }
                        if (!inString && inTag) {
                            attribute = true;
                        } else if (sb.length() != 0) {
                            sb.append(s[i]);
                        }
                        break;
                    }
                    case '=': {
                        if (comment || !inTag) {
                            sb.append(s[i]);
                            break;
                        }
                        if (attribute) {
                            attributeName = sb.toString().trim();
                            sb.setLength(0);
                            attribute = false;
                        } else if (inString) {
                            sb.append('=');
                        }
                        break;
                    }
                    case '>': {
//                    if ("script".equals(xml.name)) {
//                        System.out.println();
//                    }
                        if (html && (inString || (inAnotherLanguageTag && !inTag && !sb.toString().equals(xml.name)))) {
                            sb.append('>');
                            break;
                        }

                        if (attribute) {
                            attributeName = sb.toString().trim();
                            sb.setLength(0);
                        } else if (inTag && sb.length() > 0 && attributeName != null && !attributeName.isEmpty()) {
                            xml.attribute(attributeName, sb.toString());
                            sb.setLength(0);
                            attributeName = null;
                        }
                        attribute = false;
                        if (comment) {
                            if (sb.charAt(sb.length() - 1) == '-' && sb.charAt(sb.length() - 2) == '-') {
                                xml.add(new XmlComment(sb.substring(2, sb.length() - 2).trim()));
                                sb.setLength(0);
                                comment = false;
                            } else {
                                sb.append('>');
                            }
                            break;
                        }
                        inTag = false;
                        if (name) {
                            name = false;
                            if (!end) {
                                xml.name(sb.toString());
                                inAnotherLanguageTag = anotherLanguageTags.contains(xml.name);
                                sb.setLength(0);
                            } else {
                                if (xml.name() == null) {
                                    xml.name(sb.toString());
                                    inAnotherLanguageTag = anotherLanguageTags.contains(xml.name);
                                    sb.setLength(0);
                                } else if (!sb.toString().equals(xml.name))
                                    throw new IllegalStateException("illegal close tag: " + sb.toString() + ". close tag must be: " + xml.name());
                            }
                        }
//                    if (!end && xml.name == null) {
//                        System.out.println();
//                    }
                        if (end) {
                            break outer;
                        } else if (html && selfClosedTags.contains(xml.name().toLowerCase())) {
                            break outer;
                        }
                        break;
                    }
                    case '/': {
                        if (comment) {
                            sb.append(s[i]);
                            break;
                        }
                        if (!checkClose && (inString || !inTag)) {
                            sb.append('/');
                            break;
                        }
                        if (attribute) {
                            attributeName = sb.toString().trim();
                            sb.setLength(0);
                            attribute = false;
                        }
                        if (checkClose && sb.length() > 0) {
                            xml.add(new TextNode(trimRight(sb).toString()));
                            sb.setLength(0);
                        }
                        end = true;
                        checkClose = false;
                        break;
                    }
                    case '{': {
                        if (i > 0 && s[i - 1] == '$') {
                            inGroovy = true;
                            brackets++;
                        }
                        sb.append('{');
                        break;
                    }
                    default: {
                        if (checkClose && !end) {
                            if (s[i] == '!') {
                                comment = true;
                                inTag = false;
                            } else {
                                Node child = new Node();
                                T context = createContext();
                                context.i = i - 1;
                                context.html = html;
                                context.gsp = gsp;
                                i = context.parse(s, child);
                                xml.add(child);
                            }
                            checkClose = false;
                            name = false;
                        } else
                            sb.append(s[i]);
                        break;
                    }
                }
                i++;
            }
            if (attributeName != null && attributeName.length() > 0) {
                xml.attribute(attributeName, null);
            }
            String t;
            if (sb.length() > 0 && !(t = trimRight(sb).toString()).equals(xml.name)) {
                xml.add(new TextNode(t));
                sb.setLength(0);
            }
            return i;
        }

    }

    protected static final Set<String> selfClosedTags = new HashSet<String>();
    protected static final Set<String> anotherLanguageTags = new HashSet<String>();

    static {
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

        anotherLanguageTags.add("script");
        anotherLanguageTags.add("style");
    }

    public Node parse(String s) {
        return parse(s, false);
    }

    public Node parse(String s, boolean html) {
        return parse(s, html, false);
    }

    public Node parse(File f) throws IOException {
        return parse(f, false);
    }

    public Node parse(File f, boolean html) throws IOException {
        return parse(f, html, false);
    }

    public Node parse(File f, boolean html, boolean gsp) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        int r;
        byte[] b = new byte[10240];
        while ((r = in.read(b)) != -1) {
            bout.write(b, 0, r);
        }
        return parse(new Node(), new String(bout.toByteArray()), html, gsp);
    }

    public Node parse(String s, boolean html, boolean gsp) {
        return parse(new Node(), s, html, gsp);
    }

    public Node parse(Node xml, String s, boolean html, boolean gsp) {
        // check first char
        s = s.trim();
        T context = createContext();
        context.html = html;
        context.gsp = gsp;
        if (s.startsWith("<?xml ")) {
            context.i = s.indexOf("?>") + 2;
            context.parse(s.toCharArray(), xml);
        } else if (html) {
            int i;
            xml.name("document");
            Node child = new Node();
            while ((i = context.parse(s.toCharArray(), child) + 1) < s.length()) {
                if (child.name == null && child.children.size() == 1)
                    child = child.children.get(0);
                xml.add(child);
                child = new Node();
                context = createContext();
                context.i = i;
                context.html = html;
                context.gsp = gsp;
            }
            if (child.name == null && child.children.size() == 1)
                child = child.children.get(0);
            xml.add(child);
        } else {
            context.parse(s.toCharArray(), xml);
        }
        return xml;
    }

    protected T createContext() {
        return (T) new XmlParserContext();
    }

    protected static StringBuilder trimRight(StringBuilder sb) {
        int l = sb.length();
        while (l > 0 && sb.charAt(l - 1) <= ' ') {
            l--;
        }
        if (l != sb.length())
            sb.setLength(l);
        return sb;
    }
}
