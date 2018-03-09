package com.wizzardo.tools.misc;

/**
 * @author: moxa
 * Date: 12/11/12
 */
public class Pair<K, V> {
    public final K key;
    public final V value;

    private Pair() {
        value = null;
        key = null;
    }

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<K, V>(k, v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
        if (value != null ? !value.equals(pair.value) : pair.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return key + ": " + value;
    }
}
