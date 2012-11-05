/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public class ExpressionHolder {

    protected String exp;
    protected Operation operation;
    protected Function function;
    protected UserFunction userFunction;
    protected Object result;
    protected boolean done = false;
    protected ExpressionHolder inner;
    protected boolean simple = false;
    protected Class clazz;

    public ExpressionHolder(String exp) {
        this.exp = clean(exp.trim());
    }

    public ExpressionHolder(Operation operation) {
        this.operation = operation;
    }

    public ExpressionHolder(Function function) {
        this.function = function;
    }

    public ExpressionHolder(ExpressionHolder inner) {
        this.inner = inner;
    }

    public ExpressionHolder(UserFunction userFunction) {
        this.userFunction = userFunction;
    }

    public ExpressionHolder(Object result) {
        this.result = result;
        done = true;
    }

    public ExpressionHolder(Class clazz) {
        this.clazz = clazz;
        done = true;
    }

    public boolean isUserFunction() {
        return userFunction != null;
    }

    public UserFunction getUserFunction() {
        return userFunction;
    }

    /**
     * avoid use this before executing expression, because searching for java.lang.reflect.Method object is very slow
     *
     * @return copy of this expression
     */
    @Override
    public ExpressionHolder clone() {
        if (function != null) {
            return new ExpressionHolder(function.clone());
        }
        if (inner != null) {
            return new ExpressionHolder(inner.clone());
        }
        if (operation != null) {
            return new ExpressionHolder(operation.clone());
        }
        if (clazz != null) {
            return new ExpressionHolder(clazz);
        }
        if (userFunction != null) {
            return new ExpressionHolder(userFunction);
        }
        if (exp != null) {
            if (simple) {
                return new ExpressionHolder(result);
            }
            return new ExpressionHolder(exp);
        }
        return null;
    }

    public Class getClass(Map<String, Object> model) throws Exception {
        if (clazz == null) {
            return get(model).getClass();
        } else {
            return clazz;
        }
    }

    public String getExp() {
        return exp;
    }

    public String exp() {
        return exp;
    }

    public Object get(Map<String, Object> model) throws Exception {
        if (!done) {
            if (exp != null) {
                result = parse(exp);
                if (result != null) {
                    simple = true;
                } else if (model.containsKey(exp)) {
                    result = model.get(exp);
                }
            } else if (operation != null) {
                result = operation.evaluate(model);
            } else if (function != null) {
                result = function.evaluate(model);
            } else if (inner != null) {
                result = inner.get(model);
            } else if (userFunction != null) {
                result = userFunction.get(model);
            }
            done = true;
        }
        return result;
    }

    static Object parse(String exp) {
        if (exp == null) {
            return null;
        }
        Matcher m = string.matcher(exp);
        if (m.matches()) {
            return m.group(1);
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

    private static final Pattern string = Pattern.compile("\"(.*)\"");
    private static final Pattern number = Pattern.compile("(\\d+\\.?\\d*)([dflb]?)");
    private static final Pattern bool = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    static String clean(String s) {
        if (s != null && s.length() > 0 && s.charAt(0) == '(' && (s.charAt(s.length() - 1) == ')' || (s.charAt(s.length() - 2) == ')' && s.charAt(s.length() - 1) == '.'))) {
            if (s.charAt(s.length() - 1) == '.') {
                return s.substring(1, s.length() - 2).trim();
            } else {
                return s.substring(1, s.length() - 1).trim();
            }
        }
        return s != null ? s.trim() : null;
    }

    @Override
    public String toString() {
        if (exp != null) {
            return exp + "\tis done? " + done + (done ? "\tresult:" + result : "");
        }
        if (inner != null) {
            return inner + "\tis done? " + done + (done ? "\tresult:" + result : "");
        }
        if (function != null) {
            return function + "\tis done? " + done + (done ? "\tresult:" + result : "");
        }
        return operation + "\tis done? " + done + (done ? "\tresult:" + result : "");
    }
}
