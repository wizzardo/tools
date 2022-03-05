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

    protected ClosureExpression(String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
    }

    public ClosureExpression(EvaluationContext context) {
        super(context);
    }

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
        ClosureExpression clone = new ClosureExpression(file, lineNumber, linePosition);
        clone.args = args;
        for (Expression expression : expressions) {
            clone.add(expression.clone());
        }
        if (!context.isEmpty()) {
            if (context instanceof EvaluationContext)
                clone.context = ((EvaluationContext) context).clone();
            else
                clone.context = new HashMap<String, Object>(context);
        }

        return clone;
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (int i = 0; i < args.length; i++) {
            Pair<String, Class> arg = args[i];
            if (i > 0)
                sb.append(", ");
            sb.append(arg.value.getSimpleName()).append(' ').append(arg.key);
        }
        if (args.length > 0)
            sb.append(" -> ");
        for (int i = 0; i < expressions.size(); i++) {
            if (i > 0)
                sb.append("; ");
            sb.append(expressions.get(i));
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override
    public Object get() {
        return get(context);
    }

    public Object get(Map<String, Object> model, Object... args) {
        return getAgainst(model, model, args);
    }

    public Object getAgainst(Map<String, Object> model, Object thisObject, Object... arg) {
        HashMap<String, Object> local = context instanceof EvaluationContext ? ((EvaluationContext) context).createLocalContext() : new HashMap<>(context);
        if (context != model && model != null)
            local.putAll(model);

        if (context != thisObject || !context.containsKey("this")) {
//            local.put("delegate", thisObject);
            local.put("this", thisObject);
        }
//        if (model != null)
//            local.put("this", model);

        if (arg != null) {
            if (args.length == 0 && arg.length == 1) {
                local.put("it", Definition.DEFINITION_MARK);
                local.put("it", arg[0]);
            } else {
                if (args.length != arg.length)
                    throw new IllegalArgumentException("wrong number of arguments! there were " + (arg.length) + ", but must be " + args.length);
                for (int i = 0; i < args.length; i++) {
//                if (!args[i].value.isAssignableFrom(arg[i].getClass()))
//                    throw new ClassCastException("Can not cast " + args[i].getClass() + " to " + args[i].value);
                    local.put(args[i].key, Definition.DEFINITION_MARK);
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

    public int findAndParseArguments(String exp, int from, int to, List<String> imports, EvaluationContext model) {
        int i = exp.indexOf("->", from);
        if (i >= 0 && i < to && EvalTools.countOpenBrackets(exp, from, i) == 0 && !EvalTools.inString(exp, from, i)) {
            int next = parseArguments(exp, from, i, imports, model);
            if (next == -1)
                return from;
            return next + 2;
        }
        return from;
    }

    int parseArguments(String exp, int from, int to, List<String> imports, EvaluationContext model) {
        String args = exp.substring(from, to).trim();

        if (args.startsWith("(") && args.endsWith(")"))
            args = args.substring(1, args.length() - 1);

        args = args.trim();
        if (!args.isEmpty()) {
            int start = 0;
            ArrayList<Pair<String, Class>> l = new ArrayList<>();
            while ((start = findNextArgumentStart(args, start)) != -1) {
                int classStart = start;
                int classEnd = findNextArgumentClassEnd(args, classStart);
                if (args.startsWith("final ", classStart)) {
                    classStart = findNextArgumentStart(args, classEnd);
                    classEnd = findNextArgumentClassEnd(args, classStart);
                }

                if (classEnd == args.length() || args.charAt(classEnd) == ',') {
                    if (!isValidName(args, classStart, classEnd)) {
                        return -1;
                    }
                    String name = args.substring(classStart, classEnd);
                    l.add(new Pair<>(name, Object.class));
                    start = classEnd + 1;
                } else {
                    int nameStart = findNextArgumentStart(args, classEnd);
                    int nameEnd = findNextArgumentNameEnd(args, nameStart);
                    if (nameStart == nameEnd) {
                        String name = args.substring(classStart, classEnd);
                        if (!isValidName(args, classStart, classEnd)) {
                            return -1;
                        }
                        l.add(new Pair<>(name, Object.class));
                        start = classEnd + 1;
                    } else {
                        if (!isValidName(args, nameStart, nameEnd)) {
                            return -1;
                        }
                        String name = args.substring(nameStart, nameEnd);
                        String typeName = args.substring(classStart, classEnd);
                        Class type = EvalTools.findClass(typeName, imports, model);
                        l.add(new Pair<>(name, type == null ? Object.class : type));
                        start = nameEnd + 1;
                    }
                }

            }
            this.args = l.toArray(new Pair[l.size()]);
        }
        return to;
    }

    void parseBody(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        List<EvalTools.Statement> statements = EvalTools.getStatements(exp, from, to);
        for (EvalTools.Statement s : statements) {
            switch (s.type) {
                case IF:
                case FOR:
                case WHILE:
                    add(s.prepare(model, functions, imports));
                    break;

                case BLOCK: {
                    List<EvalTools.ExpressionPart> lines = EvalTools.getBlocks(s.statement);
                    if (lines.isEmpty())
                        continue;

                    for (EvalTools.ExpressionPart line : lines) {
                        if (EvalTools.isLineCommented(line))
                            continue;
                        add(EvalTools.prepare(line, model, functions, imports));
                    }
                    break;
                }

                default:
                    throw new IllegalStateException("not implemented yet");
            }
        }
    }

    protected static boolean isValidName(String name) {
        return isValidName(name, 0, name.length());
    }

    protected static boolean isValidName(String name, int from, int to) {
        for (int i = from; i < to; i++) {
            char c = name.charAt(i);
            if (c >= 'a' && c <= 'z')
                continue;
            if (c >= 'A' && c <= 'Z')
                continue;
            if (c >= '0' && c <= '9')
                continue;
            if (c == '_' || c == '$')
                continue;
            return false;
        }
        return true;
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
