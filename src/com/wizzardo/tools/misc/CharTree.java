package com.wizzardo.tools.misc;

/**
 * @author: wizzardo
 * Date: 7/30/14
 */
public class CharTree {

    private CharTreeNode root;

    public CharTree() {
    }

    public CharTreeNode getRoot() {
        return root;
    }

    public CharTree(String s) {
        char[] chars = s.toCharArray();

        root = new SingleCharTreeNode();
        CharTreeNode temp = root;

        for (int i = 0; i < chars.length; i++) {
            char b = chars[i];
            temp = temp.append(b).next(b);
        }
        temp.setValue(s);
    }

    public CharTree append(String s) {
        return append(s.toCharArray(), s);
    }

    public CharTree append(char[] chars, String s) {
        if (root == null)
            root = new SingleCharTreeNode();

        char b = chars[0];
        root = root.append(b);
        CharTreeNode temp = root.next(b);
        CharTreeNode prev = root;
        char p = b;
        for (int i = 1; i < chars.length; i++) {
            b = chars[i];
            CharTreeNode next = temp.append(b);
            prev.set(p, next);
            prev = next;
            temp = next.next(b);
            p = b;
        }
        temp.setValue(s);

        return this;
    }

    public boolean contains(String name) {
        if (root == null)
            return false;

        return get(name.toCharArray()) != null;
    }

    public String get(char[] chars) {
        return get(chars, 0, chars.length);
    }

    public String get(char[] chars, int offset, int length) {
        if (root == null)
            return null;

        CharTreeNode node = root.next(chars[offset]);
        for (int i = offset + 1; i < offset + length && node != null; i++) {
            node = node.next(chars[i]);
        }
        return node == null ? null : node.value;
    }

    public static abstract class CharTreeNode {
        protected String value;

        public abstract CharTreeNode next(char b);

        public abstract CharTreeNode append(char b);

        public abstract CharTreeNode set(char b, CharTreeNode node);

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class ArrayCharTreeNode extends CharTreeNode {
        private CharTreeNode[] nodes;

        public ArrayCharTreeNode(int size) {
            increase(size);
        }

        @Override
        public CharTreeNode next(char b) {
            if (b >= nodes.length)
                return null;

            return nodes[b];
        }

        @Override
        public CharTreeNode append(char b) {
            increase(b + 1);

            if (nodes[b] == null)
                nodes[b] = new SingleCharTreeNode();

            return this;
        }

        @Override
        public CharTreeNode set(char b, CharTreeNode node) {
            increase(b + 1);

            nodes[b] = node;

            return this;
        }

        private void increase(int size) {
            if (nodes == null)
                nodes = new CharTreeNode[size];
            else if (nodes.length < size) {
                CharTreeNode[] temp = new CharTreeNode[size];
                System.arraycopy(nodes, 0, temp, 0, nodes.length);
                nodes = temp;
            }
        }
    }

    public static class SingleCharTreeNode extends CharTreeNode {
        private char b;
        private CharTreeNode next;

        @Override
        public CharTreeNode next(char b) {
            if (b == this.b)
                return next;
            return null;
        }

        @Override
        public CharTreeNode append(char b) {
            if (next != null && this.b != b) {
                ArrayCharTreeNode node = new ArrayCharTreeNode(Math.max(this.b, b));
                node.set(this.b, next);
                node.append(b);
                return node;
            } else if (this.b == b)
                return this;
            else {
                this.b = b;
                next = new SingleCharTreeNode();
                return this;
            }
        }

        @Override
        public CharTreeNode set(char b, CharTreeNode n) {
            if (next != null && this.b != b) {
                ArrayCharTreeNode node = new ArrayCharTreeNode(Math.max(this.b, b));
                node.set(this.b, next);
                node.set(b, n);
                return node;
            } else if (this.b == b) {
                next = n;
                return this;
            } else {
                this.b = b;
                next = n;
                return this;
            }
        }

        @Override
        public String toString() {
            return "single " + b;
        }
    }
}
