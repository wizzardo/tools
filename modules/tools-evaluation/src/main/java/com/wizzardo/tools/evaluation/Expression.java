/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Unchecked;

import java.util.*;

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
            exp = clazz.getCanonicalName();
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

    public static class Definition extends Holder {
        public final Class<?> type;
        public final String name;
        public final String typeDefinition;

        public Definition(Class<?> type, String name) {
            super(name);
            this.type = type;
            this.name = name;
            this.typeDefinition = null;
        }

        public Definition(String type, String name) {
            super(name);
            this.type = null;
            this.name = name;
            this.typeDefinition = type;
        }
    }

    public static class DefinitionWithClassExpression extends Holder {
        public final ClassExpression type;
        public final String name;

        public DefinitionWithClassExpression(ClassExpression type, String name) {
            super(name);
            this.type = type;
            this.name = name;
        }

        @Override
        public Expression clone() {
            return new DefinitionWithClassExpression(type, name);
        }
    }

    public static class ResolveClass extends Holder {
        public final String className;

        public ResolveClass(String name) {
            super(name);
            this.className = "class " + name;
        }

        @Override
        public Object get(Map<String, Object> model) {
            ClassExpression cl = (ClassExpression) model.get(className);
            if (cl == null)
                return null;

            return cl.getJavaClass();
        }

        @Override
        public Expression clone() {
            return new ResolveClass(exp);
        }
    }

    public static class DefineAndSet extends Expression {
        public final Class type;
        public final String name;
        public final Expression action;
        public final String typeDefinition;

        public DefineAndSet(Class type, String name, Expression action, String typeDefinition) {
            this.type = type;
            this.name = name;
            this.action = action;
            this.typeDefinition = typeDefinition;
        }

        @Override
        public void setVariable(Variable v) {
            action.setVariable(v);
        }

        @Override
        public Expression clone() {
            return new DefineAndSet(type, name, action, typeDefinition);
        }

        @Override
        public Object get(Map<String, Object> model) {
            model.put(name, null);
            return action.get(model);
        }

        @Override
        public String toString() {
            return type + " " + action;
        }
    }

    public static class MethodDefinition extends Expression {
        public final String modifiers;
        public final Class returnType;
        public final String name;
        public final ClosureHolder action;

        public MethodDefinition(String modifiers, Class returnType, String name, ClosureHolder action) {
            this.modifiers = modifiers;
            this.returnType = returnType;
            this.name = name;
            this.action = action;
        }

        @Override
        public void setVariable(Variable v) {
            action.setVariable(v);
        }

        @Override
        public Expression clone() {
            return new MethodDefinition(modifiers, returnType, name, action);
        }

        @Override
        public Object get(Map<String, Object> model) {
            Object c = action.get(model);
            model.put(name, c);
            return c;
        }

        @Override
        public String toString() {
            return returnType + " " + name + " " + action;
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
        protected boolean isArray;

        public CastExpression(Class clazz, Expression inner) {
            this(clazz, inner, false);
        }

        public CastExpression(Class clazz, Expression inner, boolean isArray) {
            this.clazz = clazz;
            this.inner = inner;
            this.isArray = isArray;
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
            return new CastExpression(clazz, inner.clone(), isArray);
        }

        @Override
        public Object get(Map<String, Object> model) {
            Object o = inner.get(model);
            if (isArray) {
                if (!o.getClass().isArray() || o.getClass().getComponentType() != clazz)
                    throw new ClassCastException(o.getClass().getCanonicalName() + " cannot be cast to " + clazz.getCanonicalName() + "[]");
                return o;
            }
            try {
                return primitiveMapper != null ? primitiveMapper.map(o) : clazz.cast(o);
            } catch (ClassCastException e) {
                if (o instanceof ClosureExpression && Function.isSAMInterface(clazz)) {
                    return Function.wrapClosureAsProxy((ClosureExpression) o, clazz);
                }
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

        @Override
        public String toString() {
            return inner.toString();
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
                if (ob != null && ob instanceof ReturnResultHolder)
                    return ob;
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
        if (isString(exp)) {
            String quote = exp.charAt(0) + "";
            return exp.substring(1, exp.length() - 1).replace("\\" + quote, quote);
        }

        {
            Number0x number0x = isNumber0x(exp);
            if (number0x != null) {
                String value = removeUnderscores(exp);
                if (number0x == Number0x.HEX)
                    return Integer.valueOf(value.substring(2), 16);
                if (number0x == Number0x.BINARY)
                    return Integer.valueOf(value.substring(2), 2);
                if (number0x == Number0x.OCTAL)
                    return Integer.valueOf(value, 8);
            }
        }


        {
            NumberSimpleFormat simpleFormat = isNumber(exp);
            if (simpleFormat != null) {
                String value = removeUnderscores(exp);
                if (simpleFormat == NumberSimpleFormat.INT)
                    return Integer.valueOf(value);
                if (simpleFormat == NumberSimpleFormat.LONG)
                    return Long.valueOf(value.substring(0, value.length() - 1));
                if (simpleFormat == NumberSimpleFormat.SHORT)
                    return Short.valueOf(value.substring(0, value.length() - 1));
                if (simpleFormat == NumberSimpleFormat.FLOAT)
                    return Float.valueOf(value.substring(0, value.length() - 1));
                if (simpleFormat == NumberSimpleFormat.DOUBLE && (value.charAt(value.length() - 1) == 'd' || value.charAt(value.length() - 1) == 'D'))
                    return Double.valueOf(value.substring(0, value.length() - 1));
                if (simpleFormat == NumberSimpleFormat.DOUBLE)
                    return Double.valueOf(value);
            }
        }
        {
            Boolean result = parseBoolean(exp);
            if (result != null)
                return result;
        }
        return null;
    }

    protected static boolean isString(String s) {
        boolean inString = false;
        char quote = 0;
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if (c == '\"' || c == '\'') {
                    quote = c;
                    inString = true;
                } else
                    return false;
            } else if (c == quote && s.charAt(i - 1) != '\\') {
                return i == length - 1;
            }
        }
        return false;
    }


    static String removeUnderscores(String s) {
        int length = s.length();
        int i = 0;
        for (; i < length; i++) {
            if (s.charAt(i) == '_')
                break;
        }
        if (i == length)
            return s;

        StringBuilder sb = new StringBuilder(length - 1);
        sb.append(s, 0, i);
        i++;
        int from = i;
        for (; i < length; i++) {
            if (s.charAt(i) == '_') {
                sb.append(s, from, i);
                from = i + 1;
            }
        }
        if (from != i)
            sb.append(s, from, i);


        return sb.toString();
    }

    private static Boolean parseBoolean(String s) {
        int length = s.length();
        if (length == 4) {
            char c = s.charAt(0);
            if (!(c == 't' || c == 'T'))
                return null;
            c = s.charAt(1);
            if (!(c == 'r' || c == 'R'))
                return null;
            c = s.charAt(2);
            if (!(c == 'u' || c == 'U'))
                return null;
            c = s.charAt(3);
            if (!(c == 'e' || c == 'E'))
                return null;
            return Boolean.TRUE;
        } else if (length == 5) {
            char c = s.charAt(0);
            if (!(c == 'f' || c == 'F'))
                return null;
            c = s.charAt(1);
            if (!(c == 'a' || c == 'A'))
                return null;
            c = s.charAt(2);
            if (!(c == 'l' || c == 'L'))
                return null;
            c = s.charAt(3);
            if (!(c == 's' || c == 'S'))
                return null;
            c = s.charAt(4);
            if (!(c == 'e' || c == 'E'))
                return null;
            return Boolean.FALSE;
        } else
            return null;
    }

    enum Number0x {
        BINARY, OCTAL, HEX
    }

    private static Number0x isNumber0x(String s) {
        if (s == null || s.isEmpty())
            return null;

        if (s.charAt(0) != '0')
            return null;

        int length = s.length();
        int i = 1;

        if (i >= length)
            return null;

        char c = s.charAt(i);
        if (c == 'x' || c == 'X') {
            i++;
            if (i >= length)
                return null;

            for (; i < length; i++) {
                c = s.charAt(i);
                if (c >= 'a' && c <= 'f')
                    continue;
                if (c >= 'A' && c <= 'F')
                    continue;
                if (c >= '0' && c <= '9')
                    continue;
                if (c == '_')
                    continue;

                return null;
            }
            return Number0x.HEX;
        } else if (c == 'b' || c == 'B') {
            i++;
            if (i >= length)
                return null;

            for (; i < length; i++) {
                c = s.charAt(i);
                if (c == '0' || c == '1')
                    continue;
                if (c == '_')
                    continue;

                return null;
            }
            return Number0x.BINARY;
        } else {
            for (; i < length; i++) {
                c = s.charAt(i);
                if (c >= '0' && c <= '7')
                    continue;
                if (c == '_')
                    continue;

                return null;
            }
            return Number0x.OCTAL;
        }
    }

    enum NumberSimpleFormat {
        INT, SHORT, LONG, FLOAT, DOUBLE
    }

    private static NumberSimpleFormat isNumber(String s) {
        if (s == null || s.isEmpty())
            return null;

        int length = s.length();
        char c = s.charAt(length - 1);
        if (c == 'l' || c == 'L') {
            if (length == 1)
                return null;
            for (int i = 0; i < length - 1; i++) {
                c = s.charAt(i);
                if (c >= '0' && c <= '9')
                    continue;
                if (c == '_')
                    continue;

                return null;
            }
            return NumberSimpleFormat.LONG;
        }
        if (c == 's' || c == 'S') {
            if (length == 1)
                return null;
            for (int i = 0; i < length - 1; i++) {
                c = s.charAt(i);
                if (c >= '0' && c <= '9')
                    continue;
                if (c == '_')
                    continue;

                return null;
            }
            return NumberSimpleFormat.SHORT;
        }
        if (c == 'f' || c == 'F') {
            if (length == 1)
                return null;
            boolean hasDot = false;
            for (int i = 0; i < length - 1; i++) {
                c = s.charAt(i);
                if (c >= '0' && c <= '9')
                    continue;
                if (c == '_')
                    continue;
                if (c == '.') {
                    if (hasDot)
                        return null;
                    hasDot = true;
                    continue;
                }

                return null;
            }
            return NumberSimpleFormat.FLOAT;
        }
        if (c == 'd' || c == 'D') {
            if (length == 1)
                return null;
            boolean hasDot = false;
            for (int i = 0; i < length - 1; i++) {
                c = s.charAt(i);
                if (c >= '0' && c <= '9')
                    continue;
                if (c == '_')
                    continue;
                if (c == '.') {
                    if (hasDot)
                        return null;
                    hasDot = true;
                    continue;
                }

                return null;
            }
            return NumberSimpleFormat.DOUBLE;
        }

        boolean hasDot = false;
        for (int i = 0; i < length; i++) {
            c = s.charAt(i);
            if (c >= '0' && c <= '9')
                continue;
            if (c == '_')
                continue;
            if (c == '.') {
                if (hasDot)
                    return null;
                hasDot = true;
                continue;
            }

            return null;
        }
        return hasDot ? NumberSimpleFormat.DOUBLE : NumberSimpleFormat.INT;
    }


    @Override
    public String toString() {
        return exp;
    }
}
