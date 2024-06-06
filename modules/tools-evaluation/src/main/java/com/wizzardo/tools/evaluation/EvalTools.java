/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.misc.Pair;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Moxa
 */
public class EvalTools {
    static final String CONSTRUCTOR = "%constructor%";
    static EvaluatingStrategy defaultEvaluatingStrategy;
    private static AtomicInteger variableCounter = new AtomicInteger();
    private static final Pattern CLEAN_CLASS = Pattern.compile("(([a-z]+\\.)*(\\b[A-Z]?[a-zA-Z\\d_]+)(\\.[A-Z]?[a-zA-Z\\d_]+)*)(\\[)?]?");
    private static final Pattern TYPE = Pattern.compile(CLEAN_CLASS.pattern() + "(?<generics>\\<([\\s<]*" + CLEAN_CLASS.pattern() + "[\\s>,]*)*\\>)*");
    private static final Pattern NEW = Pattern.compile("^new +" + CLEAN_CLASS.pattern() + "(\\<(\\s*" + CLEAN_CLASS.pattern() + "\\s*,*\\s*)*\\>)*");
    private static final Pattern CLASS = Pattern.compile("^" + CLEAN_CLASS.pattern());
    private static final Pattern CAST = Pattern.compile("^\\(" + CLEAN_CLASS.pattern() + "(\\<([\\s<]*" + CLEAN_CLASS.pattern() + "[\\s>,]*)+\\>)?" + "\\)");
    private static final Pattern FUNCTION = Pattern.compile("^([a-z_]+\\w*)\\(.+");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern MAP_KEY_VALUE = Pattern.compile("[a-zA-Z\\d]+ *: *.+");
    private static final Pattern IF_FOR_WHILE = Pattern.compile("(if|for|while) *\\(");
    private static final Pattern LIST = Pattern.compile("^([a-z]+[a-zA-Z\\d]*)\\[");
    private static final Pattern VARIABLE = Pattern.compile("\\$([\\.a-z]+[\\.a-zA-Z]*)");
    private static final Pattern ACTIONS = Pattern.compile("\\+\\+|--|->|\\.\\.|\\?:|\\?\\.|\\*=|\\*(?!\\.)|/=?|\\+=?|-=?|:|<<|>>>|>>|<=?|>=?|==?|%|!=?|\\?|&&?|\\|\\|?|instanceof");
    //    private static final Pattern DEF = Pattern.compile("((?:static|final|private|protected|public|volatile|transient|\\s+)+)*(<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>)?\\s*(def\\s+|[a-zA-Z_\\d\\.]+(?:\\s*<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>|\\s*\\[\\])*)(\\s+[a-zA-Z_]+[a-zA-Z_\\d]*)? *($|=|\\()", Pattern.MULTILINE);
    private static final Pattern DEF_METHOD = Pattern.compile("^((?:static|final|private|protected|public|synchronized|\\s+)+)*(<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>)?\\s*(void\\s+|[a-zA-Z_\\d\\.]+(?:\\s*<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>|\\s*\\[\\])*)(\\s+[a-zA-Z_]+[a-zA-Z_\\d]*)? *(\\()");
    private static final Pattern DEF_VARIABLE = Pattern.compile("^((?:static|final|private|protected|public|volatile|transient|\\s+)+)*(def\\s+|[a-zA-Z_\\d\\.]+(?:\\s*<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>|\\s*\\[\\])*)\\s+([a-zA-Z_]+[a-zA-Z_\\d]*) *($|=|;)");
    private static final Pattern BRACKETS = Pattern.compile("[\\(\\)]");
    private static final Pattern CLASS_DEF = Pattern.compile("(static|private|protected|public|abstract|\\s+)*\\b(class|enum|interface) +([A-Za-z0-9_]+)" + "(\\<(\\s*" + CLEAN_CLASS.pattern() + "\\s*,*\\s*)*\\>)*" +
            "(?<extends> +extends +" + CLEAN_CLASS.pattern() + "(\\<(\\s*" + CLEAN_CLASS.pattern() + "\\s*,*\\s*)*\\>)*" + ")?" +
            "(?<implements> +implements(,? +" + CLEAN_CLASS.pattern() + "(\\<(\\s*" + CLEAN_CLASS.pattern() + "\\s*,*\\s*)*\\>)*" + ")+)?" +
            " *\\{");
    private static final Pattern STATIC_BLOCK = Pattern.compile("(static) +\\{");
    private static final Pattern JAVA_LAMBDA = Pattern.compile("^(\\(([A-Za-z0-9_]+\\s*,?\\s*)+\\)|\\(\\s*\\)|[A-Za-z0-9_]+)\\s*->\\s*(\\{)?");

    protected static int countOpenBrackets(String s, int from, int to) {
        int n = 0;
        boolean inString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if ((c == '\'' || c == '\"') && (i == 0 || s.charAt(i - 1) != '\\')) {
                    quote = c;
                    inString = true;
                    continue;
                }
                if (c == '(' || c == '[' || c == '{') {
                    n++;
                } else if (c == ')' || c == ']' || c == '}') {
                    n--;
                }
            } else if (c == quote && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        return n;
    }

    protected static int findCloseBracket(String s, int from) {
        return findCloseBracket(s, from, s.length());
    }

    protected static int findCloseBracket(String s, int from, int to) {
        int n = 1;
        boolean inString = false;
        char quote = 0;
        int i;
        for (i = from; i < to && n > 0; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if ((c == '\'' || c == '\"') && (i == 0 || s.charAt(i - 1) != '\\')) {
                    quote = c;
                    inString = true;
                    continue;
                }
                if (c == '(' || c == '[' || c == '{') {
                    n++;
                } else if (c == ')' || c == ']' || c == '}') {
                    n--;
                }
            } else if ((c == quote) && i >= 1 && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        if (n == 0)
            return i - 1;
        else
            return -1;
    }

    protected static String getTempVariableName() {
        return "__tempVariable_" + variableCounter.incrementAndGet();
    }

    protected static boolean inString(String s, int from, int to) {
        boolean inString = false;
        boolean inMultilineString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if (c == '\"' && to - i > 3 && s.charAt(i + 1) == '\"' && s.charAt(i + 2) == '\"') {
                    inMultilineString = !inMultilineString;
                } else if (inMultilineString)
                    continue;

                if ((c == '\'' || c == '\"') && (i == 0 || s.charAt(i - 1) != '\\')) {
                    quote = c;
                    inString = true;
                }
            } else if (c == quote && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        return inMultilineString || inString;
    }

    protected static boolean inString(StringBuilder s, int from, int to) {
        boolean inString = false;
        boolean inMultilineString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if (c == '\"' && to - i > 3 && s.charAt(i + 1) == '\"' && s.charAt(i + 2) == '\"') {
                    inMultilineString = !inMultilineString;
                } else if (inMultilineString)
                    continue;

                if ((c == '\'' || c == '\"') && (i == 0 || s.charAt(i - 1) != '\\')) {
                    quote = c;
                    inString = true;
                }
            } else if (c == quote && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        return inMultilineString || inString;
    }

    protected static boolean isTemplate(String s, int from, int to) {
        boolean inString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if (c == '\"') {
                    quote = c;
                    inString = true;
                }
            } else {
                if (c == '{' && s.charAt(i - 1) == '$') {
                    int next = findCloseBracket(s, i + 1, to);
                    if (next == -1)
                        throw new IllegalStateException("Unfinished expression at " + i + ": " + s);
                    i = next;
                } else if (c == quote && s.charAt(i - 1) != '\\') {
                    inString = false;
                    if (i != to - 1)
                        return false;
                }
            }
        }
        return inString;
    }

    protected static List<String> getParts(String s) {
        return getParts(s, 0, s.length()).stream().map(it -> it.toString().trim()).collect(Collectors.toList());
    }

    static int trimLeft(String exp, int from, int to) {
        while (from < to && exp.charAt(from) <= ' ')
            from++;
        return from;
    }

    static int trimRight(String exp, int from, int to) {
        while (from < to && exp.charAt(to - 1) <= ' ')
            to--;
        return to;
    }

    public static class ExpressionPart implements CharSequence {
        final String source;
        final int from;
        final int to;
        final int start;
        final int end;
        final int startBracketsTrimmed;
        final int endBracketsTrimmed;
        int hash;

        public ExpressionPart(String source) {
            this(source, 0, source.length());
        }

        public ExpressionPart(String source, int from, int to) {
            this.source = source;
            this.from = from;
            this.to = to;
            int start = trimLeft(source, from, to);
            int end = trimRight(source, start, to);
            this.start = start;
            this.end = end;
            int brackets = countWrappingBrackets(source, start, end);
            while (brackets > 0) {
                start = trimLeft(source, start + 1, end - 1);
                end = trimRight(source, start, end - 1);
                brackets--;
            }
            this.startBracketsTrimmed = start;
            this.endBracketsTrimmed = end;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            if (index < 0 || index >= end)
                throw new IndexOutOfBoundsException();
            return source.charAt(start + index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new ExpressionPart(source, this.start + start, this.start + end);
        }

        @Override
        public String toString() {
            return source.substring(start, end);
        }

        public String trimBrackets() {
            return source.substring(startBracketsTrimmed, endBracketsTrimmed);
        }

        public boolean startsWith(String s) {
            return source.startsWith(s, start);
        }

        public boolean startsWith(String s, int i) {
            return source.startsWith(s, start + i);
        }

        public boolean endsWith(String s) {
            return source.startsWith(s, end - s.length());
        }

        public String substring(int i) {
            return source.substring(start + i, end);
        }

        public String substring(int from, int to) {
            return source.substring(start + from, start + to);
        }

        public boolean wrappedWith(String starts, String ends) {
            return startsWith(starts) && endsWith(ends);
        }

        @Override
        public int hashCode() {
            if (hash != 0)
                return hash;
            int h = 0;
            for (int i = start; i < end; i++) {
                h = 31 * h + source.charAt(i);
            }
            return hash = h;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof String) {
                String s = String.valueOf(obj);
                return s.length() == end - start && source.startsWith(s, start);
            }
            if (obj instanceof ExpressionPart) {
                ExpressionPart other = (ExpressionPart) obj;
                return start == other.start && end == other.end && source.equals(other.source);
            }
            return super.equals(obj);
        }

        public boolean contains(String s) {
            int i = source.indexOf(s, start);
            return i >= 0 && i < end;
        }

        public boolean isNotBlank() {
            return end - start > 0;
        }

        public int indexOf(char c) {
            int i = source.indexOf(c, start);
            return i >= end ? -1 : i;
        }
    }

    protected static LinkedList<ExpressionPart> getParts(String s, int from, int to) {
        LinkedList<ExpressionPart> l = new LinkedList<ExpressionPart>();
        boolean inString = false;
        boolean spaceSeparator = false;
        char quote = 0;
        int brackets = 0;
        int squareBrackets = 0;
        int curlyBraces = 0;
        loop:
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                switch (c) {
                    case '"':
                    case '\'': {
                        quote = c;
                        inString = true;
                        break;
                    }
                    case ' ': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            spaceSeparator = true;
                            ExpressionPart part = new ExpressionPart(s, from, i);
                            if (part.isNotBlank()) {
                                l.add(part);
                                from = i + 1;
                            }
                        }

                        break;
                    }
                    case '(': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            ExpressionPart part = new ExpressionPart(s, from, i);
                            if (part.isNotBlank())
                                l.add(part);
                            from = i;
                        }
                        brackets++;
                        break;
                    }
                    case '{': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            ExpressionPart part = new ExpressionPart(s, from, i);
                            if (part.isNotBlank())
                                l.add(part);
                            from = i;
                        }
                        curlyBraces++;
                        break;
                    }
                    case '[': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            ExpressionPart part = new ExpressionPart(s, from, i);
                            if (part.isNotBlank())
                                l.add(part);
                            from = i;
                        }
                        squareBrackets++;
                        break;
                    }
                    case ')': {
                        brackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new ExpressionPart(s, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case '}': {
                        curlyBraces--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new ExpressionPart(s, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case ']': {
                        squareBrackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(new ExpressionPart(s, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case '.': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            if (i > 0 && (s.charAt(i - 1) == '*' || s.charAt(i - 1) == '?'))
                                if (i - from == 1)
                                    continue;
                                else
                                    i--;

                            ExpressionPart part = new ExpressionPart(s, from, i);
                            if (part.isNotBlank())
                                l.add(part);
                            from = i;
                        }
                        break;
                    }
                }
            } else if ((c == quote) && i > 1 && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        if (from != to) {
            l.add(new ExpressionPart(s, from, to));
        }
        return l;
    }

    private static String trim(char[] chars, int from, int to) {
        while (to > 0 && chars[to - 1] <= ' ') {
            to--;
        }
        return new String(chars, from, to - from);
    }

    public static enum EvaluatingStrategy {
        DEFAULT_JAVA, FLOAT, DOUBLE
    }

    public static void setDefaultEvaluatingStrategy(EvaluatingStrategy defaultEvaluatingStrategy) {
        EvalTools.defaultEvaluatingStrategy = defaultEvaluatingStrategy;
    }

    static int countWrappingBrackets(String s, int from, int to) {
        int count = 0;
        while (isWrappingBrackets(s, from, to)) {
            count++;
            from++;
            to--;
            from = trimLeft(s, from, to);
            to = trimRight(s, from, to);
        }
        return count;
    }

    static boolean isWrappingBrackets(String s, int from, int to) {
        if (!(s.startsWith("(", from) && s.startsWith(")", to - 1)))
            return false;

        int brackets = 0;
        boolean inString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            char c = s.charAt(i);
            if (!inString) {
                if (c == '\"' || c == '\'') {
                    quote = c;
                    inString = true;
                } else if (c == '(') {
                    brackets++;
                } else if (c == ')') {
                    brackets--;
                    if (brackets == 0 && i != to - 1)
                        return false;
                }
            } else {
                if (c == quote && s.charAt(i - 1) != '\\') {
                    inString = false;
                }
            }
        }
        return brackets == 0;
    }

    private static boolean isMap(String s, int from, int to) {
        if (!s.startsWith("[", from) || !s.startsWith("]", to - 1)) {
            return false;
        }
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        int brackets = 0;
        for (int i = from + 1; i < to; i++) {
            switch (s.charAt(i)) {
                case '\'':
                    if (!quotesDouble)
                        quotesSingle = !quotesSingle;
                    break;
                case '"':
                    if (!quotesSingle)
                        quotesDouble = !quotesDouble;
                    break;
                case '[': {
                    if (!quotesSingle && !quotesDouble)
                        brackets++;
                    break;
                }
                case ']': {
                    if (!quotesSingle && !quotesDouble)
                        brackets--;
                    break;
                }
                case ':':
                    if (!quotesSingle && !quotesDouble && brackets == 0) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static boolean isList(String s, int from, int to) {
        return s.startsWith("[", from) && s.startsWith("]", to - 1);
    }

    private static boolean isClosure(String exp, int from, int to) {
        if (exp.startsWith("{", from) && exp.startsWith("}", to - 1))
            return true;

        return false;
    }

    static boolean isJavaLambda(String exp, int from, int to) {
        int i = exp.indexOf("->", from);
        if (i < 0 || i >= to)
            return false;

        int eq = exp.indexOf("=", from);
        if (eq > 0 && eq < i)
            return false;

        if (exp.startsWith("return ", from))
            return false;

        if (!inString(exp, from, i) && countOpenBrackets(exp, from, i) == 0) {
            return true;
        }

        return false;
    }

    static List<String> getBlocks(String exp) {
        return getBlocks(exp, 0, exp.length(), false).stream().map(it -> it.toString().trim()).collect(Collectors.toList());
    }

    static List<ExpressionPart> getBlocks(ExpressionPart exp) {
        return getBlocks(exp, false);
    }

    static class Statement {
        Type type = Type.BLOCK;
        ExpressionPart statement;
        ExpressionPart body;
        ExpressionPart optional;
        Statement bodyStatement;
        Statement optionalStatement;

        public Statement(ExpressionPart statement) {
            this.statement = statement;
        }

        public Statement() {
        }

        public Expression prepare(EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
            switch (type) {
                case IF: {
                    List<ExpressionPart> args = getBlocks(statement, true);
                    if (args.size() > 1)
                        throw new IllegalStateException("more then one statement in condition: " + statement);

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(0), model, functions, imports), model);
                    Expression then;
                    if (bodyStatement != null)
                        then = bodyStatement.prepare(model, functions, imports);
                    else
                        then = EvalTools.prepare(body, model, functions, imports);

                    if (then == null)
                        then = Expression.Holder.NULL;

                    Expression elseExpression;
                    if (optionalStatement != null)
                        elseExpression = optionalStatement.prepare(model, functions, imports);
                    else
                        elseExpression = EvalTools.prepare(optional, model, functions, imports);

                    if (elseExpression == null)
                        return new IfExpression(condition, then, model);
                    else
                        return new IfExpression(condition, then, elseExpression, model);
                }
                case WHILE: {
                    List<ExpressionPart> args = getBlocks(statement, true);
                    if (args.size() > 1)
                        throw new IllegalStateException("more then one statement in condition: " + statement);

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(0), model, functions, imports), model);
                    Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                    return new WhileExpression(condition, then, model);
                }
                case FOR: {
                    List<ExpressionPart> args = getBlocks(statement, true, true);
                    if (args.size() == 1 && args.get(0).contains(":")) {
                        String[] split = args.get(0).toString().split(":", 2);
                        Expression def = EvalTools.prepare(split[0].trim(), model, functions, imports);
                        Expression iterable = EvalTools.prepare(split[1].trim(), model, functions, imports);

                        Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                        return new ForEachExpression(def, iterable, then, model);
                    }
                    if (args.size() == 1 && args.get(0).contains(" in ")) {
                        String[] split = args.get(0).toString().split(" +in +", 2);
                        Expression def = EvalTools.prepare(split[0].trim(), model, functions, imports);
                        Expression iterable = EvalTools.prepare(split[1].trim(), model, functions, imports);

                        Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                        return new ForEachExpression(def, iterable, then, model);
                    }
                    if (args.size() != 3)
                        throw new IllegalStateException("wrong number of statements: " + args);

                    Expression def = EvalTools.prepare(args.get(0), model, functions, imports);
                    if (def == null)
                        def = Expression.Holder.NULL;
                    Expression iterator = EvalTools.prepare(args.get(2), model, functions, imports);
                    if (iterator == null) {
                        iterator = Expression.Holder.NULL;
                    }

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(1), model, functions, imports), model);
                    Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                    return new ForExpression(def, condition, iterator, then, model);
                }
                default:
                    throw new IllegalStateException("not implemented yet");
            }
        }

        static enum Type {
            FOR, WHILE, IF, BLOCK;
        }
    }

    static List<Statement> getStatements(String s) {
        return getStatements(s, 0, s.length());
    }

    static List<Statement> getStatements(String s, int from, int to) {
        List<Statement> statements = new ArrayList<Statement>();
        Matcher m = IF_FOR_WHILE.matcher(s);
        int start = from;
        int searchOffset = from;
        while (m.find(searchOffset) && m.start() < to) {
            if (countOpenBrackets(s, start, m.start()) != 0) {
                searchOffset = m.end();
                continue;
            }

            if (m.start() != start) {
//                statements.add(new Statement(s.substring(start, m.start())));
                statements.add(new Statement(new ExpressionPart(s, start, m.start())));
            }

            Statement statement = new Statement();
            start = getBlock(s, m.start(), to, statement);
            searchOffset = start;
            statements.add(statement.bodyStatement);
        }
        if (start != to) {
//            String sub = s.substring(start, to).trim();
            ExpressionPart expressionPart = new ExpressionPart(s, start, to);
            if (expressionPart.end - expressionPart.start > 0)
                statements.add(new Statement(expressionPart));
        }
        return statements;
    }

    static int getBlock(String s, int from, int to, Statement statement) {
        Matcher m = IF_FOR_WHILE.matcher(s);
        int start = from;
        if (m.find(start) && m.start() < to) {
            ExpressionPart before = new ExpressionPart(s, start, m.start());
            if (before.isNotBlank()) {
                return getStringBlock(s, from, to, statement);
            }
            if (m.start() >= to || m.end() >= to)
                return -1;

            int close = findCloseBracket(s, m.end());
            if (close < 0)
                throw new IllegalStateException("can't find closing bracket in expression: " + s);

            Statement inner = new Statement();

            if (statement.bodyStatement == null && statement.body == null)
                statement.bodyStatement = inner;
            else
                statement.optionalStatement = inner;

            inner.type = Statement.Type.valueOf(m.group(1).toUpperCase());
//            inner.statement = s.substring(m.end(), close);
            inner.statement = new ExpressionPart(s, m.end(), close);

            char ch = 0;
            for (start = close + 1; start < to; start++) {
                ch = s.charAt(start);
                if (ch != ' ' && ch != '\n' && ch != '\t')
                    break;
            }
            if (start == to)
                throw new IllegalStateException("can't find block: " + s.substring(from, to));

            if (ch == '{') {
                start++;
                close = findCloseBracket(s, start, to);

                if (close < 0)
                    throw new IllegalStateException("can't find closing bracket in expression: " + s.substring(from, to));

//                inner.body = s.substring(start, close);
                inner.body = new ExpressionPart(s, start, close);
                start = close + 1;

                if (inner.type == Statement.Type.IF)
                    return getElse(s, start, to, inner);
                return start;
            } else {
                start = getBlock(s, start, to, inner);

                if (inner.type == Statement.Type.IF)
                    return getElse(s, start, to, inner);
                return start;

            }
        } else {
            return getStringBlock(s, from, to, statement);
        }
    }

    static int getStringBlock(String s, int from, int to, Statement statement) {
        char last = 0, stringChar = 0;
        boolean inString = false;
        int brackets = 0;
        char c;
        for (int i = from; i < to; i++) {
            c = s.charAt(i);
            if (inString) {
                if (c == stringChar && last != '\\') {
                    inString = false;
                }
            } else {
                if (c == '(' || c == '[' || c == '{') {
                    brackets++;
                } else if (c == ')' || c == ']' || c == '}') {
                    brackets--;
                }

                if (brackets == 0) {
                    if (c == ';' || c == '\n') {
                        if (statement.bodyStatement == null && statement.body == null) {
//                            statement.body = s.substring(from, i).trim();
                            statement.body = new ExpressionPart(s, from, i);
                        } else {
//                            statement.optional = s.substring(from, i).trim();
                            statement.optional = new ExpressionPart(s, from, i);
                        }

                        return i + (c == ';' ? 1 : 0);
                    }
                    if (c == '"' || c == '\'') {
                        inString = true;
                        stringChar = c;
                    }
                }
            }
            last = c;
        }
        if (statement.bodyStatement == null && statement.body == null) {
//            statement.body = s.substring(from, to).trim();
            statement.body = new ExpressionPart(s, from, to);
        } else {
//            statement.optional = s.substring(from, to).trim();
            statement.optional = new ExpressionPart(s, from, to);
        }
        return to;
    }

    static int getElse(String s, int from, int to, Statement statement) {
        int start = from;
        char ch;
        int close;
        for (; start < to; start++) {
            ch = s.charAt(start);
            if (ch != ' ' && ch != '\n' && ch != '\t')
                break;
        }

        if (!(start < to - 4 && s.startsWith("else", start)))
            return start;

        start += 4;
        ch = s.charAt(start);
        if (ch != ' ' && ch != '\n' && ch != '\t' && ch != '{' && ch != ';')
            return start - 4;

        for (; start < to; start++) {
            ch = s.charAt(start);
            if (ch != ' ' && ch != '\n' && ch != '\t')
                break;
        }

        if (start == to)
            throw new IllegalStateException("can't find block: " + s.substring(from, to));

        if (ch == '{') {
            start++;
            close = findCloseBracket(s, start, to);

            if (close < 0)
                throw new IllegalStateException("can't find closing bracket in expression: " + s.substring(from, to));

//            statement.optional = s.substring(start, close);
            statement.optional = new ExpressionPart(s, start, close);
            return close + 1;
        } else {
            return getBlock(s, start, to, statement);
        }
    }

    static List<ExpressionPart> getBlocks(String exp, int from, int to, boolean ignoreNewLine) {
        return getBlocks(exp, from, to, ignoreNewLine, false);
    }

    static List<ExpressionPart> getBlocks(ExpressionPart part, boolean ignoreNewLine) {
        return getBlocks(part, ignoreNewLine, false);
    }

    static List<ExpressionPart> getBlocks(ExpressionPart part, boolean ignoreNewLine, boolean withEmptyStatements) {
        return getBlocks(part.source, part.start, part.end, ignoreNewLine, withEmptyStatements);
    }

    static List<ExpressionPart> getBlocks(String exp, int from, int to, boolean ignoreNewLine, boolean withEmptyStatements) {
        List<ExpressionPart> list = new ArrayList<ExpressionPart>();
        char last = 0, stringChar = 0;
        boolean inString = false;
        int brackets = 0;
        int length = to;
        int start = from;
        boolean lineEndedWithSemicolon = true;
        for (int i = from; i < length; i++) {
            char c = exp.charAt(i);
            if (inString) {
                if (c == stringChar && last != '\\') {
                    inString = false;
                }
            } else {
                if (c == '(' || c == '[' || c == '{') {
                    brackets++;
                } else if (c == ')' || c == ']' || c == '}') {
                    brackets--;
                }

                if (ignoreNewLine && c == '\n')
                    continue;

                if (brackets == 0) {
                    if (c == ';' || c == '\n') {
                        lineEndedWithSemicolon = c == ';';
                        ExpressionPart part = new ExpressionPart(exp, start, i);
                        if (ignoreNewLine)
                            part = removeRN(part);
                        if (withEmptyStatements || part.end - part.start > 0)
                            list.add(part);
                        start = i + 1;
                        continue;
                    }
                    if (c == '"' || c == '\'') {
                        stringChar = c;
                        inString = true;
                    }
                }
            }
            last = c;
        }
        if (to - start > 0 || withEmptyStatements) {
            ExpressionPart part = new ExpressionPart(exp, start, to);
            if (ignoreNewLine)
                part = removeRN(part);
            if (part.end - part.start > 0 || withEmptyStatements)
                list.add(part);
        }

        for (int i = list.size() - 1; i > 0; i--) {
            if (list.get(i).startsWith(".") && !list.get(i - 1).endsWith(";")) {
                list.set(i - 1, new ExpressionPart(exp, list.get(i - 1).from, list.get(i).to));
                list.remove(i);
            }
        }

        return list;
    }

    static ExpressionPart removeRN(ExpressionPart part) {
        int from = part.start;
        int last = from;
        boolean inString = false;
        char quote = 0;
        StringBuilder sb = null;
        for (int i = from; i < part.end; i++) {
            char c = part.source.charAt(i);
            if (!inString) {
                if ((c == '\'' || c == '\"') && (i == 0 || part.source.charAt(i - 1) != '\\')) {
                    quote = c;
                    inString = true;
                    continue;
                }
                if (c == '\n' || c == '\r') {
                    if (i != last) {
                        if (sb == null)
                            sb = new StringBuilder(part.end - part.start);

                        sb.append(part.source, last, i);
                    }
                    last = i + 1;
                }
            } else if (c == quote && part.source.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        if (sb == null)
            return part;

        if (last != part.end) {
            sb.append(part.source, last, part.end);
        }
        return new ExpressionPart(sb.toString());
    }

    static StringBuilder cleanLine(StringBuilder line) {
        int comment = -1;
        while ((comment = line.indexOf("//", comment + 1)) != -1) {
            if (!inString(line, 0, comment)) {
                int end = line.indexOf("\n", comment);
                if (end == -1) {
                    line.setLength(comment);
                } else {
                    line.delete(comment, end);
                }
            }
        }
        return trim(line);
    }

    static StringBuilder trim(StringBuilder sb) {
        int i = 0;
        while (i < sb.length() && sb.charAt(i) <= ' ') {
            i++;
        }
        if (i != 0)
            sb.delete(0, i);

        i = sb.length() - 1;
        while (i > 0 && sb.charAt(i) <= ' ') {
            i--;
        }
        if (i != sb.length() - 1)
            sb.setLength(i + 1);

        return sb;
    }

    public static Expression prepareTemplate(String exp) {
        return prepare(exp, (Map<String, Object>) null, null, null, true);
    }

    public static Expression prepare(String exp) {
        return prepare(exp, null);
    }

    public static Expression prepare(String exp, Map<String, Object> model) {
        return prepare(exp, model, null);
    }

    public static Expression prepare(String script, Map<String, Object> model, Map<String, UserFunction> functions) {
        List<String> imports = new ArrayList<String>();
        script = readImports(script, imports);
        return prepare(script, model, functions, imports, false);
    }

    public static String readImports(String script, List<String> imports) {
        int s, n;
        int position = 0;
        position = skipCommentLine(script, position);
        if (script.startsWith("package", position)) {
            n = script.indexOf("\n");
            s = script.indexOf(";");
            int to = Math.min(n == -1 ? script.length() : n, s == -1 ? script.length() : s);
            imports.add(script.substring(8, to).trim() + ".*");

            position = to + 1;
            position = skipWhitespaces(script, position);
        }

        position = skipCommentLine(script, position);
        while (script.startsWith("import", position)) {
            n = script.indexOf("\n", position);
            s = script.indexOf(";", position);
            int to = Math.min(n == -1 ? script.length() : n, s == -1 ? script.length() : s);
            imports.add(script.substring(position + 7, to).trim());

            position = to + 1;
            position = skipWhitespaces(script, position);
            position = skipCommentLine(script, position);
        }
        if (position != 0)
            return script.substring(position);
        return script;
    }

    public static String readPackage(String script) {
        if (script.startsWith("package")) {
            int n = script.indexOf("\n");
            int s = script.indexOf(";");
            int to = Math.min(n == -1 ? script.length() : n, s == -1 ? script.length() : s);
            return script.substring(8, to).trim();
        }
        return "";
    }

    public static Expression prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions, List<String> imports) {
        return prepare(exp, model, functions, imports, false);
    }

    protected static Expression prepare(String exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        return prepare(exp, model, functions, imports, false);
    }

    protected static Expression prepareClosure(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        boolean isLambda = false;
        if (isClosure(exp, from, to) || (isLambda = isJavaLambda(exp, from, to))) {
            ClosureExpression closure = new ClosureExpression(model);
            if (isLambda) {
                from = closure.findAndParseArguments(exp, from, to, imports, model);
                from = trimLeft(exp, from, to);
                if (exp.startsWith("{", from)) {
                    from++;
                    to--;
                    from = trimLeft(exp, from, to);
                    to = trimRight(exp, from, to);
                }
            } else {
                from++;
                to--;
                from = trimLeft(exp, from, to);
                to = trimRight(exp, from, to);
                from = closure.findAndParseArguments(exp, from, to, imports, model);
            }

            EvaluationContext localModel = model.createLocalContext();
            for (Pair<String, Class> arg : closure.args) {
                localModel.putLocal(arg.key, null);
            }

            closure.parseBody(exp, from, to, model, functions, imports);
            return new ClosureHolder(closure, model);
        }
        return null;
    }

    static class ClassDefinitionResult {
        final int modifiersStart;
        final int modifiersEnd;
        final int typeStart;
        final int typeEnd;
        final int nameStart;
        final int nameEnd;
        final int genericsStart;
        final int genericsEnd;
        final int extendsStart;
        final int extendsEnd;
        final int implementsStart;
        final int implementsEnd;
        final int bodyStart;

        ClassDefinitionResult(int modifiersStart, int modifiersEnd, int typeStart, int typeEnd, int nameStart, int nameEnd, int genericsStart, int genericsEnd, int extendsStart, int extendsEnd, int implementsStart, int implementsEnd, int bodyStart) {
            this.modifiersStart = modifiersStart;
            this.modifiersEnd = modifiersEnd;
            this.typeStart = typeStart;
            this.typeEnd = typeEnd;
            this.nameStart = nameStart;
            this.nameEnd = nameEnd;
            this.genericsStart = genericsStart;
            this.genericsEnd = genericsEnd;
            this.extendsStart = extendsStart;
            this.extendsEnd = extendsEnd;
            this.implementsStart = implementsStart;
            this.implementsEnd = implementsEnd;
            this.bodyStart = bodyStart;
        }
    }

    static ClassDefinitionResult findClassDefinition(String exp, int from, int to) {
        int modifiersEnd = findModifiersEnd(exp, from, to);
        if (modifiersEnd == -1)
            return null;

        int typeStart = skipWhitespaces(exp, modifiersEnd, to);
        if (typeStart == to)
            return null;

        int typeEnd = skipNonWhitespaces(exp, typeStart, to);
        if (typeEnd == to)
            return null;

        if (!(substringEquals(exp, typeStart, typeEnd, "class") || substringEquals(exp, typeStart, typeEnd, "interface") || substringEquals(exp, typeStart, typeEnd, "enum")))
            return null;

        int bodyStart = indexOf(exp, '{', typeEnd, to);
        if (bodyStart == -1)
            return null;

        int nameStart = skipWhitespaces(exp, typeEnd, bodyStart);
        if (nameStart == bodyStart)
            return null;

        int nameEnd = skipNonWhitespaces(exp, nameStart, bodyStart);

        int genericsEnd = -1;
        int genericsStart = indexOf(exp, '<', nameStart, nameEnd);
        if (genericsStart != -1) {
            nameEnd = genericsStart;
            genericsEnd = findGenericsEnd(exp, genericsStart, to);
            if (genericsEnd == -1)
                return null;
        } else {
            genericsStart = skipWhitespaces(exp, typeEnd, bodyStart);
            if (genericsStart != bodyStart && exp.charAt(genericsStart) == '<') {
                genericsEnd = findGenericsEnd(exp, genericsStart, to);
                if (genericsEnd == -1)
                    return null;
            } else {
                genericsStart = -1;
            }
        }

        int extendsStart = indexOfWord(exp, "extends", nameEnd, bodyStart);
        int implementsStart = indexOfWord(exp, "implements", nameEnd, bodyStart);

        int extendsEnd = extendsStart == -1 ? -1 : (implementsStart == -1 ? bodyStart : (implementsStart < extendsStart ? bodyStart : implementsStart));
        int implementsEnd = implementsStart == -1 ? -1 : (extendsStart == -1 ? bodyStart : (extendsStart < implementsStart ? bodyStart : extendsStart));

        bodyStart++;
        return new ClassDefinitionResult(from, modifiersEnd, typeStart, typeEnd, nameStart, nameEnd, genericsStart, genericsEnd, extendsStart, extendsEnd, implementsStart, implementsEnd, bodyStart);
    }

    static int findGenericsEnd(String exp, int from, int to) {
        int genericsEnd = indexOf(exp, '>', from, to);
        if (genericsEnd == -1)
            return -1;
        int subgenerics = from;
        while ((subgenerics = indexOf(exp, '<', subgenerics + 1, genericsEnd)) != -1) {
            genericsEnd = indexOf(exp, '>', genericsEnd + 1, to);
            if (genericsEnd == -1)
                return -1;
        }
        return genericsEnd;
    }

    static int findModifiersEnd(String exp, int from, int to) {
        int last = from;
        do {
            int i = skipWhitespaces(exp, last, to);
            if (i == to)
                return -1;

            last = i;

            i = skipNonWhitespaces(exp, last, to);
            if (i == to)
                return -1;

            if (substringEquals(exp, last, i, "final")) {
                last = i;
                continue;
            }
            if (substringEquals(exp, last, i, "public")) {
                last = i;
                continue;
            }
            if (substringEquals(exp, last, i, "static")) {
                last = i;
                continue;
            }
            if (substringEquals(exp, last, i, "private")) {
                last = i;
                continue;
            }
            if (substringEquals(exp, last, i, "abstract")) {
                last = i;
                continue;
            }
            if (substringEquals(exp, last, i, "protected")) {
                last = i;
                continue;
            }
            return last;
        } while (last != -1);
        return -1;
    }

    protected static Expression prepareClass(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
//        Matcher m = CLASS_DEF.matcher(exp);
//        boolean find = m.find(from);
        ClassDefinitionResult cd = findClassDefinition(exp, from, to);
//        if (find && m.start() < to && classDefinition == null)
//            throw new RuntimeException();
//        if (!find && classDefinition != null)
//            throw new RuntimeException();

        if (cd != null && findCloseBracket(exp, cd.bodyStart) == to - 1) {
            String className = exp.substring(cd.nameStart, cd.nameEnd);

            if (substringEquals(exp, cd.typeStart, cd.typeEnd, "interface")) {
                class NoopExpression extends Expression {
                    protected NoopExpression(EvaluationContext context) {
                        super(context);
                    }

                    @Override
                    public void setVariable(Variable v) {

                    }

                    @Override
                    public Expression clone() {
                        return new NoopExpression(null);
                    }

                    @Override
                    protected Object doExecute(Map<String, Object> model) {
                        return null;
                    }
                }
                return new NoopExpression(model);
            }

            boolean isEnum = substringEquals(exp, cd.typeStart, cd.typeEnd, "enum");
            List<Type> interfaces = new ArrayList<>();
            String anImplements = cd.implementsStart == -1 ? null : exp.substring(cd.implementsStart, cd.implementsEnd);
            if (anImplements != null && !anImplements.isEmpty()) {
                anImplements = anImplements.trim();
                anImplements = anImplements.substring("implements ".length()).trim();
                Matcher interfacesMatcher = TYPE.matcher(anImplements);

                while (interfacesMatcher.find()) {
                    String name = interfacesMatcher.group(1).trim();
                    Type type = findType(name, interfacesMatcher.group("generics"), imports, model);
                    Class aClass;
                    if (type == null)
                        throw new IllegalStateException("Cannot find interface to implement: " + name);

                    if (type instanceof ParameterizedType) {
                        aClass = (Class) ((ParameterizedType) type).getRawType();
                    } else {
                        aClass = (Class) type;
                    }

                    if (!aClass.isInterface())
                        throw new IllegalStateException("Cannot implement " + name + " - it's not an interface!");

                    interfaces.add(type);
                }
            }

            Type superClass = Object.class;
            String anExtends = cd.extendsStart == -1 ? null : exp.substring(cd.extendsStart, cd.extendsEnd);
            if (anExtends != null && !anExtends.isEmpty()) {
                anExtends = anExtends.trim();
                anExtends = anExtends.substring("extends ".length()).trim();
                Matcher typeMatcher = TYPE.matcher(anExtends);

                if (typeMatcher.find()) {
                    String toExtend = typeMatcher.group(1);
                    superClass = findType(toExtend.trim(), typeMatcher.group("generics"), imports, model);
                    if (superClass == null)
                        throw new IllegalStateException("Cannot find class to extend: " + toExtend);
                } else
                    throw new IllegalStateException("Cannot find class to extend: " + anExtends);
            }

            List<ExpressionPart> lines = getBlocks(exp, cd.bodyStart, to - 1, false);

            List<Expression> definitions = new ArrayList<Expression>();
            List<Expression> definitionsStatic = new ArrayList<Expression>();
            ClassExpression classExpression = new ClassExpression(className, definitions, definitionsStatic, superClass, interfaces.toArray(new Type[interfaces.size()]), model);
            String parentClass = null;
            if (model.getRoot() instanceof ScriptEngine.Binding) {
                ScriptEngine.Binding binding = (ScriptEngine.Binding) model.getRoot();
                if (binding.currentClass == null)
                    classExpression.packageName = binding.pack;
                else {
                    classExpression.packageName = binding.pack + "." + binding.currentClass;
                    parentClass = binding.currentClass;
                }
                binding.currentClass = className;
            }
            model.getRoot().put("class " + className, classExpression);
            Object prevClass = model.put("current class", classExpression);

            for (int i = isEnum ? 1 : 0; i < lines.size(); i++) {
                ExpressionPart s = lines.get(i);
                if (isLineCommented(s))
                    continue;

                boolean isStatic = false;
                Matcher staticMatcher = STATIC_BLOCK.matcher(s.source);
                if (staticMatcher.find(s.start) && staticMatcher.start() < s.end) {
//                    s = s.substring("static ".length()).trim();
                    isStatic = true;
                    s = new ExpressionPart(s.source, s.start + 7, s.end);
                }
//
//                if (s.startsWith(className) && s.charAt(className.length()) == '(') {
//                    s = "protected " + s; // workaround for default access modifier
//                }

                Expression prepare = prepare(s, model, functions, imports);
                if (prepare instanceof ClosureHolder) {
                    ClosureExpression closure = ((ClosureHolder) prepare).closure;
                    closure.setContext(classExpression.context);
                    prepare = closure;
                } else if (prepare instanceof Expression.DefineAndSet) {
                    isStatic = ((Expression.DefineAndSet) prepare).modifiers.contains("static");
                } else if (prepare instanceof Expression.Definition) {
                    isStatic = ((Expression.Definition) prepare).modifiers.contains("static");
                } else if (prepare instanceof Expression.MethodDefinition) {
                    isStatic = ((Expression.MethodDefinition) prepare).modifiers.contains("static");
                }

                if (isStatic)
                    definitionsStatic.add(prepare);
                else
                    definitions.add(prepare);
            }

            model.put("current class", prevClass);
            classExpression.initStatic();

            if (isEnum) {
                classExpression.isEnum = true;
                ExpressionPart part = lines.get(0);
                List<ExpressionPart> enums = parseArgs(part.source, part.start, part.end);
                for (int i = 0; i < enums.size(); i++) {
                    ExpressionPart s = enums.get(i);
                    int argsStart = s.indexOf('(');

                    String name;
                    Object[] args = null;

                    if (argsStart != -1) {
                        name = s.substring(0, argsStart);

                        ExpressionPart argsRaw = new ExpressionPart(s.source, s.start + name.length(), s.to);
                        if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                            List<Expression> arr = prepareArgs(argsRaw.source, argsRaw.startBracketsTrimmed, argsRaw.endBracketsTrimmed, model, functions, imports);
                            args = arr.toArray(new Expression[arr.size()]);
                        } else {
                            args = new Object[0];
                        }
                    } else {
                        name = s.toString();
                        args = new Object[0];
                    }
                    classExpression.context.put(name, classExpression.newInstance(args));
                }
            }

            if (model.getRoot() instanceof ScriptEngine.Binding) {
                ScriptEngine.Binding binding = (ScriptEngine.Binding) model.getRoot();
                binding.currentClass = parentClass;
            }

            return classExpression;
        }

        return null;
    }

    protected static Expression prepareBlock(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        List<Statement> statements = getStatements(exp, from, to);
        Expression.BlockExpression block = new Expression.BlockExpression(model);
        for (Statement s : statements) {
            switch (s.type) {
                case IF:
                case FOR:
                case WHILE:
                    block.add(s.prepare(model, functions, imports));
                    break;

                case BLOCK: {
                    List<ExpressionPart> lines = getBlocks(s.statement);
                    if (lines.isEmpty())
                        continue;

                    if (lines.size() > 1) {
                        for (ExpressionPart line : lines) {
                            if (isLineCommented(line))
                                continue;
                            block.add(prepare(line, model, functions, imports));
                        }
                    } else if (statements.size() > 1 || !lines.get(0).equals(s.statement)) {
                        block.add(prepare(lines.get(0), model, functions, imports));
                    }
                    break;
                }

                default:
                    throw new IllegalStateException("not implemented yet");
            }

        }

        if (block.size() == 1)
            return block.get(0);

        if (!block.isEmpty())
            return block;

        return null;
    }

    protected static Expression prepareVariableDefinition(ExpressionPart exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        Matcher m = DEF_VARIABLE.matcher(exp);
        if (m.find() && m.start() == 0) {
            String modifiers = m.group(1);
            if (modifiers != null)
                modifiers = modifiers.trim();
            else
                modifiers = "";

            String type = m.group(2);
            if (type != null)
                type = type.replaceAll("\\s", "");

            if (!"new".equals(type)) {
                String name = m.group(3);
                if (name != null)
                    name = name.trim();

                if (m.group(4).equals("=")) {
                    if (name != null)
                        model.putLocal(name, null);

                    Expression action = prepare(exp.source, exp.start + m.start(3), exp.end, model, functions, imports, false);
                    if (action instanceof Operation && ((Operation) action).leftPart() instanceof ClassExpression) {
                        ((Operation) action).leftPart(new Expression.Holder(name, model));
                    }
                    Class typeClass = findClass(type, imports, model);
                    return new Expression.DefineAndSet(typeClass, name, action, type, modifiers, model);
                } else {
                    model.putLocal(name, null);

                    if (model.containsKey("class " + type))
                        return new Expression.DefinitionWithClassExpression((ClassExpression) model.get("class " + type), name, model);

                    Class typeClass = findClass(type, imports, model);
                    if (typeClass == null)
                        return new Expression.Definition(type, name, modifiers, model);

                    return new Expression.Definition(typeClass, name, modifiers, model);
                }
            }
        }

        return null;
    }

    protected static Expression prepareMethodDefinition(ExpressionPart exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        Matcher m = DEF_METHOD.matcher(exp);
        if (m.find()) {
            String modifiers = m.group(1);
            if (modifiers != null)
                modifiers = modifiers.trim();
            else
                modifiers = "";

            String generics = m.group(2);
            String type = m.group(3);
            if (type != null)
                type = type.replaceAll("\\s", "");

            if (!"new".equals(type)) {
                String name = m.group(4);
                if (name != null)
                    name = name.trim();
                else if (type.equals("this") || type.equals("super") || type.contains("."))
                    return null;
                else if (model.get("current class") != null && ((ClassExpression) model.get("current class")).getName().equals(type))
                    name = type;
                else
                    return null;

                if (name != null)
                    model.putLocal(name, null);

                int argsEnd = findCloseBracket(exp.source, exp.start + m.end()) - exp.start;
                ClosureHolder closure;
                if (argsEnd == m.end()) {
                    closure = (ClosureHolder) prepare(exp.source, exp.start + argsEnd + 1, exp.end, model, functions, imports, false);
                } else {
                    int blockStart = trimLeft(exp.source, exp.start + argsEnd + 1, exp.end);
                    if (exp.startsWith("throws", blockStart - exp.start)) {
                        blockStart = skipUntil(exp.source, blockStart, '{');
                        if (blockStart == -1)
                            throw new IllegalStateException("Cannot parse: " + exp);
                    }
                    if (!exp.startsWith("{", blockStart - exp.start))
                        throw new IllegalStateException("Cannot parse: " + exp);

                    ClosureExpression c = new ClosureExpression(model);
                    c.parseArguments(exp.source, exp.start + m.end(), exp.start + argsEnd, imports, model);
                    EvaluationContext localModel = model.createLocalContext();
                    for (Pair<String, Class> arg : c.args) {
                        localModel.putLocal(arg.key, null);
                    }
                    c.parseBody(exp.source, blockStart + 1, exp.end - 1, localModel, functions, imports);
                    ClosureHolder closureHolder = new ClosureHolder(c, model);
                    closure = closureHolder;
                }

                Class typeClass = findClass(type, imports, model);
                if (typeClass == null)
                    typeClass = Object.class;
                return new Expression.MethodDefinition(modifiers, typeClass, name == null ? type : name, closure, model);
            }
        }

        return null;
    }

    protected static Expression prepareAction(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        Matcher m = ACTIONS.matcher(exp);
        List<Operation> operations = new ArrayList<Operation>();
        int last = from;
        int position = from;
        Operation operation = null;
        Expression lastExpressionHolder = null;
        boolean ternary = false;
        int ternaryInner = 0;
        while (m.find(position)) {
            int start = m.start();
            if (start >= to)
                break;

            String find = m.group();
            if ("->".equals(find) && operation != null && !inString(exp, from, start) && countOpenBrackets(exp, from, start) == 0) {
                Expression closure = prepareClosure(exp, last, to, model, functions, imports);
                operation.rightPart(closure);
                position = m.end();
                return operation;
            }
            if (ternary) {
                if (inString(exp, 0, start)) {
                    position = m.end();
                    continue;
                }

                if (find.equals("?")) {
                    ternaryInner++;
                    position = m.end();
                    continue;
                }
                if (!find.equals(":")) {
                    position = m.end();
                    continue;
                }
                if (ternaryInner > 0) {
                    ternaryInner--;
                    position = m.end();
                    continue;
                }
            }
            if ("?.".equals(find)) {
                position = m.end();
                continue;
            }

            if (!inString(exp, from, start) && countOpenBrackets(exp, from, start) == 0) {
                ExpressionPart subexpression = new ExpressionPart(exp, last, start);
                if (subexpression.startsWith("new ") && (find.equals("<") || find.equals(">"))) {
                    position = m.end();
                    continue;
                }
                lastExpressionHolder = prepare(exp, last, start, model, functions, imports, false);
                if (operation != null) {
                    //complete last operation
                    operation.end(start);
                    operation.rightPart(lastExpressionHolder);
                }
                operation = new Operation(lastExpressionHolder, Operator.get(find), last, m.end(), model);
                operations.add(operation);
                //add operation to list
                last = position = m.end();
                if (ternary) {
                    lastExpressionHolder = prepare(exp, last, to, model, functions, imports, false);
                    operation.rightPart(lastExpressionHolder);
                    break;
                }
                if (find.equals("?")) {
                    ternary = true;
                }
            }
            position = m.end();
        }
        if (operation != null) {
            if (last != to) {
                operation.end(to);
                operation.rightPart(prepare(exp, last, to, model, functions, imports, false));
            }

            return prioritize(operations);
        }

        return null;
    }

    protected static Expression prepareTemplate(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
        if (!isTemplate && exp.startsWith("\"\"\"", from) && exp.startsWith("\"\"\"", to - 3) && inString(exp, from, to - 1)) {
            return prepare(exp.substring(from + 3, to - 3), model, functions, imports, true);
        }
        if (!isTemplate && exp.startsWith("\"", from) && exp.startsWith("\"", to - 1) && isTemplate(exp, from, to - 1)) {
            String quote = exp.charAt(from) + "";
            exp = exp.substring(from + 1, to - 1).replace("\\" + quote, quote);
            return prepare(exp, model, functions, imports, true);
        }

        if (isTemplate) {
            TemplateBuilder tb = new TemplateBuilder(model);
            Matcher m = VARIABLE.matcher(exp);
            int start = from;
            int end = from;
            while ((start = exp.indexOf("$", start)) != -1) {
                if (start >= to)
                    break;

                if (start >= 0 && exp.charAt(start + 1) == '{') {
                    if (end != start)
                        tb.append(exp.substring(end, start));

                    end = findCloseBracket(exp, start + 2);
                    String sub = exp.substring(start + 2, end);
                    tb.append(prepare(sub, model, functions, imports, false));
                    end++;
                } else {
                    if (m.find(start)) {
                        if (m.start() != end)
                            tb.append(exp.substring(end, m.start()));
                        String sub = m.group(1);
                        tb.append(prepare(sub, model, functions, imports, false));
                        end = m.end();
                    }
                }
                start = end;
            }
            if (end != to)
                tb.append(exp.substring(end, to));

            return tb;
        }

        return null;
    }

    static class PrepareResolveClassResult {
        final Expression thatObject;
        final int position;

        PrepareResolveClassResult(Expression thatObject, int position) {
            this.thatObject = thatObject;
            this.position = position;
        }
    }

    protected static PrepareResolveClassResult prepareResolveClass(ExpressionPart exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        Matcher m = CLASS.matcher(exp);
        if (m.find()) {
            String className = m.group(1);
            boolean endsWithClass = className.endsWith(".class");
            if (endsWithClass) {
                className = className.substring(0, className.length() - ".class".length());
            } else {
                int dot = className.indexOf(".");
                if (dot != -1) {
                    if (model.containsKey(className.substring(0, dot)))
                        return null;
                } else {
                    if (model.containsKey(exp))
                        return null;
                }

                Class clazz;
                while (dot != -1) {
                    String name = className.substring(0, dot);
                    ClassExpression cl = (ClassExpression) model.get("class " + name);
                    if (cl != null) {
                        return new PrepareResolveClassResult(cl, exp.start + name.length());
                    }

                    clazz = findClass(name, imports, model);
                    int prev = dot;
                    dot = className.indexOf('.', dot + 1);
                    if (clazz != null) {
                        if (dot == -1 || clazz.isEnum()) {
                            return new PrepareResolveClassResult(new Expression.Holder(clazz, model), exp.start + prev);
                        }
                        Field field = Function.findField(clazz, className.substring(prev + 1, dot));
                        if (field != null) {
                            return new PrepareResolveClassResult(new Expression.Holder(clazz, model), exp.start + prev);
                        }
                    }
                }
            }

            StringBuilder sb = new StringBuilder(className);

            Class clazz;
            int i;
            do {
                className = sb.toString();
                ClassExpression cl = (ClassExpression) model.get("class " + className);
                if (cl != null) {
                    return new PrepareResolveClassResult(cl, exp.start + sb.length());
                } else {
                    clazz = findClass(className, imports, model);
                    if (clazz == null) {
                        int lastDot = sb.lastIndexOf(".");
                        if (lastDot != -1) {
                            sb.setCharAt(lastDot, '$');
                            clazz = findClass(sb.toString(), imports, model);
                            sb.setCharAt(lastDot, '.');
                        }
                    }

                    if (clazz != null) {
                        return new PrepareResolveClassResult(new Expression.Holder(clazz, model), exp.start + sb.length());
                    }
                }

                i = sb.lastIndexOf(".");
                if (i > 0)
                    sb.setLength(i);
            } while (i > 0);

            if (endsWithClass) {
                className = m.group(0);
                if (endsWithClass)
                    className = className.substring(0, className.length() - ".class".length());

                return new PrepareResolveClassResult(new Expression.ResolveClass(className, model), exp.start + m.group(0).length());
            }
        }
        return null;
    }

    public static Expression prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
        if (model instanceof EvaluationContext)
            return prepare(exp, (EvaluationContext) model, functions, imports, isTemplate);
        else
            return prepare(exp, new EvaluationContext(model), functions, imports, isTemplate);
    }

    public static Expression prepare(String exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
        if (exp == null)
            return null;
        return prepare(exp, 0, exp.length(), model, functions, imports, isTemplate);
    }

    static Expression prepare(ExpressionPart exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        return prepare(exp, model, functions, imports, false);
    }

    static Expression prepare(ExpressionPart exp, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
        if (exp == null)
            return null;
        return prepare(exp.source, exp.start, exp.end, model, functions, imports, isTemplate);
    }

    public static Expression prepare(String exp, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
        if (exp == null) {
            return null;
        }

        int start = from;
        if (!isTemplate) {
            if (from == 0 && to == exp.length())
                exp = removeComments(exp);

            start = trimLeft(exp, from, to);
            to = trimRight(exp, start, to);

            int brackets = countWrappingBrackets(exp, start, to);
            while (brackets > 0) {
                start++;
                to--;
                start = trimLeft(exp, start, to);
                to = trimRight(exp, start, to);
                brackets--;
            }
            if (to - start <= 0) {
                return null;
            }
        }

        ExpressionPart expressionPart = new ExpressionPart(exp, start, to);
        model.lineNumber = 1 + countNewLines(exp, 0, start);

        if (isTemplate && to - from == 0)
            return new Expression.Holder("", true, model);

        if (functions == null) {
            functions = new HashMap<String, UserFunction>();
        }

        {
            Expression result = prepareTemplate(exp, start, to, model, functions, imports, isTemplate);
            if (result != null)
                return result;
        }

        {
            Expression result = prepareClosure(exp, start, to, model, functions, imports);
            if (result != null)
                return result;
        }

        {
            Expression result = prepareClass(exp, start, to, model, functions, imports);
            if (result != null)
                return result;
        }

        {
            Expression result = prepareBlock(exp, start, to, model, functions, imports);
            if (result != null)
                return result;
        }

        {
            if (expressionPart.equals("null")) {
                return Expression.Holder.NULL;
            }
            Object obj = Expression.parse(exp, start, to);
            if (obj != null) {
                return new Expression.Holder(exp, obj, model);
            }
            if (model.containsKey(exp)) {
                return new Expression.VariableOrFieldOfThis(exp, model);
            }

            if (expressionPart.startsWith("return ") || expressionPart.equals("return")) {
                return new Expression.ReturnExpression(prepare(expressionPart.source, expressionPart.start + 6, expressionPart.end, model, functions, imports, isTemplate), model);
            }
            if (expressionPart.startsWith("throw ")) {
                return new Expression.ThrowExpression(prepare(expressionPart.source, expressionPart.start + 5, expressionPart.end, model, functions, imports, isTemplate), model);
            }
            if (expressionPart.equals("[]")) {
                return new Expression.CollectionExpression(model);
            }
            if (expressionPart.equals("[:]")) {
                return new Expression.MapExpression(model);
            }
        }

        {
            Expression result = prepareVariableDefinition(expressionPart, model, functions, imports);
            if (result != null)
                return result;
        }
        {
            Expression result = prepareMethodDefinition(expressionPart, model, functions, imports);
            if (result != null)
                return result;
        }

        {
            Expression result = prepareAction(exp, start, to, model, functions, imports);
            if (result != null)
                return result;
        }

        {
            if (isMap(exp, start, to)) {
                Map<String, Expression> map = new LinkedHashMap<String, Expression>();
                for (Map.Entry<String, String> entry : parseMap(exp, start, to).entrySet()) {
                    map.put(entry.getKey(), prepare(entry.getValue(), model, functions, imports, isTemplate));
                }
                return new Expression.MapExpression(map, model);
            }
            if (isList(exp, start, to)) {
                List<Expression> arr = prepareArgs(exp, start + 1, to - 1, model, functions, imports);
                return new Expression.CollectionExpression(arr, model);
            }
        }

        Expression thatObject = null;
        String methodName = null;
        Class arrayClass = null;
        {
            Matcher m = NEW.matcher(expressionPart);
            if (m.find()) {
                String className = m.group(1);
                ClassExpression cl = (ClassExpression) model.get("class " + className);
                if (cl != null) {
                    thatObject = cl;
                    methodName = CONSTRUCTOR;
                    from = start = expressionPart.start + m.end();
                } else {
                    Class clazz = findClass(className, imports, model);

                    if (clazz != null) {
                        if (m.group(5) != null) {
                            thatObject = new Expression.Holder(Array.class, model);
                            methodName = "newInstance";
                            arrayClass = clazz;
                            from = start = expressionPart.start + m.end() - 1;
                        } else {
                            thatObject = new Expression.Holder(clazz, model);
                            methodName = CONSTRUCTOR;
                            from = start = expressionPart.start + m.end();
                        }
                    } else {
                        thatObject = new ClassExpressionLookup(className, model);
                        methodName = CONSTRUCTOR;
                        from = start = expressionPart.start + m.end();
                    }
                }
            }
        }

        if (thatObject == null) {
            Matcher m = LIST.matcher(expressionPart);
            if (m.find()) {
                thatObject = new Expression.Holder(m.group(1), model);
                from = start = expressionPart.start + m.end(1);
            }
        }

        if (thatObject == null) {
            PrepareResolveClassResult result = prepareResolveClass(expressionPart, model, functions, imports);
            if (result != null) {
                thatObject = result.thatObject;
                start = from = result.position;
            }
        }

        if (thatObject == null) {
            Matcher m = CAST.matcher(expressionPart);
            if (m.find()) {
                String className = m.group(1);
                Class clazz = findClass(className, imports, model);
                if (clazz == null) {
                    int lastDot = className.lastIndexOf('.');
                    if (lastDot != -1)
                        clazz = findClass(className.substring(0, lastDot) + "$" + className.substring(lastDot + 1), imports, model);
                }

                if (clazz != null) {
                    boolean isArray = m.group(5) != null;
                    return new Expression.CastExpression(clazz, prepare(exp, expressionPart.start + m.end(), to, model, functions, imports, isTemplate), isArray, model);
                }
                if (model.containsKey("class " + className)) {
                    return prepare(exp, expressionPart.start + m.end(), to, model, functions, imports, isTemplate);
                }
                throw new IllegalStateException("Cannot find class to cast (" + className + ") for expression '" + exp + "'");
            }
        }

        if (thatObject == null) {
            Matcher m = FUNCTION.matcher(expressionPart);
            if (m.find()) {
//                System.out.println("find user function: " + m.group(1) + "\t from " + exp);
//                System.out.println("available functions: " + functions);
                ClassExpression currentClass = (ClassExpression) model.get("current class");
                String functionName = m.group(1);
                List<ExpressionPart> args = parseArgs(exp, start + functionName.length() + 1, to - 1);
                thatObject = new ClosureLookup(functionName, functions, args.size(), currentClass, model);
                start = from = start + functionName.length();
            }
        }


        List<ExpressionPart> parts = getParts(exp, start, to);
        String last = null;
//        System.out.println(exp);
        while (!parts.isEmpty()) {
            String temp = parts.toString();
            if (temp.equals(last))
                throw new IllegalStateException("loop at " + exp + "\t\t" + parts);
            last = temp;


            if (thatObject == null && parts.size() == 1 && parts.get(0).from == start && parts.get(0).to == to) {
                ExpressionPart part = parts.remove(0);
                thatObject = new Expression.VariableOrFieldOfThis(part.toString(), model);
                continue;
            }
            if (thatObject == null) {
                ExpressionPart part = parts.remove(0);
                thatObject = prepare(part.source, part.from, part.to, model, functions, imports, isTemplate);
                continue;
            }

            //.concat("ololo")
            if (parts.size() >= 2 && parts.get(0).startsWith(".")
                    && (parts.get(1).wrappedWith("(", ")") || parts.get(1).wrappedWith("{", "}") || parts.get(1).start - parts.get(0).end > 0)) {
                methodName = parts.remove(0).substring(1);
                Expression[] args = null;
                ExpressionPart part = parts.remove(0);
                if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                    List<Expression> arr = prepareArgs(part.source, part.startBracketsTrimmed, part.endBracketsTrimmed, model, functions, imports);
                    args = arr.toArray(new Expression[arr.size()]);
                }
                thatObject = new Function(thatObject, methodName, args, model);

                //?.concat("ololo")
            } else if (parts.size() >= 2 && parts.get(0).startsWith("?.")
                    && (parts.get(1).wrappedWith("(", ")") || parts.get(1).wrappedWith("{", "}") || parts.get(1).start - parts.get(0).end > 0)) {
                methodName = parts.remove(0).substring(2);
                Expression[] args = null;
                ExpressionPart part = parts.remove(0);
                if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                    List<Expression> arr = prepareArgs(part.source, part.startBracketsTrimmed, part.endBracketsTrimmed, model, functions, imports);
                    args = arr.toArray(new Expression[arr.size()]);
                }
                thatObject = new Function(thatObject, methodName, args, true, model);

                //*.concat("ololo")
            } else if (parts.size() >= 1 && parts.get(0).startsWith("*.")) {
                methodName = parts.remove(0).substring(2);
                ClosureExpression closure = new ClosureExpression(model);
                Expression.VariableOrFieldOfThis it = new Expression.VariableOrFieldOfThis("it", model);
                if (parts.size() >= 1 && parts.get(0).wrappedWith("(", ")")) {
                    Expression[] args = null;
                    ExpressionPart part = parts.remove(0);
                    if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                        List<Expression> arr = prepareArgs(part.source, part.startBracketsTrimmed, part.endBracketsTrimmed, model, functions, imports);
                        args = arr.toArray(new Expression[arr.size()]);
                    }
                    closure.add(new Function(it, methodName, args, model));
                } else {
                    closure.add(new Function(it, methodName, model));
                }

                ClosureHolder closureHolder = new ClosureHolder(closure, model);
                thatObject = new Function(thatObject, "collect", new Expression[]{closureHolder}, model);

                //("ololo")
            } else if (parts.get(0).startsWith("(") && parts.get(0).endsWith(")")) {
                Expression[] args = null;
                ExpressionPart part = parts.remove(0);
                if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                    List<Expression> arr = prepareArgs(part.source, part.startBracketsTrimmed, part.endBracketsTrimmed, model, functions, imports);
                    args = arr.toArray(new Expression[arr.size()]);
                }
                if (methodName == null) {
                    if (Function.findMeta(null, thatObject.exp, false) != null) {
                        thatObject = new Function(Expression.Holder.NULL, thatObject.exp, args, model);
                        continue;
                    }
                    methodName = "%execute%";
                }

                if (thatObject instanceof UserFunction) {
                    UserFunction function = (UserFunction) thatObject;
                    function.setArgs(args);
                    function.setUserFunctions(functions);
                } else {
                    thatObject = new Function(thatObject, methodName, args, model);
                }

                //.x
            } else if (parts.get(0).startsWith(".")) {
                String field = parts.remove(0).substring(1);
                thatObject = new Function(thatObject, field, model);

                //?.x
            } else if (parts.get(0).startsWith("?.")) {
                String field = parts.remove(0).substring(2);
                thatObject = new Function(thatObject, field, true, model);

                //[0]
            } else if (parts.get(0).wrappedWith("[", "]")) {
                ExpressionPart argsRaw = parts.remove(0);
                if (arrayClass == null)
                    thatObject = new Operation(thatObject, prepare(argsRaw.source, argsRaw.start + 1, argsRaw.to - 1, model, functions, imports, isTemplate), Operator.GET, model);
                else {
                    List<ExpressionPart> moreArgs = Collections.emptyList();
                    while (!parts.isEmpty() && parts.get(0).startsWith("[") && parts.get(0).endsWith("]")) {
                        ExpressionPart anotherDimentions = parts.remove(0);
                        if (moreArgs.isEmpty())
                            moreArgs = new ArrayList<ExpressionPart>(4);

                        moreArgs.add(anotherDimentions);
                    }
                    Expression[] args = new Expression[2 + moreArgs.size()];
                    args[0] = new Expression.Holder(arrayClass, model);
                    args[1] = prepare(argsRaw.source, argsRaw.start + 1, argsRaw.to - 1, model, functions, imports, isTemplate);
                    if (!moreArgs.isEmpty()) {
                        for (int i = 0; i < moreArgs.size(); i++) {
                            ExpressionPart part = moreArgs.get(i);
                            args[i + 2] = prepare(part.source, part.start + 1, part.to - 1, model, functions, imports, isTemplate);
                        }
                    }
                    thatObject = new Function(thatObject, methodName, args, model);
                    arrayClass = null;
                }

                //execute closure
            } else if (parts.size() == 1) {
                Expression[] args = null;
                ExpressionPart part = parts.remove(0);
                if (part.endBracketsTrimmed - part.startBracketsTrimmed > 0) {
                    List<Expression> arr = prepareArgs(part.source, part.startBracketsTrimmed, part.endBracketsTrimmed, model, functions, imports);
                    args = arr.toArray(new Expression[arr.size()]);
                }
                thatObject = new Function(thatObject, "%execute%", args, model);
            }
        }

        return thatObject;
    }

    private static String removeComments(String exp) {
        int comment = -1;
        int length = exp.length();
        do {
            comment = exp.indexOf("/*", comment + 1);
            if (comment != -1 && !inString(exp, 0, comment)) {
                int commentEnd = exp.indexOf("*/", comment);
                if (commentEnd == -1)
                    throw new IllegalStateException("Cannot find end of comment block for exp '" + exp + "'");

                exp = exp.substring(0, comment) + exp.substring(comment, commentEnd + 2).replaceAll("[^\\s]", " ") + exp.substring(commentEnd + 2);
            }
        } while (comment != -1);

        do {
            comment = exp.indexOf("//", comment + 1);
            if (comment != -1 && !inString(exp, 0, comment)) {
                int r = exp.indexOf("\r", comment);
                int n = exp.indexOf("\n", comment);
                int commentEnd;
                if (r != -1 && n != -1) {
                    if (n != r + 1)
                        throw new IllegalStateException("Cannot find end of comment for exp '" + exp + "'");
                    commentEnd = r;
                } else if (r != -1) {
                    commentEnd = r;
                } else if (n != -1) {
                    commentEnd = n;
                } else
                    commentEnd = exp.length();

                exp = exp.substring(0, comment) + exp.substring(comment, commentEnd).replaceAll("[^\\s]", " ") + exp.substring(commentEnd);
            }
        } while (comment != -1);

        assert exp.length() == length;
        return exp;
    }

    protected static Expression prioritize(List<Operation> operations) {
        if (operations.size() == 1)
            return operations.get(0);

        Expression eh = null;
        Operation operation = operations.get(0);

        if (operation.operator().priority == Operator.EQUAL.priority) {
            operation.rightPart(prioritize(operations.subList(1, operations.size())));
            return operation;
        }

        while (operations.size() > 0) {
            operation = null;
            int n = 0;
            for (int i = 0; i < operations.size(); i++) {
                if (operation == null || operations.get(i).operator().priority > operation.operator().priority) {
                    operation = operations.get(i);
                    n = i;
                }
            }
            if (operation.operator() == Operator.TERNARY) {
                int ternaryIndex = n;
                operation = null;
                n = 0;
                for (int i = 0; i < ternaryIndex; i++) {
                    if (operation == null || operations.get(i).operator().priority > operation.operator().priority) {
                        operation = operations.get(i);
                        n = i;
                    }
                }
                if (operation == null) {
                    operation = operations.get(0);
                }
            }

            if (operation.operator() == Operator.TERNARY) {
                operation.rightPart(operations.remove(n + 1));
            }

            //System.out.println("operation: " + operation);
            if (n > 0) {
                operations.get(n - 1).rightPart(operation);
            }
            if (n < operations.size() - 1) {
                operations.get(n + 1).leftPart(operation);
            }
            eh = operations.remove(n);
        }
        return eh;
    }

    protected static boolean isLineCommented(String s) {
        return s.startsWith("//");
    }

    protected static boolean isLineCommented(ExpressionPart s) {
        return s.startsWith("//");
    }

    protected static boolean isLineCommented(String s, int position) {
        return s.startsWith("//", position);
    }

    protected static int skipCommentLine(String s, int from) {
        while (isLineCommented(s, from)) {
            from = skipUntil(s, from, '\n');
            from = skipWhitespaces(s, from);
        }
        return from;
    }

    protected static int skipWhitespaces(String s, int from) {
        return skipWhitespaces(s, from, s.length());
    }

    protected static int skipWhitespaces(String s, int from, int to) {
        while (from < to && s.charAt(from) <= ' ')
            from++;
        return from;
    }

    protected static int skipNonWhitespaces(String s, int from, int to) {
        while (from < to && s.charAt(from) > ' ')
            from++;
        return from;
    }

    protected static int indexOf(String s, char c, int from, int to) {
        while (from < to) {
            if (s.charAt(from) == c)
                return from;
            from++;
        }
        return -1;
    }

    protected static int indexOf(String s, String substring, int from, int to) {
        while (from < to) {
            if (s.startsWith(substring, from))
                return from;
            from++;
        }
        return -1;
    }

    protected static int indexOfWord(String s, String substring, int from, int to) {
        int length = substring.length();
        int limit = to - length;
        while (from <= limit) {
            if ((from == 0 || s.charAt(from - 1) <= ' ')
                    && (from + length == to || s.charAt(from + length) <= ' ')
                    && s.startsWith(substring, from)
            )
                return from;
            from++;
        }
        return -1;
    }

    protected static boolean substringEquals(String s, int from, int to, String substring) {
        return to - from == substring.length() && s.startsWith(substring, from);
    }

    protected static int countNewLines(String s, int from, int to) {
        int count = 0;
        boolean r = false;
        for (int i = from; i < to; i++) {

            char c = s.charAt(i);
            if (r) {
                if (c == '\r') {
                    count++;
                } else {
                    r = false;
                }
            } else {
                if (c == '\r') {
                    count++;
                    r = true;
                } else if (c == '\n') {
                    count++;
                }
            }
        }
        return count;
    }

    protected static int skipUntil(String s, int position, char until) {
        int limit = s.length();
        while (position < limit && s.charAt(position) != until)
            position++;
        return position;
    }

    private static Map<ClassKey, Class> javaClassesCache = new ConcurrentHashMap<ClassKey, Class>();
    private static Set<ClassKey> notFoundClassesCache = Collections.newSetFromMap(new ConcurrentHashMap<ClassKey, Boolean>());

    private static class ClassKey {
        final String pack;
        final String name;

        private ClassKey(String pack, String name) {
            this.pack = pack;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassKey classKey = (ClassKey) o;

            if (!pack.equals(classKey.pack)) return false;
            return name.equals(classKey.name);
        }

        @Override
        public int hashCode() {
            int result = pack.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    public static Type findType(String name, String generics, List<String> imports, EvaluationContext model) {
        Class aClass = findClass(name, imports, model);
        if (aClass == null) {
            ClassExpression ce = (ClassExpression) model.get("class " + name);
            if (ce != null)
                return ce;
            return null;
        }

        TypeVariable[] typeParameters = aClass.getTypeParameters();
        if (typeParameters.length == 0 || generics == null)
            return aClass;

        List<Type> actualTypes = new ArrayList<>(typeParameters.length);

        Matcher typeMatcher = TYPE.matcher(generics);

        while (typeMatcher.find()) {
            String subTypeName = typeMatcher.group(1).trim();
            Type t = findType(subTypeName, typeMatcher.group("generics"), imports, model);
            if (t == null)
                throw new IllegalStateException("Cannot find class: " + subTypeName);

            actualTypes.add(t);
        }

        Type[] typesArray = actualTypes.toArray(new Type[actualTypes.size()]);
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return typesArray;
            }

            @Override
            public Type getRawType() {
                return aClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    public static Class findClass(String s, List<String> imports, EvaluationContext model) {
        if (s == null)
            return null;

        if (s.contains("<"))
            s = s.substring(0, s.indexOf('<')).trim();

        if (model.getRoot() instanceof ScriptEngine.Binding) {
            ScriptEngine.Binding binding = (ScriptEngine.Binding) model.getRoot();
            if (binding.classCache.containsKey(s))
                return binding.classCache.get(s);

            long time = System.nanoTime();
            Class cl = doFindClass(s, imports);
            time = System.nanoTime() - time;

            binding.findClassDuration += time;
            binding.findClassCount++;

            binding.classCache.put(s, cl);
            return cl;
        }
        return doFindClass(s, imports);
    }

    private static Class doFindClass(String s, List<String> imports) {
        if (s.equals("def"))
            return Object.class;
        if (s.equals("void"))
            return void.class;
        if (s.equals("byte"))
            return byte.class;
        if (s.equals("int"))
            return int.class;
        if (s.equals("short"))
            return short.class;
        if (s.equals("long"))
            return long.class;
        if (s.equals("float"))
            return float.class;
        if (s.equals("double"))
            return double.class;
        if (s.equals("char"))
            return char.class;
        if (s.equals("boolean"))
            return boolean.class;

        if (s.equals("byte[]"))
            return byte[].class;
        if (s.equals("int[]"))
            return int[].class;
        if (s.equals("short[]"))
            return short[].class;
        if (s.equals("long[]"))
            return long[].class;
        if (s.equals("float[]"))
            return float[].class;
        if (s.equals("double[]"))
            return double[].class;
        if (s.equals("char[]"))
            return char[].class;
        if (s.equals("boolean[]"))
            return boolean[].class;

        ClassKey key;
        Class aClass;
        ClassLoader classLoader = EvalTools.class.getClassLoader();

        key = new ClassKey("", s);
        if (!notFoundClassesCache.contains(key)) {
            try {
                return classLoader.loadClass(s);
            } catch (ClassNotFoundException ignored) {
                notFoundClassesCache.add(key);
            }
        }

        key = new ClassKey("java.lang.", s);
        aClass = javaClassesCache.get(key);
        if (aClass != null)
            return aClass;
        if (!notFoundClassesCache.contains(key)) {
            try {
                aClass = classLoader.loadClass("java.lang." + s);
                javaClassesCache.put(key, aClass);
                return aClass;
            } catch (ClassNotFoundException ignored) {
                notFoundClassesCache.add(key);
            }
        }

        key = new ClassKey("java.util.", s);
        aClass = javaClassesCache.get(key);
        if (aClass != null)
            return aClass;
        if (!notFoundClassesCache.contains(key)) {
            try {
                aClass = classLoader.loadClass("java.util." + s);
                javaClassesCache.put(key, aClass);
                return aClass;
            } catch (ClassNotFoundException ignored) {
                notFoundClassesCache.add(key);
            }
        }

        key = new ClassKey("java.io.", s);
        aClass = javaClassesCache.get(key);
        if (aClass != null)
            return aClass;
        if (!notFoundClassesCache.contains(key)) {
            try {
                aClass = classLoader.loadClass("java.io." + s);
                javaClassesCache.put(key, aClass);
                return aClass;
            } catch (ClassNotFoundException ignored) {
                notFoundClassesCache.add(key);
            }
        }

        key = new ClassKey("java.net.", s);
        aClass = javaClassesCache.get(key);
        if (aClass != null)
            return aClass;
        if (!notFoundClassesCache.contains(key)) {
            try {
                aClass = classLoader.loadClass("java.net." + s);
                javaClassesCache.put(key, aClass);
                return aClass;
            } catch (ClassNotFoundException ignored) {
                notFoundClassesCache.add(key);
            }
        }

        if (imports != null) {
            for (String imp : imports) {
                if (imp.length() - 1 - s.length() > 0 && imp.charAt(imp.length() - 1 - s.length()) == '.' && imp.endsWith(s)) {
                    try {
                        return classLoader.loadClass(imp);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
                if (imp.endsWith(".*")) {
                    key = new ClassKey(imp, s);
                    if (!notFoundClassesCache.contains(key)) {
                        try {
                            return classLoader.loadClass(imp.substring(0, imp.length() - 1) + s);
                        } catch (ClassNotFoundException ignored) {
                            notFoundClassesCache.add(key);
                        }
                    }
                }
            }
            if (s.contains("$")) {
                String mainClass = "." + s.substring(0, s.indexOf('$'));
                String subClass = s.substring(s.indexOf('$') + 1);
                for (String imp : imports) {
                    if (imp.endsWith(mainClass))
                        try {
                            return classLoader.loadClass(imp + "$" + subClass);
                        } catch (ClassNotFoundException ignored) {
                        }
                }
            }
            if (s.contains(".")) {
                String mainClass = "." + s.substring(0, s.indexOf('.'));
                String subClass = s.substring(s.indexOf('.') + 1);
                for (String imp : imports) {
                    if (imp.endsWith(mainClass))
                        try {
                            return classLoader.loadClass(imp + "$" + subClass);
                        } catch (ClassNotFoundException ignored) {
                        }
                    if (imp.endsWith(".*"))
                        try {
                            return classLoader.loadClass(imp.substring(0, imp.length() - 2) + mainClass + "$" + subClass);
                        } catch (ClassNotFoundException ignored) {
                        }
                }
            }
        }
        return null;
    }

    private static List<ExpressionPart> parseArgs(String argsRaw) {
        return parseArgs(argsRaw, 0, argsRaw.length());
    }

    private static List<ExpressionPart> parseArgs(String argsRaw, int from, int to) {
        ArrayList<ExpressionPart> l = new ArrayList<ExpressionPart>();
        Matcher m = COMMA.matcher(argsRaw);
        int last = from;
        int position = from;
        while (m.find(position)) {
            if (m.start() >= to)
                break;
            if (countOpenBrackets(argsRaw, last, m.start()) == 0 && !inString(argsRaw, last, m.start())) {
                l.add(new ExpressionPart(argsRaw, last, m.start()));
                last = m.end();
            }
            position = m.end();
        }
        if (last > from || to - from > 0) {
            l.add(new ExpressionPart(argsRaw, last, to));
        }
        return l;
    }

    private static List<Expression> prepareArgs(String argsRaw, int from, int to, EvaluationContext model, Map<String, UserFunction> functions, List<String> imports) {
        List<ExpressionPart> args = parseArgs(argsRaw, from, to);
        ArrayList<Expression> l = new ArrayList<Expression>(args.size());

        int mapStart = -1;
        int mapEnd = -1;
        for (int i = 0; i < args.size(); i++) {
            ExpressionPart arg = args.get(i);
            if (MAP_KEY_VALUE.matcher(arg).matches()) {
                int j = i + 1;
                for (; j < args.size(); j++) {
                    if (!MAP_KEY_VALUE.matcher(args.get(j)).matches())
                        break;
                }
                ExpressionPart lastMapArg = args.get(j - 1);

                Map<String, String> namedArgs = parseMap(argsRaw, arg.start - 1, lastMapArg.end + 1);
                Map<String, Expression> map = new LinkedHashMap<String, Expression>();
                for (Map.Entry<String, String> entry : namedArgs.entrySet()) {
                    map.put(entry.getKey(), prepare(entry.getValue(), model, functions, imports, false));
                }
                l.add(new Expression.MapExpression(map, model));
                i = j - 1;
                continue;
            }
            l.add(prepare(arg, model, functions, imports));
        }
        return l;
    }

    private static Map<String, String> parseMap(String s, int from, int to) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        StringBuilder sb = new StringBuilder();
        String key = null;
        boolean escape = false;
        int brackets = 0;
        to--;
        for (int i = from + 1; i < to; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\'':
                    sb.append(ch);
                    if (!quotesDouble && !escape)
                        quotesSingle = !quotesSingle;
                    escape = false;
                    break;
                case '"':
                    sb.append(ch);
                    if (!quotesSingle && !escape)
                        quotesDouble = !quotesDouble;
                    escape = false;
                    break;
                case '(':
                    if (!quotesSingle && !quotesDouble) {
                        brackets++;
                    }
                    sb.append(ch);
                    break;
                case '[':
                    if (!quotesSingle && !quotesDouble) {
                        brackets++;
                    }
                    sb.append(ch);
                    break;
                case ')':
                    if (!quotesSingle && !quotesDouble) {
                        brackets--;
                    }
                    sb.append(ch);
                    break;
                case ']':
                    if (!quotesSingle && !quotesDouble) {
                        brackets--;
                    }
                    sb.append(ch);
                    break;
                case ':':
                    if (brackets > 0) {
                        sb.append(ch);
                        break;
                    }
                    if (!quotesSingle && !quotesDouble) {
                        key = sb.toString().trim();
                        sb.setLength(0);
                    } else {
                        sb.append(ch);
                    }
                    break;
                case ',':
                    if (brackets > 0) {
                        sb.append(ch);
                        break;
                    }
                    if (!quotesSingle && !quotesDouble) {
                        String value = sb.toString().trim();
                        m.put(key, value);
                        key = null;
                        sb.setLength(0);
                    } else {
                        sb.append(ch);
                    }
                    break;
                case '\\':
                    escape = !escape;
                    if (escape) {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        if (sb.length() > 0) {
            String value = sb.toString().trim();
            m.put(key, value);
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp) {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, null);
        return (T) ex.get(null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model) {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model == null ? null : new HashMap<String, Object>(model));
        return (T) ex.get(model);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model, Map<String, UserFunction> functions) {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model == null ? null : new HashMap<String, Object>(model), functions);
        return (T) ex.get(model);
    }


    public static abstract class ClosureInvoker implements Function.MethodInvoker {
        @Override
        public boolean canInvoke(Object instance) {
            return true;
        }
    }

    static {
        Function.setMethod(Collection.class, "collect", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                List l = new ArrayList();
                ClosureExpression closure = (ClosureExpression) args[0];
                Collection c = (Collection) it;
                for (Object ob : c) {
                    l.add(closure.get(closure.context, ob));
                }
                return l;
            }

            @Override
            public String toString() {
                return "collect";
            }
        });
        Function.setMethod(Collection.class, "find", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(closure.context, ob)) {
                        return ob;
                    }
                }
                return null;
            }

            @Override
            public String toString() {
                return "find";
            }
        });
        Function.setMethod(Collection.class, "findAll", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                List l = new ArrayList();
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(closure.context, ob)) {
                        l.add(ob);
                    }
                }
                return l;
            }

            @Override
            public String toString() {
                return "findAll";
            }
        });
        Function.setMethod(Collection.class, "findIndexOf", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                int i = 0;
                for (Object ob : c) {
                    if ((Boolean) closure.get(closure.context, ob)) {
                        return i;
                    }
                    i++;
                }
                return -1;
            }

            @Override
            public String toString() {
                return "findIndexOf";
            }
        });
        Function.setMethod(Collection.class, "each", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    closure.get(closure.context, ob);
                }
                return null;
            }

            @Override
            public String toString() {
                return "each";
            }
        });
        Function.setMethod(Collection.class, "eachWithIndex", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                int i = 0;
                for (Object ob : c) {
                    closure.get(closure.context, ob, i++);
                }
                return null;
            }

            @Override
            public String toString() {
                return "eachWithIndex";
            }
        });
        Function.setMethod(Collection.class, "every", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if (!(Boolean) closure.get(closure.context, ob))
                        return false;
                }
                return true;
            }

            @Override
            public String toString() {
                return "every";
            }
        });
        Function.setMethod(Collection.class, "any", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                Collection c = (Collection) it;
                ClosureExpression closure = (ClosureExpression) args[0];
                for (Object ob : c) {
                    if ((Boolean) closure.get(closure.context, ob))
                        return true;
                }
                return false;
            }

            @Override
            public String toString() {
                return "any";
            }
        });
        Function.setMethod(Collection.class, "join", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                StringBuilder sb = new StringBuilder();
                Collection c = (Collection) it;
                Object separator = args[0];
                for (Object ob : c) {
                    if (sb.length() != 0) {
                        sb.append(separator);
                    }
                    sb.append(ob);
                }
                return sb.toString();
            }

            @Override
            public String toString() {
                return "join";
            }
        });

        Function.setMethod(Number.class, "multiply", new ClosureInvoker() {

            @Override
            public Object map(Object it, Object[] args) {
                if (args.length != 1)
                    throw new MissingMethodException(it.getClass(), "multiply", args);

                return Operation.multiply(it, args[0]);
            }

            @Override
            public String toString() {
                return "multiply";
            }
        });

        Function.setMethod(Object.class, "with", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                if (args.length != 1 || args[0].getClass() != ClosureExpression.class)
                    throw new MissingMethodException(it.getClass(), "with", args);

                ClosureExpression closure = (ClosureExpression) args[0];
                closure.getAgainst(closure.context, it);
                return it;
            }

            @Override
            public String toString() {
                return "with";
            }
        });

        Function.setMethod(null, "println", new ClosureInvoker() {
            @Override
            public Object map(Object it, Object[] args) {
                System.out.println(args[0]);
                return null;
            }

            @Override
            public String toString() {
                return "println";
            }
        });
    }
}
