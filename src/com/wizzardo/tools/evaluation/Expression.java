/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.WrappedException;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public abstract class Expression {

    protected String exp;
    protected Object result;
    protected boolean hardcoded = false;

    public static class Holder extends Expression {

        public static final Expression NULL = new Holder() {{
            hardcoded = true;
        }};

        protected Expression inner;
        protected boolean parsed = false;

        private Holder() {
        }

        public Holder(String exp) {
            this.exp = exp;
        }

        public Holder(String exp, boolean hardcoded) {
            if (hardcoded)
                result = exp;
            this.exp = exp;
            this.hardcoded = hardcoded;
        }

        public Holder(Class clazz) {
            hardcoded = true;
            this.result = clazz;
        }

        public Holder(Expression inner) {
            this.inner = inner;
        }

        public Holder(Object result) {
            hardcoded = true;
            this.result = result;
        }

        @Override
        public String toString() {
            if (hardcoded) {
                return String.valueOf(result);
            }
            if (exp != null) {
                return exp;
            }
            if (inner != null) {
                return inner.toString();
            }
            return super.toString();
        }

        @Override
        public Expression clone() {
            if (inner != null) {
                return new Holder(inner.clone());
            }
            if (exp != null) {
                if (hardcoded) {
                    return new Holder(result);
                }
                return new Holder(exp);
            }
            return null;
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (hardcoded) {
                return result;
            }
            Object result = this.result;
            if (result == null)
                if (exp != null) {
                    if (!parsed) {
                        result = parse(exp);
                        parsed = true;
                        if (result != null) {
                            hardcoded = true;
                            this.result = result;
                            return result;
                        }
                    }
                    if (model.containsKey(exp)) {
                        result = model.get(exp);
                    }
                } else if (inner != null) {
                    result = inner.get(model);
                }
            return result;
        }

    }

    public static class MapExpression extends Expression {
        protected Map map;

        public MapExpression(Map map) {
            this.map = map;
        }

        @Override
        public Expression clone() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        @Override
        public Object get(Map<String, Object> model) {
//            Object result = this.result;
//            if (result == null) {
            Map r = null;
            try {
                r = map.getClass().newInstance();
            } catch (InstantiationException e) {
                throw new WrappedException(e);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
            Iterator<Map.Entry> i = ((Set<Map.Entry>) map.entrySet()).iterator();
            while (i.hasNext()) {
                Map.Entry entry = i.next();
                r.put(String.valueOf(entry.getKey()), ((Expression) entry.getValue()).get(model));
            }
//                result = r;
//            }
            return r;
        }

        @Override
        public String toString() {
            return "new " + map.getClass();
        }
    }

    public static class CollectionExpression extends Expression {
        protected Collection collection;

        public CollectionExpression(Collection collection) {
            this.collection = collection;
        }

        @Override
        public String toString() {
            return "new " + collection.getClass();
        }

        @Override
        public Expression clone() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        @Override
        public Object get(Map<String, Object> model) {
//            Object result = this.result;
//            if (result == null) {
            Collection r = null;
            try {
                r = collection.getClass().newInstance();
            } catch (InstantiationException e) {
                throw new WrappedException(e);
            } catch (IllegalAccessException e) {
                throw new WrappedException(e);
            }
            Iterator i = collection.iterator();
            while (i.hasNext()) {
                r.add(((Expression) i.next()).get(model));
            }
//                result = r;
//            }
//            return result;
            return r;
        }
    }


    public String raw() {
        return exp;
    }

    public abstract Expression clone();

    public abstract Object get(Map<String, Object> model);

    static Object parse(String exp) {
        if (exp == null) {
            return null;
        }
        Matcher m = string.matcher(exp);
        if (m.matches()) {
            return m.group(1) == null ? m.group(2).replace("\\'", "'") : m.group(1).replace("\\\"", "\"");
        }
        m = number.matcher(exp);
        if (m.matches()) {
            if (m.groupCount() > 1 && m.group(2).length() > 0) {
                if ("d".equals(m.group(2))) {
                    return Double.valueOf(m.group(1));
                } else if ("f".equals(m.group(2))) {
                    return Float.valueOf(m.group(1));
                } else if ("l".equals(m.group(2))) {
                    return Long.valueOf(m.group(1));
                } else if ("b".equals(m.group(2))) {
                    return Byte.valueOf(m.group(1));
                }
            } else {
                try {
                    return Integer.valueOf(exp);
                } catch (NumberFormatException e) {
                    return Double.valueOf(exp);
                }
            }
        }
        m = bool.matcher(exp);
        if (m.matches()) {
            return Boolean.valueOf(exp);
        }
        return null;
    }

    private static final Pattern string = Pattern.compile("\"(.*)\"|\'(.*)\'");
    private static final Pattern number = Pattern.compile("(\\d+\\.?\\d*)([dflb]?)");
    private static final Pattern bool = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    @Override
    public String toString() {
        return exp;
    }
}
