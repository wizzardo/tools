package org.bordl.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 *
 * @author moxa
 */
public class Node {

    private HashMap<String, String> attributes;
    private boolean ignoreCase;
    private String name;
    private LinkedList<Node> innerNodes;
    private Node parent;

    public static class TextNode extends Node {

        private String text;

        public TextNode(String text) {
            this.text = text;
        }

        @Override
        public String toString(int level) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < level; i++) {
                s.append("\t");
            }
            s.append(toXMLEscapedString(text)).append("\n");
            return s.toString();
        }

        @Override
        public String getText() {
            return text;
        }
    }

    private Node() {
    }

    public Node(String name) {
        this.name = name;
        innerNodes = new LinkedList<Node>();
        attributes = new HashMap<String, String>();
    }

    public Node(String name, String text) {
        this(name);
        innerNodes.add(new TextNode(text));
    }

    public Node(String xml, boolean ignoreCase) throws XmlParseException {
        this.ignoreCase = ignoreCase;
        int start = 0;
        boolean data = false;
        while (!data && (start = xml.indexOf("<", start)) >= 0) {
            start++;
            if (xml.charAt(start) != '?') {
                data = true;
            }
        }
        parse(xml.substring(start - 1));
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public LinkedList<Node> getInnerNodes() {
        return innerNodes;
    }

    public void addChild(Node child) {
        if (innerNodes == null) {
            return;
        }
        innerNodes.add(child);
        child.setParent(this);
    }

    public Node removeLastChild() {
        return innerNodes.pollLast();
    }

    public Node removeFirstChild() {
        return innerNodes.pollFirst();
    }

    public Node findChildWithNameEquals(String name, boolean recursive) {
        if (innerNodes == null) {
            return null;
        }
        Node find = null;
        if (!innerNodes.isEmpty()) {
            for (Node n : innerNodes) {
                if (name.equals(n.getName())) {
                    find = n;
                    break;
                }
            }
        }
        if (recursive && find == null) {
            for (Node n : innerNodes) {
                Node temp = n.findChildWithNameEquals(name, recursive);
                if (temp != null) {
                    find = temp;
                    break;
                }
            }
        }
        return find;
    }

    public LinkedList<Node> findChildsWithNameEquals(String name, boolean recursive) {
        LinkedList<Node> found = new LinkedList<Node>();
        if (innerNodes == null) {
            return found;
        }
        if (!innerNodes.isEmpty()) {
            for (Node n : innerNodes) {
                if (n.getName().equals(name)) {
                    found.add(n);
                }
            }
        }
        if (recursive) {
            for (Node n : innerNodes) {
                found.addAll(n.findChildsWithNameEquals(name, recursive));
            }
        }
        return found;
    }

    public Node getNodeWithXPath(String path) {
        Node find = null;
        String[] names = path.split("/");
        if (names.length >= 2 && names[1].equals(name)) {
            find = this;
            for (int i = 2; i < names.length && find != null; i++) {
                find = find.findChildWithNameEquals(names[i], false);
            }
        }
        return find;
    }

    public Set<String> getAttributeNames() {
        if (innerNodes == null) {
            return null;
        }
        return attributes.keySet();
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setAttribute(String key, String value) {
        if (innerNodes == null) {
            return;
        }
        attributes.put(key, value);
    }

    public int getInnerNodesCount() {
        return innerNodes.size();
    }

    public boolean containsAttribute(String key) {
        if (innerNodes == null) {
            return false;
        }
        return attributes.containsKey(key);
    }

    private void parse(String s) throws XmlParseException {
        int k1 = s.indexOf(">"), k2 = s.indexOf("/"), k3 = s.indexOf(" ");
        if (k3 > 0 && k3 < k1) {
            setName(s.substring(1, k3));
        } else if (k1 < k2) {
            setName(s.substring(1, k1));
        } else {
            setName(s.substring(1, k2));
        }
        innerNodes = new LinkedList<Node>();
        int tagEnds = s.indexOf(">") + 1, temp;
        String tag = s.substring(s.indexOf("<"), tagEnds);
        attributes = parseAttributes(tag, ignoreCase);
        int level = 1;
        temp = tagEnds;
        int lastInnerTextStart = -1;
        if (s.charAt(tagEnds - 2) != '/') {
            int innerTagStart = 0, innerTagEnd = -1;
            while (level > 0 && (temp = s.indexOf("<", temp)) > 0) {
                if (innerTagEnd != -1 && level == 1) {
                    lastInnerTextStart = innerTagEnd;
                    String innerText = s.substring(innerTagEnd, temp).trim();
                    if (innerText.length() > 0) {
                        innerNodes.add(new TextNode(fromXMLEscapedString(innerText)));
                    }
                }
                temp++;
                if (s.charAt(temp) == '/') {//close tag
                    level--;
                    if (level == 1) {
                        int innerStart = Math.max(tagEnds, innerTagEnd);
                        innerTagEnd = s.indexOf(">", temp) + 1;
                        if (innerStart != lastInnerTextStart) {
                            String innerText = s.substring(innerStart, innerTagStart).trim();
                            if (innerText.length() > 0) {
                                innerNodes.add(new TextNode(fromXMLEscapedString(innerText)));
                            }
                        }
                        addChild(new Node(s.substring(innerTagStart, innerTagEnd), ignoreCase));
                    }
                } else {// new Node start
                    level++;
                    if (level > 1) {
                        int tt = temp - 1;
                        int t = s.indexOf(">", tt) - 1;
                        if (s.charAt(t) == '/') {// tag without inner data
                            level--;
                            if (level == 1) {
                                addChild(new Node(s.substring(tt, t + 2), ignoreCase));
                            }
                        } else {
                            if (level == 2) {
                                innerTagStart = tt;
                            }
                        }
                    }
                }
            }
            if (innerNodes.isEmpty()) {
                String innerText = s.substring(tagEnds, temp - 1).trim();
                if (innerText.length() > 0) {
                    innerNodes.add(new TextNode(fromXMLEscapedString(innerText)));
                }
            }
            tagEnds = s.indexOf(">", temp);
            if (!s.substring(temp + 1, tagEnds).equals(name)) {
                System.out.println((s.substring(temp + 1, tagEnds)) + " != " + name);
                throw new XmlParseException("wrong close tag");
            }
        }
    }

    public void serialize(OutputStream out, String charset) throws UnsupportedEncodingException, IOException {
        String header = "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n";
        out.write(header.getBytes(charset));
        out.write(toString().getBytes(charset));
        out.close();
    }

    public String serialize(String charset) {
        return "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n" + toString();
    }

    protected String toString(int level) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < level; i++) {
            s.append("\t");
        }
        s.append("<");
        s.append(name);
        for (String attribute : attributes.keySet()) {
            s.append(" ");
            s.append(attribute);
            s.append("=\"");
            s.append(toXMLEscapedString(attributes.get(attribute)));
            s.append("\"");
        }
        if (innerNodes.isEmpty()) {
            s.append("/>\n");
        } else {
            s.append(">\n");
            for (Node inner : innerNodes) {
                s.append(inner.toString(level + 1));
            }
            for (int i = 0; i < level; i++) {
                s.append("\t");
            }
            s.append("</");
            s.append(name);
            s.append(">");
            if (level > 0) {
                s.append("\n");
            }
        }
        return s.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String getAttribute(String key) {
        if (ignoreCase) {
            return attributes.get(key.toLowerCase());
        } else {
            return attributes.get(key);
        }
    }

    private HashMap<String, String> parseAttributes(String node, boolean ignoreCase) {
        HashMap<String, String> atts = new HashMap<String, String>();
        if (node.contains(" ")) {
            String temp = node.substring(node.indexOf(" ") + 1);
            String[] keys = temp.split("=[ ]*\"[^\"]*\"");
            String[] values = temp.split("[ ]*[\\w]*[ ]*=\"");
            for (int i = 0; i < keys.length - 1; i++) {
                values[i + 1] = values[i + 1].substring(0, values[i + 1].lastIndexOf("\""));
                if (ignoreCase) {
                    atts.put(keys[i].trim().toLowerCase(), fromXMLEscapedString(values[i + 1]));
                } else {
                    atts.put(keys[i].trim(), fromXMLEscapedString(values[i + 1]));
                }
            }
        }
        return atts;
    }

    public static String fromXMLEscapedString(String s) {
        return s.replaceAll("&quot;", "\"").replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&apos;", "'");
    }

    public static String toXMLEscapedString(String s) {
        return s.replaceAll("\"", "&quot;").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (Node n : innerNodes) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(n.getText());
        }
        return sb.toString();
    }

    public String getTextOwn() {
        StringBuilder sb = new StringBuilder();
        for (Node n : innerNodes) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            if (n instanceof TextNode) {
                sb.append(n.getText());
            }
        }
        return sb.toString();
    }
}
