package org.bordl.utils.evaluation;

import org.bordl.utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: moxa
 * Date: 8/11/13
 */
public class ClosureExpression extends Expression {

    private List<Expression> expressions = new ArrayList<Expression>();
    private Pair<String, Class>[] args;

    @Override
    public Expression clone() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Object get(Map<String, Object> model) {
//        HashMap<String, Object> local = new HashMap<String, Object>(model);
        Object ob = null;
        for (Expression expression : expressions) {
            ob = expression.get(model);
        }
        return ob;
    }

    public Object get(Map<String, Object> model, Object... arg) {
        HashMap<String, Object> local = new HashMap<String, Object>(model);
        if (args != null) {
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
        }
        return ob;
    }

    public void add(Expression expression) {
        expressions.add(expression);
    }

    public String parseArguments(String firstLine) {
        int i = firstLine.indexOf("->");
        if (i > 0 && !EvalUtils.inString(firstLine, 0, i)) {
            String args = firstLine.substring(0, i).trim();
            firstLine = firstLine.substring(i + 2).trim();
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
            this.args = new Pair[1];
            args[0] = new Pair<String, Class>("it", Object.class);
        }
        return firstLine;
    }

    public static void main(String[] args) {
        System.out.println("qwe qwe".split(" ").length);
    }
}
