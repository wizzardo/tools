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
    protected Pair<String, Class>[] args = DEFAULT_ARGS;
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
        local.put("this", model);
        if (!(args.length == 1 && args[0].key.equals("it") && (arg == null || arg.length == 0))) {
            if (args.length != arg.length)
                throw new IllegalArgumentException("wrong number of arguments! there were " + arg.length + ", but must be " + args.length);
            for (int i = 0; i < args.length; i++) {
//                if (!args[i].value.isAssignableFrom(arg[i].getClass()))
//                    throw new ClassCastException("Can not cast " + args[i].getClass() + " to " + args[i].value);
                local.put(args[i].key, arg[i]);
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

            args = args.trim();
            if (!args.isEmpty()) {
                String[] pairs = args.split(",");
                this.args = new Pair[pairs.length];

                for (i = 0; i < pairs.length; i++) {
                    String[] kv = pairs[i].trim().split(" ");
                    if (kv.length == 2)
                        this.args[i] = new Pair<String, Class>(kv[1], Object.class);
                    else
                        this.args[i] = new Pair<String, Class>(kv[0], Object.class);
                }
            } else {
                this.args = EMPTY_ARGS;
            }
        }
        return exp;
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
