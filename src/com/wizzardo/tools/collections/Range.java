package com.wizzardo.tools.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author: moxa
 * Date: 12/17/12
 */
public class Range implements Iterable<Integer>, List<Integer> {
    private int from, to;

    /**
     * @param from- inclusive
     * @param to    - exclusive
     */
    public Range(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("from must be lower then to. " + from + " - " + to);
        }
        this.from = from;
        this.to = to;
    }

    @Override
    public int size() {
        return to - from;
    }

    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            Integer i = (Integer) o;
            return i >= from && i < to;
        }
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int cur = from;

            @Override
            public boolean hasNext() {
                return cur < to;
            }

            @Override
            public Integer next() {
                return cur++;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public Object[] toArray() {
        Integer[] arr = new Integer[size()];
        for (int i = from; i < to; i++) {
            arr[i - from] = i;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.getClass().getName().equals("[Ljava.lang.Integer;")) {
            Integer[] arr;
            if (a.length == 0)
                arr = new Integer[size()];
            else
                arr = (Integer[]) a;
            for (int i = from; i < to; i++) {
                arr[i - from] = i;
            }
            return (T[]) arr;
        }
        return a;
    }

    @Override
    public boolean add(Integer integer) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean b = true;
        for (Object i : c) {
            b &= contains(i);
            if (!b) {
                return false;
            }
        }
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public Integer get(int index) {
        return from + index;
    }

    @Override
    public Integer set(int index, Integer element) {
        return null;
    }

    @Override
    public void add(int index, Integer element) {
    }

    @Override
    public Integer remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        if (o.getClass() == Integer.class) {
            int i = (Integer) o;
            if (i >= from && i < to) {
                return i - from;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public ListIterator<Integer> listIterator() {
        return null;
    }

    @Override
    public ListIterator<Integer> listIterator(int index) {
        return null;
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        return new Range(from + fromIndex, fromIndex + toIndex);
    }

    public String toString() {
        return from + ".." + to;
    }

}
