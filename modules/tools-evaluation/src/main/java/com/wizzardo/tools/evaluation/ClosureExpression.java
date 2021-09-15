package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.misc.Pair;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author: moxa
 * Date: 8/11/13
 */
public class ClosureExpression extends Expression implements Runnable, Callable {

    protected static final Pair<String, Class>[] DEFAULT_ARGS = new Pair[]{new Pair<String, Class>("it", Object.class)};
    protected static final Pair<String, Class>[] EMPTY_ARGS = new Pair[0];
    protected List<Expression> expressions = new ArrayList<Expression>();
    protected Pair<String, Class>[] args = EMPTY_ARGS;
    protected Map<String, Object> context = Collections.emptyMap();

    @Override
    public void setVariable(Variable v) {
        for (Expression e : expressions)
            e.setVariable(v);
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public Expression clone() {
        ClosureExpression clone = new ClosureExpression();
        clone.args = args;
        for (Expression expression : expressions) {
            clone.add(expression.clone());
        }
        if (!context.isEmpty())
            clone.context = new HashMap<String, Object>(context);

        return clone;
    }

    @Override
    public Object get(Map<String, Object> model) {
//        HashMap<String, Object> local = new HashMap<String, Object>(model);
        Object ob = null;
        for (Expression expression : expressions) {
            ob = expression.get(model);
            if (ob != null && ob.getClass() == Expression.ReturnResultHolder.class)
                return ((ReturnResultHolder) ob).value;
        }
        return ob;
    }

    @Override
    public String toString() {
        return expressions.toString();
    }

    @Override
    public Object get() {
        return get(context);
    }

    public Object get(Map<String, Object> model, Object... args) {
        return getAgainst(model, model, args);
    }

    public Object getAgainst(Map<String, Object> model, Object thisObject, Object... arg) {
        HashMap<String, Object> local = model != null ? new HashMap<String, Object>(model) : new HashMap<String, Object>(2, 1);
        local.putAll(context);
        local.put("delegate", thisObject);
        if (model != null)
            local.put("this", model);

        if (arg != null) {
            if (args.length == 0 && arg.length == 1) {
                local.put("it", arg[0]);
            } else {
                if (args.length != arg.length)
                    throw new IllegalArgumentException("wrong number of arguments! there were " + (arg.length) + ", but must be " + args.length);
                for (int i = 0; i < args.length; i++) {
//                if (!args[i].value.isAssignableFrom(arg[i].getClass()))
//                    throw new ClassCastException("Can not cast " + args[i].getClass() + " to " + args[i].value);
                    local.put(args[i].key, arg[i]);
                }
            }
        }
        Object ob = null;
        for (Expression expression : expressions) {
            ob = expression.get(local);
            if (ob != null && ob.getClass() == Expression.ReturnResultHolder.class)
                return ((ReturnResultHolder) ob).value;
        }
        return ob;
    }

    public void add(Expression expression) {
        expressions.add(expression);
    }

    public String parseArguments(String exp) {
        int i = exp.indexOf("->");
        if (i >= 0 && EvalTools.countOpenBrackets(exp, 0, i) == 0 && !EvalTools.inString(exp, 0, i)) {
            String args = exp.substring(0, i).trim();
            exp = exp.substring(i + 2).trim();

            if (args.startsWith("(") && args.endsWith(")"))
                args = args.substring(1, args.length() - 1);

            args = args.trim();
            if (!args.isEmpty()) {
//                String[] pairs = args.split(",\\s*");
//                this.args = new Pair[pairs.length];
//
//                for (i = 0; i < pairs.length; i++) {
//                    String pair = pairs[i];
//                    if (pair.startsWith("final "))
//                        pair = pair.substring(6);
//
//                    String[] kv = pair.trim().split(" ");
//                    if (kv.length == 2)
//                        this.args[i] = new Pair<String, Class>(kv[1], Object.class);
//                    else
//                        this.args[i] = new Pair<String, Class>(kv[0], Object.class);
//                }
                int from = 0;
                ArrayList<Pair<String, Class>> l = new ArrayList<Pair<String, Class>>();
                while ((from = findNextArgumentStart(args, from)) != -1) {
                    int classStart = from;
                    int classEnd = findNextArgumentClassEnd(args, classStart);
                    if (args.startsWith("final ", classStart)) {
                        classStart = findNextArgumentStart(args, classEnd);
                        classEnd = findNextArgumentClassEnd(args, classStart);
                    }
                    if (classEnd == args.length() || args.charAt(classEnd) == ',') {
                        l.add(new Pair<String, Class>(args.substring(classStart, classEnd), Object.class));
                        from = classEnd + 1;
                    } else {
                        int nameStart = findNextArgumentStart(args, classEnd);
                        int nameEnd = findNextArgumentNameEnd(args, nameStart);
                        if (nameStart == nameEnd) {
                            l.add(new Pair<String, Class>(args.substring(classStart, classEnd), Object.class));
                            from = classEnd + 1;
                        } else {
                            l.add(new Pair<String, Class>(args.substring(nameStart, nameEnd), Object.class));
                            from = nameEnd + 1;
                        }
                    }

                }
                this.args = l.toArray(new Pair[l.size()]);
            } else {
                this.args = EMPTY_ARGS;
            }
        }
        return exp;
    }

    protected static int findNextArgumentStart(String args, int from) {
        if (from == -1)
            return -1;

        int to = args.length();
        for (int i = from; i < to; i++) {
            char c = args.charAt(i);
            if (c <= ' ')
                continue;

            return i;
        }
        return -1;
    }

    protected static int findNextArgumentClassEnd(String args, int from) {
        int to = args.length();
        int genericBrackets = 0;
        for (int i = from; i < to; i++) {
            char c = args.charAt(i);
            if (c == '<') {
                genericBrackets++;
            } else if (c == '>') {
                genericBrackets--;
            } else if (genericBrackets == 0 && (c <= ' ' || c == ','))
                return i;
        }
        return to;
    }

    protected static int findNextArgumentNameEnd(String args, int from) {
        if (from == -1)
            return -1;

        int to = args.length();
        for (int i = from; i < to; i++) {
            char c = args.charAt(i);
            if (c <= ' ' || c == ',')
                return i;
        }
        return to;
    }

    public boolean isEmpty() {
        return expressions.isEmpty();
    }

    @Override
    public void run() {
        get();
    }

    @Override
    public Object call() throws Exception {
        return get();
    }

    public int getParametersCount() {
        return args.length;
    }

    public Pair<String, Class> getParameter(int i) {
        return args[i];
    }

    public String getParameterName(int i) {
        return args[i].key;
    }
}
