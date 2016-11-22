package com.wizzardo.tools.collections;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author: moxa
 * Date: 12/17/12
 */
public class CollectionTools {

    public static <T> void each(Iterable<T> c, VoidClosure<? super T> closure) {
        for (T t : c) {
            closure.execute(t);
        }
    }

    public static <K, V> void each(Map<K, V> c, VoidClosure2<? super K, ? super V> closure) {
        for (Map.Entry<K, V> e : c.entrySet()) {
            closure.execute(e.getKey(), e.getValue());
        }
    }

    public static <T> void eachWithIndex(Iterable<T> c, VoidClosure2<Integer, ? super T> closure) {
        int i = 0;
        for (T t : c) {
            closure.execute(i, t);
            i++;
        }
    }

    public static <T, R> List<R> collect(Iterable<T> c, Closure<R, ? super T> closure) {
        List<R> l = new ArrayList<R>();
        for (T t : c) {
            l.add(closure.execute(t));
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Collection r) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (r.contains(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Pattern p) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (p.matcher(String.valueOf(t)).matches())
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Class clazz) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (clazz.isAssignableFrom(t.getClass()))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> findAll(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> T find(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        for (T t : c) {
            if (closure.execute(t))
                return t;
        }
        return null;
    }

    public static <T> T remove(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        Iterator<T> iterator = c.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (closure.execute(t)) {
                iterator.remove();
                return t;
            }
        }
        return null;
    }

    public static <T, C extends Iterable<T>> C removeAll(C c, Closure<Boolean, ? super T> closure) {
        Iterator<T> iterator = c.iterator();
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (closure.execute(t))
                iterator.remove();
        }
        return c;
    }

    public static <T> boolean every(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        boolean b = true;
        for (T t : c) {
            b &= closure.execute(t);
            if (!b) {
                return false;
            }
        }
        return b;
    }

    public static <T> boolean any(Iterable<T> c, Closure<Boolean, ? super T> closure) {
        for (T t : c) {
            if (closure.execute(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> String join(Iterable<T> c, String separator) {
        StringBuilder sb = new StringBuilder();
        for (T t : c) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(t);
        }
        return sb.toString();
    }

    public static <K, V, T> Map<K, List<V>> group(Iterable<T> c, Closure<K, ? super T> toKey, Closure<V, ? super T> toValue) {
        HashMap<K, List<V>> map = new HashMap<K, List<V>>();
        for (T t : c) {
            K key = toKey.execute(t);
            List<V> list = map.get(key);
            if (list == null) {
                list = new ArrayList<V>();
                map.put(key, list);
            }
            list.add(toValue.execute(t));
        }
        return map;
    }

    public static void times(int times, VoidClosure<? super Integer> closure) {
        for (int i = 0; i < times; i++) {
            closure.execute(i);
        }
    }

    public interface Closure<R, T> {
        R execute(T it);
    }

    public interface Closure2<R, T1, T2> {
        R execute(T1 it, T2 it2);
    }

    public interface Closure3<R, T1, T2, T3> {
        R execute(T1 it, T2 it2, T3 it3);
    }

    public interface Closure4<R, T1, T2, T3, T4> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4);
    }

    public interface Closure5<R, T1, T2, T3, T4, T5> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5);
    }

    public interface Closure6<R, T1, T2, T3, T4, T5, T6> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6);
    }

    public interface Closure7<R, T1, T2, T3, T4, T5, T6, T7> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7);
    }

    public interface Closure8<R, T1, T2, T3, T4, T5, T6, T7, T8> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8);
    }

    public interface Closure9<R, T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9);
    }

    public interface Closure10<R, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
        R execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9, T10 it10);
    }

    public interface VoidClosure<T> {
        void execute(T t);
    }

    public interface VoidClosure2<T1, T2> {
        void execute(T1 it, T2 it2);
    }

    public interface VoidClosure3<T1, T2, T3> {
        void execute(T1 it, T2 it2, T3 it3);
    }

    public interface VoidClosure4<T1, T2, T3, T4> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4);
    }

    public interface VoidClosure5<T1, T2, T3, T4, T5> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5);
    }

    public interface VoidClosure6<T1, T2, T3, T4, T5, T6> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6);
    }

    public interface VoidClosure7<T1, T2, T3, T4, T5, T6, T7> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7);
    }

    public interface VoidClosure8<T1, T2, T3, T4, T5, T6, T7, T8> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8);
    }

    public interface VoidClosure9<T1, T2, T3, T4, T5, T6, T7, T8, T9> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9);
    }

    public interface VoidClosure10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> {
        void execute(T1 it, T2 it2, T3 it3, T4 it4, T5 it5, T6 it6, T7 it7, T8 it8, T9 it9, T10 it10);
    }

}
