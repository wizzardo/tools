package org.bordl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author: moxa
 * Date: 12/17/12
 */
public class CollectionUtils {

    public static <T> void each(Iterable<T> c, Closure<T> closure) {
        for (T t : c) {
            closure.execute(t);
        }
    }

    public static <T> void eachWithIndex(Iterable<T> c, ClosureWithParam<T, Integer> closure) {
        int i = 0;
        for (T t : c) {
            closure.execute(t, i);
            i++;
        }
    }

    public static <T, R> List<R> collect(Iterable<T> c, ClosureWithResult<T, R> closure) {
        List<R> l = new ArrayList<R>();
        for (T t : c) {
            l.add(closure.execute(t));
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Object expression) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (t.equals(expression))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, ClosureWithResult<T, Boolean> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> List<T> grep(Iterable<T> c, Range r) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (r.contains(t))
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
            if (t.toString().matches(p.pattern()))
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

    public static <T> List<T> findAll(Iterable<T> c, ClosureWithResult<T, Boolean> closure) {
        List<T> l = new ArrayList<T>();
        for (T t : c) {
            if (closure.execute(t))
                l.add(t);
        }
        return l;
    }

    public static <T> T find(Iterable<T> c, ClosureWithResult<T, Boolean> closure) {
        for (T t : c) {
            if (closure.execute(t))
                return t;
        }
        return null;
    }

    public static <T> boolean every(Iterable<T> c, ClosureWithResult<T, Boolean> closure) {
        boolean b = true;
        for (T t : c) {
            b &= closure.execute(t);
            if (!b) {
                return false;
            }
        }
        return b;
    }

    public static <T> boolean any(Iterable<T> c, ClosureWithResult<T, Boolean> closure) {
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

    public static abstract class Closure<T> {
        public abstract void execute(T it);
    }

    public static abstract class ClosureWithResult<T, R> {
        public abstract R execute(T it);
    }

    public static abstract class ClosureWithParam<T, P> {
        public abstract void execute(T it, P p);
    }

}
