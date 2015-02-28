package com.wizzardo.tools.misc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wizzardo
 * Date: 7/30/14
 */
public class CharTree<V> {

    private CharTreeNode<V> root;

    public CharTree() {
    }

    public CharTreeNode<V> getRoot() {
        return root;
    }

    public CharTree<V> append(String s, V value) {
        return append(s.toCharArray(), value);
    }

    public CharTree<V> appendReverse(String s, V value) {
        return append(reverse(s.toCharArray()), value);
    }

    public CharTree<V> append(char[] chars, V value) {
        if (root == null)
            root = new SingleCharTreeNode<V>();

        char b = chars[0];
        root = root.append(b);
        CharTreeNode<V> temp = root.next(b);
        CharTreeNode<V> prev = root;
        char p = b;
        for (int i = 1; i < chars.length; i++) {
            b = chars[i];
            CharTreeNode<V> next = temp.append(b);
            prev.set(p, next);
            prev = next;
            temp = next.next(b);
            p = b;
        }
        temp.value = value;

        return this;
    }

    public boolean contains(String name) {
        return get(name) != null;
    }

    public boolean contains(char[] chars) {
        return contains(chars, 0, chars.length);
    }

    public boolean contains(char[] chars, int offset, int length) {
        return get(chars, offset, length) != null;
    }

    public V get(char[] chars) {
        return get(chars, 0, chars.length);
    }

    public V get(char[] chars, int offset, int length) {
        CharTreeNode<V> node = root;
        for (int i = offset; i < offset + length && node != null; i++) {
            node = node.next(chars[i]);
        }
        return node == null ? null : node.value;
    }

    public V get(String s) {
        CharTreeNode<V> node = root;
        int length = s.length();
        for (int i = 0; i < length && node != null; i++) {
            node = node.next(s.charAt(i));
        }
        return node == null ? null : node.value;
    }

    public V findStarts(char[] chars) {
        return findStarts(chars, 0, chars.length);
    }

    public V findStarts(char[] chars, int offset, int length) {
        CharTreeNode<V> node = root;
        for (int i = offset; i < offset + length && node != null && node.value == null; i++) {
            node = node.next(chars[i]);
        }
        return node == null ? null : node.value;
    }

    public V findStarts(String s) {
        CharTreeNode<V> node = root;
        int length = s.length();
        for (int i = 0; i < length && node != null && node.value == null; i++) {
            node = node.next(s.charAt(i));
        }
        return node == null ? null : node.value;
    }

    public List<V> findAllStarts(char[] chars) {
        return findAllStarts(chars, 0, chars.length);
    }

    public List<V> findAllStarts(char[] chars, int offset, int length) {
        CharTreeNode<V> node = root;
        List<V> list = new ArrayList<V>();
        for (int i = offset; i < offset + length && node != null; i++) {
            node = node.next(chars[i]);
            addIfNotNull(list, node);
        }
        return list;
    }

    public List<V> findAllStarts(String s) {
        CharTreeNode<V> node = root;
        List<V> list = new ArrayList<V>();
        int length = s.length();
        for (int i = 0; i < length && node != null; i++) {
            node = node.next(s.charAt(i));
            addIfNotNull(list, node);
        }
        return list;
    }

    public V findEnds(char[] chars) {
        return findEnds(chars, 0, chars.length);
    }

    public V findEnds(char[] chars, int offset, int length) {
        CharTreeNode<V> node = root;
        int l = chars.length - 1;
        for (int i = offset; i < offset + length && node != null && node.value == null; i++) {
            node = node.next(chars[l - i]);
        }
        return node == null ? null : node.value;
    }

    public V findEnds(String s) {
        CharTreeNode<V> node = root;
        int length = s.length();
        for (int i = 0; i < length && node != null && node.value == null; i++) {
            node = node.next(s.charAt(length - i - 1));
        }
        return node == null ? null : node.value;
    }

    public List<V> findAllEnds(char[] chars) {
        return findAllEnds(chars, 0, chars.length);
    }

    public List<V> findAllEnds(char[] chars, int offset, int length) {
        CharTreeNode<V> node = root;
        int l = chars.length - 1;
        List<V> list = new ArrayList<V>();
        for (int i = offset; i < offset + length && node != null; i++) {
            node = node.next(chars[l - i]);
            addIfNotNull(list, node);
        }
        return list;
    }

    public List<V> findAllEnds(String s) {
        CharTreeNode<V> node = root;
        int length = s.length();
        List<V> list = new ArrayList<V>();
        for (int i = 0; i < length && node != null; i++) {
            node = node.next(s.charAt(length - i - 1));
            addIfNotNull(list, node);
        }
        return list;
    }

    private void addIfNotNull(List<V> list, CharTreeNode<V> node) {
        if (node != null && node.value != null)
            list.add(node.value);
    }

    public static abstract class CharTreeNode<V> {
        protected V value;

        public abstract CharTreeNode<V> next(char b);

        public abstract CharTreeNode<V> append(char b);

        public abstract CharTreeNode<V> set(char b, CharTreeNode<V> node);

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    public static class ArrayCharTreeNode<V> extends CharTreeNode<V> {
        private CharTreeNode<V>[] nodes;

        public ArrayCharTreeNode(int size) {
            increase(size);
        }

        @Override
        public CharTreeNode<V> next(char b) {
            if (b >= nodes.length)
                return null;

            return nodes[b];
        }

        @Override
        public CharTreeNode<V> append(char b) {
            increase(b + 1);

            if (nodes[b] == null)
                nodes[b] = new SingleCharTreeNode<V>();

            return this;
        }

        @Override
        public CharTreeNode<V> set(char b, CharTreeNode<V> node) {
            increase(b + 1);

            nodes[b] = node;

            return this;
        }

        private void increase(int size) {
            if (nodes == null)
                nodes = new CharTreeNode[size];
            else if (nodes.length < size) {
                CharTreeNode<V>[] temp = new CharTreeNode[size];
                System.arraycopy(nodes, 0, temp, 0, nodes.length);
                nodes = temp;
            }
        }
    }

    static class SingleCharTreeNode<V> extends CharTreeNode<V> {
        private char b;
        private CharTreeNode<V> next;

        @Override
        public CharTreeNode<V> next(char b) {
            if (b == this.b)
                return next;
            return null;
        }

        @Override
        public CharTreeNode<V> append(char b) {
            if (next != null && this.b != b) {
                ArrayCharTreeNode<V> node = new ArrayCharTreeNode<V>(Math.max(this.b, b));
                node.set(this.b, next);
                node.append(b);
                return node;
            } else if (this.b == b)
                return this;
            else {
                this.b = b;
                next = new SingleCharTreeNode<V>();
                return this;
            }
        }

        @Override
        public CharTreeNode<V> set(char b, CharTreeNode<V> n) {
            if (next != null && this.b != b) {
                ArrayCharTreeNode<V> node = new ArrayCharTreeNode<V>(Math.max(this.b, b));
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

    public static char[] reverse(char[] chars) {
        char c;
        for (int i = 0, j = chars.length - 1; i < j; i++, j--) {
            c = chars[i];
            chars[i] = chars[j];
            chars[j] = c;
        }
        return chars;
    }
}
