package com.wizzardo.tools.collections;

import java.util.*;

public class Range implements Iterable<Integer>, List<Integer> {
    private int from, to;

    /**
     * @param from - inclusive
     * @param to   - exclusive
     */
    public Range(int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException("from must be lower or equal then to. " + from + " - " + to);
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
        return size() == 0;
    }

    @Override
    public Integer get(int index) {
        if (from + index >= to || index < 0)
            throw new IndexOutOfBoundsException("Index " + index + " is out of this range [" + from + ", " + to + ")");

        return from + index;
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
                if (!hasNext())
                    throw new NoSuchElementException("iteration has no more elements");

                return cur++;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
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
        Integer[] arr;
        if (a.length < size())
            arr = new Integer[size()];
        else
            arr = (Integer[]) a;

        for (int i = from; i < to; i++) {
            arr[i - from] = i;
        }
        return (T[]) arr;
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
    public List<Integer> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || from + toIndex > to)
            throw new IndexOutOfBoundsException();
        return new Range(from + fromIndex, from + toIndex);
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

    public String toString() {
        return from + ".." + to;
    }


    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public boolean add(Integer integer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Integer> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer set(int index, Integer element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Integer element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Integer> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<Integer> listIterator(int index) {
        throw new UnsupportedOperationException();
    }
}
