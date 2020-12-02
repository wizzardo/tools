/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Unchecked;

import java.util.*;
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

        protected Variable variable;

        private Holder() {
        }

        public Holder(String exp) {
            this.exp = exp;
            Object result = parse(exp);
            if (result != null) {
                hardcoded = true;
                this.result = result;
            }
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

        public Holder(Object result) {
            hardcoded = true;
            this.result = result;
        }

        public Holder(String exp, Object result) {
            hardcoded = true;
            this.result = result;
            this.exp = exp;
        }

        @Override
        public String toString() {
            if (hardcoded) {
                return String.valueOf(result);
            }

            return super.toString();
        }

        @Override
        public void setVariable(Variable v) {
            if (hardcoded)
                return;

            if (exp.equals(v.getName()))
                variable = v;
        }

        @Override
        public Expression clone() {
            if (hardcoded) {
                return new Holder(exp, result);
            }

            Holder holder = new Holder(exp);
            holder.variable = variable;
            return holder;
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (hardcoded)
                return result;

            if (variable != null)
                return variable.get();

            if (exp != null)
                return model.get(exp);

            return null;
        }

    }

    public static class VariableOrFieldOfThis extends Expression {

        public final Expression thisHolder;
        public final Function function;
        protected Variable variable;

        public VariableOrFieldOfThis(String exp) {
            this.exp = exp;
            thisHolder = new Expression.Holder("delegate");
            function = new Function(thisHolder, exp);
        }

        @Override
        public void setVariable(Variable v) {
            if (exp.equals(v.getName()))
                variable = v;
        }

        @Override
        public Expression clone() {
            VariableOrFieldOfThis expression = new VariableOrFieldOfThis(exp);
            expression.variable = variable;
            return expression;
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (variable != null)
                return variable.get();

            if (model.containsKey(exp))
                return model.get(exp);

            if (hasDelegate(model)) {
                return function.get(model);
            }

            return model.get(exp);
        }

        public static boolean hasDelegate(Map<String, Object> model) {
            return model.containsKey("delegate");
        }
    }


    public static class MapExpression extends Expression {
        protected Map<String, Expression> map;

        public MapExpression(Map<String, Expression> map) {
            this.map = map;
        }

        public MapExpression() {
        }

        @Override
        public void setVariable(Variable v) {
            if (map == null)
                return;

            for (Map.Entry<String, Expression> e : map.entrySet())
                e.getValue().setVariable(v);
        }

        @Override
        public Expression clone() {
            if (map == null)
                return this;

            Map<String, Expression> m = new HashMap<String, Expression>(map.size() + 1);
            for (Map.Entry<String, Expression> entry : map.entrySet()) {
                m.put(entry.getKey(), entry.getValue().clone());
            }
            return new MapExpression(m);
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (map == null)
                return new HashMap();

            Map r = null;
            try {
                r = map.getClass().newInstance();
            } catch (InstantiationException e) {
                throw Unchecked.rethrow(e);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            }
            Iterator<Map.Entry<String, Expression>> i = map.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry entry = i.next();
                r.put(String.valueOf(entry.getKey()), ((Expression) entry.getValue()).get(model));
            }
            return r;
        }

        @Override
        public String toString() {
            if (map != null)
                return "new " + map.getClass();
            else
                return "new LinkedHashMap";
        }
    }

    public static class CollectionExpression extends Expression {
        protected Collection<Expression> collection;

        public CollectionExpression(Collection<Expression> collection) {
            this.collection = collection;
        }

        public CollectionExpression() {
        }

        @Override
        public String toString() {
            if (collection != null)
                return "new " + collection.getClass();
            else
                return "new ArrayList";
        }

        @Override
        public void setVariable(Variable v) {
            if (collection == null)
                return;

            for (Expression e : collection)
                e.setVariable(v);
        }

        @Override
        public Expression clone() {
            if (collection == null)
                return this;

            Collection<Expression> l = new ArrayList<Expression>(collection.size());
            for (Expression expression : collection) {
                l.add(expression.clone());
            }

            return new CollectionExpression(l);
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (collection == null)
                return new ArrayList();

            Collection r = null;
            try {
                r = collection.getClass().newInstance();
            } catch (InstantiationException e) {
                throw Unchecked.rethrow(e);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            }
            Iterator<Expression> i = collection.iterator();
            while (i.hasNext()) {
                r.add(i.next().get(model));
            }
            return r;
        }
    }

    public static class CastExpression extends Expression {
        protected Class clazz;
        protected Expression inner;
        protected Mapper<Object, Object> primitiveMapper;

        public CastExpression(Class clazz, Expression inner) {
            this.clazz = clazz;
            this.inner = inner;
            if (clazz.isPrimitive()) {
                if (int.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).intValue();
                        }
                    };
                else if (long.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).longValue();
                        }
                    };
                else if (short.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).shortValue();
                        }
                    };
                else if (byte.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).byteValue();
                        }
                    };
                else if (float.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).floatValue();
                        }
                    };
                else if (double.class == clazz)
                    primitiveMapper = new Mapper<Object, Object>() {
                        @Override
                        public Object map(Object o) {
                            return ((Number) o).doubleValue();
                        }
                    };
            }
        }

        @Override
        public void setVariable(Variable v) {
            inner.setVariable(v);
        }

        @Override
        public Expression clone() {
            return new CastExpression(clazz, inner.clone());
        }

        @Override
        public Object get(Map<String, Object> model) {
            Object o = inner.get(model);
            try {
                return primitiveMapper != null ? primitiveMapper.map(o) : clazz.cast(o);
            } catch (ClassCastException e) {
                throw new ClassCastException(o.getClass().getCanonicalName() + " cannot be cast to " + clazz.getCanonicalName());
            }
        }
    }

    public static class ReturnResultHolder {
        public final Object value;

        public ReturnResultHolder(Object value) {
            this.value = value;
        }
    }

    public static class ReturnExpression extends Expression {
        protected Expression inner;

        public ReturnExpression(Expression inner) {
            this.inner = inner;
        }

        @Override
        public void setVariable(Variable v) {
            if (inner != null)
                inner.setVariable(v);
        }

        @Override
        public Expression clone() {
            if (inner == null)
                return this;

            return new ReturnExpression(inner.clone());
        }

        @Override
        public Object get(Map<String, Object> model) {
            if (inner == null)
                return new ReturnResultHolder(null);

            Object o = inner.get(model);
            return new ReturnResultHolder(o);
        }
    }

    public static class BlockExpression extends Expression {
        protected List<Expression> expressions = new ArrayList<Expression>();

        @Override
        public void setVariable(Variable v) {
            for (Expression expression : expressions) {
                expression.setVariable(v);
            }
        }

        public void add(Expression e) {
            expressions.add(e);
        }

        @Override
        public Expression clone() {
            BlockExpression clone = new BlockExpression();
            for (Expression expression : expressions) {
                clone.add(expression);
            }
            return clone;
        }

        @Override
        public Object get(Map<String, Object> model) {
            Object ob = null;
            for (Expression expression : expressions) {
                ob = expression.get(model);
            }
            return ob;
        }

        public boolean isEmpty() {
            return expressions.isEmpty();
        }

        public int size() {
            return expressions.size();
        }

        public Expression get(int i) {
            return expressions.get(i);
        }
    }

    public String raw() {
        return exp;
    }

    public abstract void setVariable(Variable v);

    public abstract Expression clone();

    public abstract Object get(Map<String, Object> model);

    public Object get() {
        return get(null);
    }

    public boolean isHardcoded() {
        return hardcoded;
    }

    static Object parse(String exp) {
        if (exp == null) {
            return null;
        }
        Matcher m;
        if (isString(exp)) {
            String quote = exp.charAt(0) + "";
            return exp.substring(1, exp.length() - 1).replace("\\" + quote, quote);
        }

        m = numberOx.matcher(exp);
        if (m.matches()) {
            if (m.groupCount() > 1 && m.group(1) != null) {
                String value = removeUnderscores(m.group(2));
                String prefix = m.group(1);
                if ("0x".equalsIgnoreCase(prefix))
                    return Integer.valueOf(value, 16);
                if ("0b".equalsIgnoreCase(prefix))
                    return Integer.valueOf(value, 2);
                if ("0".equals(prefix) && !value.toLowerCase().endsWith("f"))
                    return Integer.valueOf(value, 8);
            }
        }

        m = number.matcher(exp);
        if (m.matches()) {
            if (m.groupCount() > 1 && m.group(2).length() > 0) {
                char suffix = m.group(2).charAt(0);
                String value = removeUnderscores(m.group(1));
                if (suffix == 'd' || suffix == 'D') {
                    return Double.valueOf(value);
                } else if (suffix == 'f' || suffix == 'F') {
                    return Float.valueOf(value);
                } else if (suffix == 'l' || suffix == 'L') {
                    return Long.valueOf(value);
                } else if (suffix == 'b' || suffix == 'B') {
                    return Byte.valueOf(value);
                }
            } else {
                String value = removeUnderscores(exp);
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException e) {
                    return Double.valueOf(value);
                }
            }
        }
        m = bool.matcher(exp);
        if (m.matches()) {
            return Boolean.valueOf(exp);
        }
        return null;
    }

    protected static boolean isString(String s) {
        boolean inString = false;
        char quote = 0;
        int length = s.length();
        for (int i = 0; i < length; i++) {
            if (!inString) {
                if (s.charAt(i) == '\"' || s.charAt(i) == '\'') {
                    quote = s.charAt(i);
                    inString = true;
                } else
                    return false;
            } else if (s.charAt(i) == quote && s.charAt(i - 1) != '\\') {
                return i == length - 1;
            }
        }
        return false;
    }


    static String removeUnderscores(String s) {
        return underscore.matcher(s).replaceAll("");
    }

    private static final Pattern number = Pattern.compile("([\\d_]+\\.?[\\d_]*)([dflbDFLB]?)");
    private static final Pattern numberOx = Pattern.compile("(0[xbXB]?)([\\d_abcdefABCDEF]+)");
    private static final Pattern underscore = Pattern.compile("_");
    private static final Pattern bool = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    @Override
    public String toString() {
        return exp;
    }
}
