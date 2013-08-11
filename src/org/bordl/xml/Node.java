package org.bordl.xml;

/**
 * @author: moxa
 * Date: 12/24/12
 */

import org.bordl.utils.io.FileUtils;

import java.io.*;
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
    private static final Set<String> anotherLanguageTags = new HashSet<String>();

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

    public Node(String name) {
        this.name = name;
    }

    protected Node() {
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
        return parse(s, html, false);
    }

    public static Node parse(String s, boolean html, boolean gsp) {
        // check first char
        s = s.trim();
        Node xml = new Node();
        if (s.startsWith("<?xml ")) {
            parse(s.toCharArray(), s.indexOf("?>") + 2, xml, html, gsp);
        } else if (html) {
            int i = 0;
            Node document = new Node("document");
            while ((i = parse(s.toCharArray(), i, xml, html, gsp) + 1) < s.length()) {
                if (xml.name == null && xml.children.size() == 1)
                    xml = xml.children.get(0);
                document.add(xml);
                xml = new Node();
            }
            if (xml.name == null && xml.children.size() == 1)
                xml = xml.children.get(0);
            document.add(xml);
            return document;
        } else {
            parse(s.toCharArray(), 0, xml, html, gsp);
        }
        return xml;
    }

    public static Node parse(File f) throws IOException {
        return parse(f, false);
    }

    public static Node parse(File f, boolean html) throws IOException {
        return parse(f, html, false);
    }

    public static Node parse(File f, boolean html, boolean gsp) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        FileInputStream in = new FileInputStream(f);
        int r;
        byte[] b = new byte[10240];
        while ((r = in.read(b)) != -1) {
            bout.write(b, 0, r);
        }
        return parse(new String(bout.toByteArray()), html, gsp);
    }

    private static int parse(char[] s, int from, Node xml, boolean html, boolean gsp) {
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
        boolean inGroovy = false;
        boolean inAnotherLanguageTag = false;
        char quote = 0, ch;
        outer:
        while (i < s.length) {
            ch = s[i];

            if (gsp && inGroovy) {
                switch (ch) {
                    case '}': {
                        sb.append('}');
                        if (!inString) {
                            inGroovy = false;
                            xml.attribute(sb.toString(), null);
                        }
                        break;
                    }
                    case '\"':
                    case '\'': {
                        if (inString) {
                            if (quote == ch && s[i - i] != '\\')
                                inString = false;
                        } else {
                            quote = ch;
                            inString = true;
                        }
                        sb.append(ch);
                        break;
                    }
                    default:
                        sb.append(s[i]);
                }
                i++;
                continue;
            }

            if (!inTag && inAnotherLanguageTag) {
                String t;
                if (ch == '>' && (t = sb.toString().trim()).endsWith("</" + xml.name)) {
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

            switch (s[i]) {
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
                    if (comment) {
                        sb.append(s[i]);
                        break;
                    }
                    if (sb.length() > 0 && !(html && inAnotherLanguageTag)) {
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
                    }
                    if (!inString && inTag) {
                        attribute = true;
                    } else if (sb.length() != 0) {
                        sb.append(' ');
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
                    }
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
                        xml.add(new TextNode(sb.toString()));
                        sb.setLength(0);
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
                case '{': {
                    if (!inString && inTag && i > 0 && s[i - 1] == '$') {
                        inGroovy = true;
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
        if (sb.length() > 0 && !(t = sb.toString()).equals(xml.name)) {
            xml.add(new TextNode(t));
            sb.setLength(0);
        }
        return i;
    }

    public static void main(String[] args) throws IOException {
//        String s = "<head>\n" +
//                "    <script src=\"video.js\" type=\"text/javascript\" charset=\"utf-8\"></script>\n" +
//                "\n" +
//                "    <script type=\"text/javascript\">\n" +
//                "        VideoJS.setupAllWhenReady();\n" +
//                "    </script>" +
//                "</head>";
//
        String s = FileUtils.text("/home/moxa/IdeaProjects/WVC/views/UploadController/index_temp.gsp");
        System.out.println(Node.parse(s, true, true));
    }
}
