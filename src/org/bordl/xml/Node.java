package org.bordl.xml;

/**
 * @author: moxa
 * Date: 12/24/12
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

    private static final Set<String> selfClosedTags = new HashSet<String>();

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
    }

    public void attribute(String attributeName, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap<String, String>();
        attributes.put(attributeName, value);
    }

    public void attr(String attributeName, String value) {
        if (attributes == null)
            attributes = new LinkedHashMap<String, String>();
        attributes.put(attributeName, value);
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

    public void add(Node node) {
        if (children == null)
            children = new ArrayList<Node>();
        children.add(node);
        node.parent = this;
    }

    public Node parent() {
        return parent;
    }

    public String toString() {
        return "node " + name + " attributes: " + attributes + " children: " + children;
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

    public static class TextNode extends Node {
        protected String text;

        public TextNode(String text) {
            this.text = text;
        }

        public String toString() {
            return "textNode: " + text;
        }

        protected String text(boolean recursive) {
            return text;
        }

        protected String ownText() {
            return text;
        }
    }

    public static class CommentNode extends TextNode {

        public CommentNode(String text) {
            super(text);
        }

        public String toString() {
            return "commentNode: " + text;
        }

        protected String text(boolean recursive) {
            return "<!-- " + text + " -->";
        }

        protected String ownText() {
            return "<!-- " + text + " -->";
        }
    }

    public static Node parse(String s) {
        return parse(s, false);
    }

    public static Node parse(String s, boolean html) {
        // check first char
        s = s.trim();
        Node xml = new Node();
        if (s.startsWith("<?xml ")) {
            parse(s.toCharArray(), s.indexOf("?>") + 2, xml, html);
        } else {
            parse(s.toCharArray(), 0, xml, html);
        }
        return xml;
    }

    public static Node parse(File f) throws IOException {
        return parse(f, false);
    }

    public static Node parse(File f, boolean html) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        int r;
        byte[] b = new byte[10240];
        while ((r = in.read(b)) != -1) {
            bout.write(b, 0, r);
        }
        return parse(new String(bout.toByteArray()), html);
    }

    private static int parse(char[] s, int from, Node xml, boolean html) {
        int i = from;
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean name = false;
        boolean end = false;
        boolean attribute = false;
        String attributeName = null;
        boolean checkClose = false;
        boolean comment = false;
        boolean inTag = false;
        outer:
        while (i < s.length) {
            switch (s[i]) {
                case '"': {
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    inString = sb.toString().trim().length() == 0;
                    if (!inString) {
                        xml.attribute(attributeName.trim(), sb.toString());
                        sb.setLength(0);
                        attribute = false;
                    }
                    break;
                }
                case '<': {
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    if (sb.length() > 0) {
                        xml.add(new TextNode(sb.toString()));
                        sb.setLength(0);
                    }
                    if (xml.name() != null)
                        checkClose = true;
                    name = true;
                    inTag = true;
                    break;
                }
                case ' ': {
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    if (name) {
                        name = false;
                        if (!end) {
                            xml.name(sb.toString());
                            sb.setLength(0);
                            attribute = true;
                        }
                    } else if (attribute) {
                        attributeName = sb.toString();
                        sb.setLength(0);
                        attribute = false;
                    }
                    if (!inString && inTag) {
                        attribute = true;
                    } else if (sb.length() != 0) {
                        sb.append(' ');
                    }
                    break;
                }
                case '=': {
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    if (attribute) {
                        attributeName = sb.toString();
                        sb.setLength(0);
                        attribute = false;
                    } else if (inString) {
                        sb.append('=');
                    }
                    break;
                }
                case '>': {
                    attribute = false;
                    if (comment) {
                        if (sb.charAt(sb.length() - 1) == '-' && sb.charAt(sb.length() - 2) == '-') {
                            xml.add(new CommentNode(sb.substring(2, sb.length() - 2).trim()));
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
                            sb.setLength(0);
                            if (html && selfClosedTags.contains(xml.name().toLowerCase())) {
                                break outer;
                            }
                        } else {
                            if (xml.name() == null) {
                                xml.name(sb.toString());
                                sb.setLength(0);
                            } else if (!sb.toString().equals(xml.name()))
                                throw new IllegalStateException("illegal close tag: " + sb.toString() + ". close tag must be: " + xml.name());
                        }
                    }
                    if (end) {
                        break outer;
                    }
                    break;
                }
                case '/': {
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    if (inString) {
                        sb.append('/');
                        break;
                    }
                    end = true;
                    checkClose = false;
                    break;
                }
                case '\n': {
                    if (sb.length() != 0) {
                        sb.append('\n');
                    }
                    break;
                }
                default: {
                    if (checkClose && !end) {
                        if (s[i] == '!') {
                            comment = true;
                            inTag = false;
                        } else {
                            Node child = new Node();
                            i = parse(s, i - 2, child, html);
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
        return i;
    }
}
