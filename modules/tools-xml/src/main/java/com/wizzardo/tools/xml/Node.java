package com.wizzardo.tools.xml;

/**
 * @author: moxa
 * Date: 12/24/12
 */

import com.wizzardo.tools.collections.CollectionTools;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: moxa
 * Date: 12/23/12
 */
public class Node {

    protected Map<String, String> attributes;
    protected List<Node> children;
    protected String name;
    protected Node parent;

    public Node(String name) {
        this.name = name;
    }

    protected Node() {
    }

    public Node attribute(String attributeName, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap<String, String>();
        attributes.put(attributeName, value);
        return this;
    }

    public Node attr(String attributeName, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap<String, String>();
        attributes.put(attributeName, value);
        return this;
    }

    public String attribute(String attributeName) {
        if (attributes == null)
            return null;
        return attributes.get(attributeName);
    }

    public String attr(String attributeName) {
        if (attributes == null)
            return null;
        return attributes.get(attributeName);
    }

    public Set<String> attributesNames() {
        return attributes.keySet();
    }

    public Map<String, String> attributes() {
        if (attributes == null) {
            attributes = new LinkedHashMap<String, String>();
        }
        return attributes;
    }

    public List<Node> children() {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        return children;
    }

    public Node get(int i) {
        return children().get(i);
    }

    public Node first() {
        return children().get(0);
    }

    public Node last() {
        return children().get(children.size() - 1);
    }

    public boolean isEmpty() {
        return children == null || children.isEmpty();
    }

    public int level() {
        int l = 0;
        Node p = this;
        while ((p = p.parent) != null) {
            l++;
        }
        return l;
    }

    public String offset() {
        return offset("    ");
    }

    public String offset(String step) {
        StringBuilder sb = new StringBuilder();
        int level = level();
        for (int i = 0; i < level; i++) {
            sb.append(step);
        }
        return sb.toString();
    }

    public Node add(Node node) {
        if (children == null)
            children = new ArrayList<Node>();
        children.add(node);
        node.parent = this;
        return this;
    }

    public Node addText(String text) {
        return add(new TextNode(text));
    }

    public Node addComment(String text) {
        return add(new XmlComment(text));
    }

    public Node execute(CollectionTools.Closure<Void, Node> closure) {
        closure.execute(this);
        return this;
    }

    public Node parent() {
        return parent;
    }

    public String toString() {
        return toString("", new StringBuilder());
    }

    private String toString(String offset, StringBuilder sb) {
        sb.append(offset);
        sb.append(name);
        if (attributes != null) {
            String s;
            if ((s = attributes.get("id")) != null) {
                sb.append("#").append(s);
            }
            if ((s = attributes.get("class")) != null) {
                for (String clazz : s.split(" ")) {
                    sb.append(".").append(clazz);
                }
            }
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                if (attr.getKey().endsWith("id") || attr.getKey().endsWith("class"))
                    continue;
                sb.append("[").append(attr.getKey()).append("=").append(attr.getValue()).append("]");
            }
        }
        if (children != null)
            for (Node child : children)
                child.toString(offset + "\t", sb.append("\n"));
        return sb.toString();
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Node get(String path) {
        if (children == null || path == null)
            return null;
        int l = path.indexOf('/');
        if (l == -1) {
            l = path.indexOf("[");
            XPathExpression exp = null;
            if (l != -1) {
                exp = new XPathExpression(path.substring(l, path.length()));
                path = path.substring(0, l);
            }
            for (Node node : children) {
                if (node.name != null && node.name.equals(path) && (exp == null || exp.check(node)))
                    return node;
            }
        } else {
            String tag = path.substring(0, l);
            path = path.substring(l + 1);
            for (Node node : children) {
                if (node.name != null && node.name.equals(tag)) {
                    Node r = node.get(path);
                    if (r != null)
                        return r;
                }
            }
        }
        return null;
    }

    public List<Node> getAll(String path) {
        List<Node> list = new ArrayList<Node>();
        if (children == null || path == null)
            return list;
        int l = path.indexOf('/');
        if (l == -1) {
            l = path.indexOf("[");
            XPathExpression exp = null;
            if (l != -1) {
                exp = new XPathExpression(path.substring(l, path.length()));
                path = path.substring(0, l);
            }
            for (Node node : children) {
                if (node.name != null && node.name.equals(path) && (exp == null || exp.check(node)))
                    list.add(node);
            }
        } else {
            String tag = path.substring(0, l);
            path = path.substring(l + 1);
            for (Node node : children) {
                if (node.name != null && node.name.equals(tag)) {
                    List<Node> r = node.getAll(path);
                    if (r != null)
                        list.addAll(r);
                }
            }
        }
        return list;
    }

    public Node find(String path) {
        if (children == null || path == null)
            return null;
        for (Node node : children) {
            if (node.name != null && node.name.equals(path))
                return node;
            Node r = node.find(path);
            if (r != null)
                return r;
        }
        return null;
    }

    public List<Node> findAll(String path) {
        List<Node> list = new ArrayList<Node>();
        if (children == null || path == null)
            return list;
        for (Node node : children) {
            if (node.name != null && node.name.equals(path))
                list.add(node);
            List<Node> r = node.findAll(path);
            list.addAll(r);
        }
        return list;
    }

    public String text() {
        return text(true);
    }

    public String textOwn() {
        return text(false);
    }

    protected String text(boolean recursive) {
        if (children == null || children.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Node node : children) {
            if (sb.length() > 0)
                sb.append(' ');
            String inner;
            if (recursive)
                inner = node.text(recursive);
            else
                inner = node.ownText();
            if (inner != null)
                sb.append(inner);
        }
        return sb.toString();
    }

    protected String ownText() {
        return null;
    }

    public boolean hasAttr(String attr) {
        if (attributes == null)
            return false;
        return attributes.containsKey(attr);
    }

    public int size() {
        return children == null ? 0 : children.size();
    }

    private static class XPathExpression {
        private String command, result;
        private static Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
        private XPathExpression next;

        private XPathExpression(String command, String result) {
            this.command = command;
            this.result = result;
        }

        public XPathExpression(String s) {
            Matcher m = pattern.matcher(s);
            while (m.find()) {
                String[] arr = m.group(1).split("=", 2);
                String command = arr[0].trim();
                String result = arr[1].trim();
                if ((result.startsWith("\"") && result.endsWith("\"")) || (result.startsWith("'") && result.endsWith("'"))) {
                    result = result.substring(1, result.length() - 1);
                }
                if (this.command == null) {
                    this.command = command;
                    this.result = result;
                }
                if (next == null) {
                    next = new XPathExpression(command, result);
                } else {
                    XPathExpression next = this.next;
                    while (next.next != null) {
                        next = next.next;
                    }
                    next.next = new XPathExpression(command, result);
                }
            }
        }

        public boolean check(Node n) {
            boolean b = false;
            if (command.equals("text()")) {
                b = result.equals(n.text());
            } else if (command.startsWith("@")) {
                b = result.equals(n.attr(command.substring(1)));
            }
            return b && (next == null || next.check(n));
        }
    }

    public String toXML() {
        return toXML(false);
    }

    public String toXML(boolean prettyPrint) {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding='UTF-8' ?>");
        if (prettyPrint)
            sb.append("\n");
        return toXML("", sb, prettyPrint);
    }

    private String toXML(String offset, StringBuilder sb, boolean prettyPrint) {
        if (prettyPrint)
            sb.append(offset);

        if (this instanceof TextNode) {
            sb.append(text());
            if (prettyPrint)
                sb.append("\n");
        } else {
            sb.append("<").append(name);

            if (attributes != null) {
                for (Map.Entry<String, String> attr : attributes.entrySet()) {
                    sb.append(" ").append(attr.getKey()).append("=\"").append(escape(attr.getValue())).append("\"");
                }
            }
            if (children != null) {
                sb.append(">");
                if (prettyPrint)
                    sb.append("\n");

                for (Node child : children)
                    child.toXML(offset + "\t", sb, prettyPrint);

                if (prettyPrint)
                    sb.append(offset);
                sb.append("</").append(name).append(">");
            } else
                sb.append("/>");

            if (prettyPrint)
                sb.append("\n");
        }
        return sb.toString();
    }

    private String escape(String s) {
        return s
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                ;
    }
}
