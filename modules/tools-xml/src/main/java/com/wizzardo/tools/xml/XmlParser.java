package com.wizzardo.tools.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by wizzardo on 20.07.15.
 */
public class XmlParser<T extends XmlParser.XmlParserContext> {

    public class XmlParserContext {
        protected int i;
        protected StringBuilder sb = new StringBuilder();
        protected boolean inString = false;
        protected boolean name = false;
        protected boolean end = false;
        protected boolean attribute = false;
        protected String attributeName = null;
        protected boolean checkClose = false;
        protected boolean comment = false;
        protected boolean inTag = false;
        protected char ch;
        protected boolean finished = false;
        protected int lineNumber = 1;
        protected int textStartLineNumber;

        protected boolean onChar(char[] s, Node xml) {
            return false;
        }

        protected int parse(char[] s, Node xml) {
            outer:
            while (i < s.length && !finished) {
                ch = s[i];
                if (ch == '\n')
                    lineNumber++;

                if (onChar(s, xml))
                    continue;

                switch (ch) {
                    case '"': {
                        onQuotationMark(s, xml);
                        break;
                    }
                    case '<': {
                        onLessThanSign(s, xml);
                        break;
                    }
                    case '\r': {
                        onCarriageReturnSign(s, xml);
                        break;
                    }
                    case '\n': {
                        onLineFeedSign(s, xml);
                        break;
                    }
                    case '\t': {
                        onTabSign(s, xml);
                        break;
                    }
                    case ' ': {
                        onSpaceSign(s, xml);
                        break;
                    }
                    case '=': {
                        onEqualsSign(s);
                        break;
                    }
                    case '>': {
                        if (onGreaterThanSign(s, xml))
                            break outer;
                        break;
                    }
                    case '/': {
                        onSlash(s, xml);
                        break;
                    }
                    case '{': {
                        onCurlyBracketOpen(s, xml);
                        break;
                    }
                    default: {
                        processDefault(s, xml);
                        break;
                    }
                }
                i++;
            }
            if (attributeName != null && attributeName.length() > 0) {
                xml.attribute(attributeName, "");
            }
            String t;
            if (sb.length() > 0 && !(t = trimRight(sb).toString()).equals(xml.name)) {
                xml.add(addLineNumber(new TextNode(t)));
                sb.setLength(0);
            }
            return i;
        }

        protected void processDefault(char[] s, Node xml) {
            if (checkClose && !end) {
                if (s[i] == '!') {
                    comment = true;
                    inTag = false;
                } else {
                    Node child = new Node();
                    addLineNumber(child);
                    T context = createContext();
                    context.lineNumber = lineNumber;
                    context.i = i - 1;
                    i = context.parse(s, child);
                    lineNumber = context.lineNumber;
                    xml.add(child);
                }
                checkClose = false;
                name = false;
            } else {
                if (sb.length() == 0)
                    textStartLineNumber = lineNumber;
                sb.append(s[i]);
            }
        }

        protected void onCarriageReturnSign(char[] s, Node xml) {
            onSpaceSign(s, xml);
        }

        protected void onLineFeedSign(char[] s, Node xml) {
            onSpaceSign(s, xml);
        }

        protected void onTabSign(char[] s, Node xml) {
            onSpaceSign(s, xml);
        }

        protected void onSpaceSign(char[] s, Node xml) {
            if (comment) {
                sb.append(s[i]);
                return;
            }
            if (name) {
                name = false;
                if (!end) {
                    xml.name(sb.toString());
                    onTagName(xml.name);
                    sb.setLength(0);
                    attribute = true;
                }
            } else if (attribute) {
                attributeName = sb.toString().trim();
                if (attributeName.length() > 0) {
                    xml.attribute(attributeName, "");
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
        }

        protected void onEqualsSign(char[] s) {
            if (comment || !inTag) {
                sb.append(s[i]);
                return;
            }
            if (attribute) {
                attributeName = sb.toString().trim();
                sb.setLength(0);
                attribute = false;
            } else if (inString) {
                sb.append('=');
            }
        }

        protected boolean onGreaterThanSign(char[] s, Node xml) {
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
                    xml.add(addLineNumber(new XmlComment(sb.substring(2, sb.length() - 2).trim())));
                    sb.setLength(0);
                    comment = false;
                } else {
                    sb.append('>');
                }
                return false;
            }
            inTag = false;
            if (name) {
                name = false;
                if (!end) {
                    xml.name(sb.toString());
                    onTagName(xml.name);
                    sb.setLength(0);
                } else {
                    if (xml.name() == null) {
                        xml.name(sb.toString());
                        onTagName(xml.name);
                        sb.setLength(0);
                    } else if (!sb.toString().equals(xml.name))
                        throw new IllegalStateException("illegal close tag: " + sb.toString() + ". close tag must be: " + xml.name());
                }
            }
            return end;
        }

        protected void onTagName(String name) {
        }

        protected void onCurlyBracketOpen(char[] s, Node xml) {
            sb.append('{');
        }

        protected void onSlash(char[] s, Node xml) {
            if (comment) {
                sb.append(s[i]);
                return;
            }
            if (!checkClose && (inString || !inTag)) {
                sb.append('/');
                return;
            }
            if (attribute) {
                attributeName = sb.toString().trim();
                sb.setLength(0);
                attribute = false;
            }
            if (checkClose && sb.length() > 0) {
                xml.add(addLineNumber(new TextNode(trimRight(sb).toString())));
                sb.setLength(0);
            }
            end = true;
            checkClose = false;
        }

        protected void onLessThanSign(char[] s, Node xml) {
            if (comment || inString) {
                sb.append(s[i]);
                return;
            }
            if (sb.length() > 0) {
                xml.add(addLineNumber(new TextNode(trimRight(sb).toString())));
                sb.setLength(0);
            }
            if (xml.name() != null)
                checkClose = true;
            else
                inTag = true;
            name = true;
        }

        protected void onQuotationMark(char[] s, Node xml) {
            if (comment || !inTag) {
                sb.append(s[i]);
                return;
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
        }

        protected Node addLineNumber(Node node) {
            node.lineNumber = lineNumber;
            return node;
        }

        protected Node addLineNumber(TextNode node) {
            node.lineNumber = textStartLineNumber;
            return node;
        }
    }

    public Node parse(String s) {
        return parse(new Node(), s);
    }

    public Node parse(File f) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        int r;
        byte[] b = new byte[10240];
        while ((r = in.read(b)) != -1) {
            bout.write(b, 0, r);
        }
        return parse(new Node(), new String(bout.toByteArray()));
    }

    public Node parse(Node xml, String s) {
        // check first char
        s = s.trim();
        xml.lineNumber = 1;
        T context = createContext();
        if (s.startsWith("<?xml ")) {
            context.i = s.indexOf("?>") + 2;
            context.parse(s.toCharArray(), xml);
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
