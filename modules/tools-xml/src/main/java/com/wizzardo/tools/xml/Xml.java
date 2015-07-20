package com.wizzardo.tools.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by wizzardo on 20.07.15.
 */
public class Xml extends Node {

    public Xml(String s) {
        this(s, false);
    }

    public Xml(String s, boolean html) {
        this(s, html, false);
    }

    public Xml(File f) throws IOException {
        this(f, false);
    }

    public Xml(File f, boolean html) throws IOException {
        this(f, html, false);
    }

    public Xml(File f, boolean html, boolean gsp) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        int r;
        byte[] b = new byte[10240];
        while ((r = in.read(b)) != -1) {
            bout.write(b, 0, r);
        }
        parse(this, new String(bout.toByteArray()), html, gsp);
    }

    public Xml(String s, boolean html, boolean gsp) {
        parse(this, s, html, gsp);
    }

    public Node parse(Node xml, String s, boolean html, boolean gsp) {
        // check first char
        s = s.trim();
        if (s.startsWith("<?xml ")) {
            parse(s.toCharArray(), s.indexOf("?>") + 2, xml, html, gsp);
        } else if (html) {
            int i = 0;
            xml.name("document");
            Node child = new Node();
            while ((i = parse(s.toCharArray(), i, child, html, gsp) + 1) < s.length()) {
                if (child.name == null && child.children.size() == 1)
                    child = child.children.get(0);
                xml.add(child);
                child = new Node();
            }
            if (child.name == null && child.children.size() == 1)
                child = child.children.get(0);
            xml.add(child);
        } else {
            parse(s.toCharArray(), 0, xml, html, gsp);
        }
        return xml;
    }

    protected int parse(char[] s, int from, Node xml, boolean html, boolean gsp) {
        int i = from;
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean inStringInGroovy = false;
        boolean name = false;
        boolean end = false;
        boolean attribute = false;
        String attributeName = null;
        boolean checkClose = false;
        boolean comment = false;
        boolean inTag = false;
        boolean inGroovy = false;
        int brackets = 0;
        boolean inAnotherLanguageTag = false;
        char quote = 0, ch;
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
                            i = parse(s, i - 1, child, html, gsp);
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
