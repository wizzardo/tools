/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public class EvalTools {
    static final String CONSTRUCTOR = "%constructor%";
    static EvaluatingStrategy defaultEvaluatingStrategy;
    private static AtomicInteger variableCounter = new AtomicInteger();
    private static final Pattern CLEAN_CLASS = Pattern.compile("(([a-z]+\\.)*(\\b[A-Z]?[a-zA-Z\\d_]+)(\\.[A-Z]?[a-zA-Z\\d_]+)*)(\\[)?]?");
    private static final Pattern NEW = Pattern.compile("^new +" + CLEAN_CLASS.pattern() + "(\\<(\\s*" + CLEAN_CLASS.pattern() + "\\s*,*\\s*)*\\>)*");
    private static final Pattern CLASS = Pattern.compile("^" + CLEAN_CLASS.pattern());
    private static final Pattern CAST = Pattern.compile("^\\(" + CLEAN_CLASS.pattern() + "(\\<([\\s<]*" + CLEAN_CLASS.pattern() + "[\\s>,]*)+\\>)?" + "\\)");
    private static final Pattern FUNCTION = Pattern.compile("^([a-z_]+\\w*)\\(.+");
    private static final Pattern COMMA = Pattern.compile(",");
    private static final Pattern MAP_KEY_VALUE = Pattern.compile("[a-zA-Z\\d]+ *: *.+");
    private static final Pattern IF_FOR_WHILE = Pattern.compile("(if|for|while) *\\(");
    private static final Pattern LIST = Pattern.compile("^([a-z]+[a-zA-Z\\d]*)\\[");
    private static final Pattern VARIABLE = Pattern.compile("\\$([\\.a-z]+[\\.a-zA-Z]*)");
    private static final Pattern ACTIONS = Pattern.compile("\\+\\+|--|\\.\\.|\\?:|\\?\\.|\\*=|\\*(?!\\.)|/=?|\\+=?|-=?|:|<<|<=?|>=?|==?|%|!=?|\\?|&&?|\\|\\|?");
    private static final Pattern DEF = Pattern.compile("(static|private|protected|public)*(def|[a-zA-Z_\\d\\.]+(?:<[\\s,a-zA-Z_\\d\\.<>\\[\\]]+>)*(?:\\[\\])*) +([a-zA-Z_]+[a-zA-Z_\\d]*) *($|=|\\()");
    private static final Pattern RETURN = Pattern.compile("^return\\b");
    private static final Pattern BRACKETS = Pattern.compile("[\\(\\)]");
    private static final Pattern CLASS_DEF = Pattern.compile("(static|private|protected|public)*\\bclass +([A-Za-z0-9_]+) *\\{");
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

    protected static LinkedList<String> getParts(String s) {
        LinkedList<String> l = new LinkedList<String>();
        boolean inString = false;
        boolean spaceSeparator = false;
        char quote = 0;
        char[] chars = s.toCharArray();
        int from = 0;
        int brackets = 0;
        int squareBrackets = 0;
        int curlyBraces = 0;
        loop:
        for (int i = 0; i < chars.length; i++) {
            if (!inString) {
                switch (chars[i]) {
                    case '"':
                    case '\'': {
                        quote = chars[i];
                        inString = true;
                        break;
                    }
                    case ' ': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            spaceSeparator = true;
                            l.add(trim(chars, from, i));
                            from = i + 1;

                            break loop;
                        }

                        break;
                    }
                    case '(': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(trim(chars, from, i));
                            from = i;
                        }
                        brackets++;
                        break;
                    }
                    case '{': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(trim(chars, from, i));
                            from = i;
                        }
                        curlyBraces++;
                        break;
                    }
                    case '[': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            l.add(trim(chars, from, i));
                            from = i;
                        }
                        squareBrackets++;
                        break;
                    }
                    case ')': {
                        brackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(trim(chars, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case '}': {
                        curlyBraces--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(trim(chars, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case ']': {
                        squareBrackets--;
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0) {
                            l.add(trim(chars, from, i + 1));
                            from = i + 1;
                        }
                        break;
                    }
                    case '.': {
                        if (brackets == 0 && curlyBraces == 0 && squareBrackets == 0 && i != from) {
                            if (i > 0 && (chars[i - 1] == '*' || chars[i - 1] == '?'))
                                if (i - from == 1)
                                    continue;
                                else
                                    i--;

                            l.add(trim(chars, from, i));
                            from = i;
                        }
                        break;
                    }
                }
            } else if ((chars[i] == quote) && i > 1 && chars[i - 1] != '\\') {
                inString = false;
            }
        }
        if (from != chars.length) {
            if (spaceSeparator)
                l.add("(" + trim(chars, from, chars.length) + ")");
            else
                l.add(trim(chars, from, chars.length));
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

    public static String trimBrackets(String s) {
        int db = s.indexOf("((");
        if (db != -1) {
            String t = s.substring(db + 2);
            Matcher m = BRACKETS.matcher(t);
            int brackets = 2;
            boolean innerBrackets = false;
            while (m.find()) {
                if (m.group().equals("(")) {
                    if (brackets < 2)
                        innerBrackets = true;
                    brackets++;
                } else if (m.group().equals(")")) {
                    brackets--;
                }
                if (!innerBrackets && brackets == 0 && m.start() > 0 && t.charAt(m.start() - 1) == ')') {
                    return trimBrackets(s.substring(0, db + 1) + t.substring(0, m.start() - 1) + t.substring(m.start()));
                }
            }
        }

        if (s.startsWith("(") && s.endsWith(")")) {
            Matcher m = BRACKETS.matcher(s);
            int brackets = 0;
            while (m.find()) {
                if (m.group().equals("(")) {
                    brackets++;
                } else if (m.group().equals(")")) {
                    brackets--;
                }
                if (brackets == 0 && m.end() != s.length()) {
                    return s;
                }
            }
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static boolean isMap(String s) {
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return false;
        }
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        int brackets = 0;
        for (int i = 1; i < s.length(); i++) {
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

    private static boolean isList(String s) {
        return s.startsWith("[") && s.endsWith("]");
    }

    private static boolean isClosure(String exp) {
        if (exp.startsWith("{") && exp.endsWith("}"))
            return true;

        return false;
    }

    static boolean isJavaLambda(String exp) {
        int i = exp.indexOf("->");

        int eq = exp.indexOf("=");
        if (eq > 0 && eq < i)
            return false;

        if (exp.startsWith("return "))
            return false;

        if (i > 0 && !inString(exp, 0, i) && countOpenBrackets(exp, 0, i) == 0) {
            return true;
        }

        return false;
    }

    static List<String> getBlocks(String exp) {
        return getBlocks(exp, false);
    }

    static class Statement {
        Type type = Type.BLOCK;
        String statement;
        String body;
        String optional;
        Statement bodyStatement;
        Statement optionalStatement;

        public Statement(String statement) {
            this.statement = statement;
        }

        public Statement() {
        }

        public Expression prepare(Map<String, Object> model, Map<String, UserFunction> functions, List<String> imports) {
            switch (type) {
                case IF: {
                    List<String> args = getBlocks(statement, true);
                    if (args.size() > 1)
                        throw new IllegalStateException("more then one statement in condition: " + statement);

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(0), model, functions, imports));
                    Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                    Expression elseExpression = optionalStatement != null ? optionalStatement.prepare(model, functions, imports) : EvalTools.prepare(optional, model, functions, imports);
                    if (elseExpression == null)
                        return new IfExpression(condition, then);
                    else
                        return new IfExpression(condition, then, elseExpression);
                }
                case WHILE: {
                    List<String> args = getBlocks(statement, true);
                    if (args.size() > 1)
                        throw new IllegalStateException("more then one statement in condition: " + statement);

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(0), model, functions, imports));
                    Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                    return new WhileExpression(condition, then);
                }
                case FOR: {
                    List<String> args = getBlocks(statement, true, true);
                    if (args.size() != 3)
                        throw new IllegalStateException("wrong number of statements: " + args);

                    Expression def = EvalTools.prepare(args.get(0), model, functions, imports);
                    if (def == null)
                        def = Expression.Holder.NULL;
                    Expression iterator = EvalTools.prepare(args.get(2), model, functions, imports);
                    if (iterator == null) {
                        iterator = Expression.Holder.NULL;
                    }

                    AsBooleanExpression condition = new AsBooleanExpression(EvalTools.prepare(args.get(1), model, functions, imports));
                    Expression then = bodyStatement != null ? bodyStatement.prepare(model, functions, imports) : EvalTools.prepare(body, model, functions, imports);
                    return new ForExpression(def, condition, iterator, then);
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
        List<Statement> statements = new ArrayList<Statement>();
        Matcher m = IF_FOR_WHILE.matcher(s);
        int start = 0;
        int to = s.length();
        int searchOffset = 0;
        while (m.find(searchOffset)) {
            if (countOpenBrackets(s, start, m.start()) != 0) {
                searchOffset = m.end();
                continue;
            }

            if (m.start() != start) {
                statements.add(new Statement(s.substring(start, m.start())));
            }

            Statement statement = new Statement();
            start = getBlock(s, m.start(), to, statement);
            searchOffset = start;
            statements.add(statement.bodyStatement);
        }
        if (start != to) {
            String sub = s.substring(start, to).trim();
            if (sub.length() > 0)
                statements.add(new Statement(sub));
        }
        return statements;
    }

    static int getBlock(String s, int from, int to, Statement statement) {
        Matcher m = IF_FOR_WHILE.matcher(s);
        int start = from;
        if (m.find(start)) {
            String before = s.substring(start, m.start()).trim();
            if (before.length() > 0) {
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
            inner.statement = s.substring(m.end(), close);

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

                inner.body = s.substring(start, close);
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
                        if (statement.bodyStatement == null && statement.body == null)
                            statement.body = s.substring(from, i).trim();
                        else
                            statement.optional = s.substring(from, i).trim();

                        return i + (c == ';' ? 1 : 0);
                    }
                    if (c == '"' || c == '\'') {
                        inString = true;
                    }
                }
            }
            last = c;
        }
        if (statement.bodyStatement == null && statement.body == null)
            statement.body = s.substring(from, to).trim();
        else
            statement.optional = s.substring(from, to).trim();
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

            statement.optional = s.substring(start, close);
            return close + 1;
        } else {
            return getBlock(s, start, to, statement);
        }
    }

    static List<String> getBlocks(String exp, boolean ignoreNewLine) {
        return getBlocks(exp, ignoreNewLine, false);
    }

    static List<String> getBlocks(String exp, boolean ignoreNewLine, boolean withEmptyStatements) {
        List<String> list = new ArrayList<String>();

        StringBuilder sb = new StringBuilder(128);
        char last = 0, stringChar = 0;
        boolean inString = false;
        int brackets = 0;
        int length = exp.length();
        for (int i = 0; i < length; i++) {
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
                        cleanLine(sb);
                        if (withEmptyStatements || sb.length() > 0)
                            list.add(sb.toString());
                        sb.setLength(0);
                        continue;
                    }
                    if (c == '"' || c == '\'') {
                        stringChar = c;
                        inString = true;
                    }
                }
            }
            last = c;
            sb.append(c);
        }
        cleanLine(sb);
        if (withEmptyStatements || sb.length() > 0)
            list.add(sb.toString());

        return list;
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
        return prepare(exp, null, null, null, true);
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
        script = script.trim();
        int s, n;
        int position = 0;
        if (script.startsWith("package")) {
            n = script.indexOf("\n");
            s = script.indexOf(";");
            int to = Math.min(n == -1 ? script.length() : n, s == -1 ? script.length() : s);
            imports.add(script.substring(8, to).trim() + ".*");

            position = to + 1;
            while (position < script.length() && script.charAt(position) <= ' ')
                position++;
        }
        while (script.startsWith("import", position)) {
            n = script.indexOf("\n", position);
            s = script.indexOf(";", position);
            int to = Math.min(n == -1 ? script.length() : n, s == -1 ? script.length() : s);
            imports.add(script.substring(position + 7, to).trim());

            position = to + 1;
            while (position < script.length() && script.charAt(position) <= ' ')
                position++;
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

    public static Expression prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions, List<String> imports, boolean isTemplate) {
//        System.out.println("try to prepare: " + exp);
        if (exp == null) {
            return null;
        }
        //remove comments
        int comment = -1;
        do {
            comment = exp.indexOf("/*", comment + 1);
            if (comment != -1 && !inString(exp, 0, comment)) {
                int commentEnd = exp.indexOf("*/", comment);
                if (commentEnd == -1)
                    throw new IllegalStateException("Cannot find end of comment block for exp '" + exp + "'");

                exp = exp.substring(0, comment) + exp.substring(commentEnd + 2);
            }
        } while (comment != -1);

        if (!isTemplate) {
            exp = exp.trim();
            String trimmed = trimBrackets(exp);
            while (trimmed != exp) {
                exp = trimmed;
                trimmed = trimBrackets(exp);
            }
            if (exp.length() == 0) {
                return null;
            }
        }

        if (isTemplate && exp.length() == 0)
            return new Expression.Holder("", true);

        if (model == null) {
            model = new HashMap<String, Object>();
        }

        if (functions == null) {
            functions = new HashMap<String, UserFunction>();
        }

        {
            if (!isTemplate && exp.startsWith("\"\"\"") && exp.endsWith("\"\"\"") && inString(exp, 0, exp.length() - 1)) {
                return prepare(exp.substring(3, exp.length() - 3), model, functions, imports, true);
            }
            if (!isTemplate && exp.startsWith("\"") && exp.endsWith("\"") && isTemplate(exp, 0, exp.length() - 1)) {
                String quote = exp.charAt(0) + "";
                exp = exp.substring(1, exp.length() - 1).replace("\\" + quote, quote);
                return prepare(exp, model, functions, imports, true);
            }

            if (isTemplate) {
                TemplateBuilder tb = new TemplateBuilder();
                Matcher m = VARIABLE.matcher(exp);
                int start = 0;
                int end = 0;
                while ((start = exp.indexOf("$", start)) != -1) {
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
                if (end != exp.length())
                    tb.append(exp.substring(end, exp.length()));

                return tb;
            }
        }

        boolean isLambda = false;
        if (isClosure(exp) || (isLambda = isJavaLambda(exp))) {
            ClosureExpression closure = new ClosureExpression();
            if (isLambda) {
                exp = closure.parseArguments(exp);
                if (exp.startsWith("{"))
                    exp = exp.substring(1, exp.length() - 1).trim();
            } else {
                exp = exp.substring(1, exp.length() - 1).trim();
                exp = closure.parseArguments(exp);
            }

            List<Statement> statements = getStatements(exp);
            for (Statement s : statements) {
                switch (s.type) {
                    case IF:
                    case FOR:
                    case WHILE:
                        closure.add(s.prepare(model, functions, imports));
                        break;

                    case BLOCK: {
                        List<String> lines = getBlocks(s.statement);
                        if (lines.isEmpty())
                            continue;

                        for (String line : lines) {
                            if (isLineCommented(line))
                                continue;
                            closure.add(prepare(line, model, functions, imports, isTemplate));
                        }
                        break;
                    }

                    default:
                        throw new IllegalStateException("not implemented yet");
                }

            }
            return new ClosureHolder(closure);
        }

        {
            Matcher m = CLASS_DEF.matcher(exp);
            if (m.find() && findCloseBracket(exp, m.end()) == exp.length() - 1) {
                String className = m.group(2);

                exp = exp.substring(m.end(), exp.length() - 1).trim();
                List<String> lines = getBlocks(exp);

                List<Expression> definitions = new ArrayList<Expression>(lines.size());
                ClassExpression classExpression = new ClassExpression(className, definitions);
                model.put("class " + className, classExpression);

                for (String s : lines) {
                    if (isLineCommented(s))
                        continue;

                    boolean isStatic = false;
                    Matcher staticMatcher = STATIC_BLOCK.matcher(s);
                    if (staticMatcher.find()) {
                        s = s.substring("static ".length()).trim();
                        isStatic = true;
                    }

                    Expression prepare = prepare(s, model, functions, imports, isTemplate);
                    if (prepare instanceof ClosureHolder) {
                        ClosureExpression closure = ((ClosureHolder) prepare).closure;
                        closure.setContext(classExpression.context);
                        prepare = closure;
                    }
                    definitions.add(prepare);
                }
                classExpression.init();
                return classExpression;
            }
        }

        {
            List<Statement> statements = getStatements(exp);
            Expression.BlockExpression block = new Expression.BlockExpression();
            for (Statement s : statements) {
                switch (s.type) {
                    case IF:
                    case FOR:
                    case WHILE:
                        block.add(s.prepare(model, functions, imports));
                        break;

                    case BLOCK: {
                        List<String> lines = getBlocks(s.statement);
                        if (lines.isEmpty())
                            continue;

                        if (lines.size() > 1) {
                            for (String line : lines) {
                                if (isLineCommented(line))
                                    continue;
                                block.add(prepare(line, model, functions, imports, isTemplate));
                            }
                        } else if (statements.size() > 1 || !lines.get(0).equals(s.statement)) {
                            block.add(prepare(lines.get(0), model, functions, imports, isTemplate));
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
        }

        {
            if (exp.equals("null")) {
                return Expression.Holder.NULL;
            }
            Object obj = Expression.parse(exp);
            if (obj != null) {
                return new Expression.Holder(exp);
            }
            if (model.containsKey(exp)) {
                return new Expression.VariableOrFieldOfThis(exp);
            }

            {
                Matcher m = RETURN.matcher(exp);
                if (m.find()) {
                    return new Expression.ReturnExpression(prepare(exp.substring(6), model, functions, imports, isTemplate));
                }
            }

            {
                Matcher m = DEF.matcher(exp);
                if (m.find() && !m.group(2).equals("new")) {
                    if (m.group(4).equals("=")) {
                        exp = m.replaceFirst("def $3 =");
                    } else if (m.group(4).equals("(")) {
                        int argsEnd = findCloseBracket(exp, m.end());
                        if (argsEnd == m.end()) {
                            exp = "def " + m.group(3) + " = " + exp.substring(argsEnd + 1);
                        } else {
                            String block = exp.substring(argsEnd + 1).trim();
                            if (!block.startsWith("{"))
                                throw new IllegalStateException("Cannot parse: " + exp);

                            String args = exp.substring(m.end(), argsEnd);
                            exp = "def " + m.group(3) + " = { " + args + " -> " + block.substring(1);
                        }
                        return prepare(exp, model, functions, imports, isTemplate);
                    } else {
                        model.put(m.group(3), null);
                        return new Expression.Holder(m.group(3));
                    }
                }
            }
        }

        {
            Matcher m = ACTIONS.matcher(exp);
            List<String> exps = new ArrayList<String>();
            List<Operation> operations = new ArrayList<Operation>();
            int last = 0;
            Operation operation = null;
            Expression lastExpressionHolder = null;
            boolean ternary = false;
            int ternaryInner = 0;
            while (m.find()) {
                if (ternary) {
                    if (inString(exp, 0, m.start()))
                        continue;

                    if (m.group().equals("?")) {
                        ternaryInner++;
                        continue;
                    }
                    if (!m.group().equals(":")) {
                        continue;
                    }
                    if (ternaryInner > 0) {
                        ternaryInner--;
                        continue;
                    }
                }
                if ("?.".equals(m.group()))
                    continue;

//                System.out.println(m.group());
                if (!inString(exp, 0, m.start()) && countOpenBrackets(exp, 0, m.start()) == 0) {
                    String subexpression = exp.substring(last, m.start()).trim();
                    if (subexpression.startsWith("new ") && (m.group().equals("<") || m.group().equals(">"))) {
                        continue;
                    }
                    exps.add(subexpression);
                    lastExpressionHolder = prepare(subexpression, model, functions, imports, isTemplate);
                    if (operation != null) {
                        //complete last operation
                        operation.end(m.start());
                        operation.rightPart(lastExpressionHolder);
                    }
                    operation = new Operation(lastExpressionHolder, Operator.get(m.group()), last, m.end());
                    operations.add(operation);
                    //add operation to list
                    last = m.end();
                    if (ternary) {
                        lastExpressionHolder = prepare(exp.substring(last), model, functions, imports, isTemplate);
                        operation.rightPart(lastExpressionHolder);
                        break;
                    }
                    if (m.group().equals("?")) {
                        ternary = true;
                    }
                }
            }
            if (operation != null) {
                if (last != exp.length()) {
                    exps.add(exp.substring(last).trim());
                    operation.end(exp.length());
                    operation.rightPart(prepare(exp.substring(last), model, functions, imports, isTemplate));
                }

                return prioritize(operations);
            }
        }

        {
            if (exp.equals("[]")) {
                return new Expression.CollectionExpression();
            }
            if (exp.equals("[:]")) {
                return new Expression.MapExpression();
            }
            if (isMap(exp)) {
                Map<String, Expression> map = new LinkedHashMap<String, Expression>();
                for (Map.Entry<String, String> entry : parseMap(exp).entrySet()) {
                    map.put(entry.getKey(), prepare(entry.getValue(), model, functions, imports, isTemplate));
                }
                return new Expression.MapExpression(map);
            }
            if (isList(exp)) {
                List<Expression> l = new ArrayList<Expression>();
                exp = exp.substring(1, exp.length() - 1);
                List<String> arr = parseArgs(exp);
                for (String anArr : arr) {
                    l.add(prepare(anArr, model, functions, imports, isTemplate));
                }
                return new Expression.CollectionExpression(l);
            }
        }

        Expression thatObject = null;
        String methodName = null;
        Class arrayClass = null;
        {
            Matcher m = NEW.matcher(exp);
            if (m.find()) {
                String className = m.group(1);
                ClassExpression cl = (ClassExpression) model.get("class " + className);
                if (cl != null) {
                    thatObject = cl;
                    methodName = CONSTRUCTOR;
                    exp = exp.substring(m.end());
                } else {
                    Class clazz = findClass(className, imports, model);

                    if (clazz != null) {
                        if (m.group(5) != null) {
                            thatObject = new Expression.Holder(Array.class);
                            methodName = "newInstance";
                            arrayClass = clazz;
                            exp = exp.substring(m.end() - 1);
                        } else {
                            thatObject = new Expression.Holder(clazz);
                            methodName = CONSTRUCTOR;
                            exp = exp.substring(m.end());
                        }
                    } else
                        Unchecked.rethrow(new ClassNotFoundException("Can not find class '" + className + "'"));
                }
            }
        }

        if (thatObject == null) {
            Matcher m = LIST.matcher(exp);
            if (m.find()) {
                thatObject = new Expression.Holder(m.group(1));
                exp = exp.substring(m.group(1).length());
            }
        }

        if (thatObject == null) {
            Matcher m = CLASS.matcher(exp);
            if (m.find()) {
                String className = m.group(1);
                StringBuilder sb = new StringBuilder(className);

                int i;
                do {
                    className = sb.toString();
                    ClassExpression cl = (ClassExpression) model.get("class " + className);
                    if (cl != null) {
                        thatObject = cl;
                        exp = exp.substring(sb.length());
                    } else {
                        Class clazz = findClass(className, imports, model);
                        if (clazz == null) {
                            int lastDot = sb.lastIndexOf(".");
                            if (lastDot != -1) {
                                sb.setCharAt(lastDot, '$');
                                clazz = findClass(sb.toString(), imports, model);
                                sb.setCharAt(lastDot, '.');
                            }
                        }

                        if (clazz != null) {
                            thatObject = new Expression.Holder(clazz);
                            exp = exp.substring(sb.length());
                            break;
                        }
                    }

                    i = sb.lastIndexOf(".");
                    if (i > 0)
                        sb.setLength(i);
                } while (i > 0);
            }
        }

        if (thatObject == null) {
            Matcher m = CAST.matcher(exp);
            while (m.find()) {
                if (m.start(6) >= 0) {
                    //remove generics
                    exp = exp.substring(0, m.start(6)) + exp.substring(m.end(6));
                    m = CAST.matcher(exp);
                    continue;
                }

                String className = m.group(1);
                Class clazz = findClass(className, imports, model);
                if (clazz == null) {
                    int lastDot = className.lastIndexOf('.');
                    if (lastDot != -1)
                        clazz = findClass(className.substring(0, lastDot) + "$" + className.substring(lastDot + 1), imports, model);
                }

                if (clazz != null) {
                    exp = exp.substring(m.end());
                    boolean isArray = m.group(5) != null;
                    return new Expression.CastExpression(clazz, prepare(exp, model, functions, imports, isTemplate), isArray);
                }
                if (model.containsKey("class " + className)) {
                    exp = exp.substring(m.end());
                    return prepare(exp, model, functions, imports, isTemplate);
                }
                throw new IllegalStateException("Cannot find class to cast (" + className + ") for expression '" + exp + "'");
            }
        }

        if (thatObject == null) {
            Matcher m = FUNCTION.matcher(exp);
            if (m.find()) {
//                System.out.println("find user function: " + m.group(1) + "\t from " + exp);
//                System.out.println("available functions: " + functions);
                String functionName = m.group(1);
                thatObject = new ClosureLookup(functionName, functions);
                exp = exp.substring(functionName.length());
            }
        }


        List<String> parts = getParts(exp);
        String last = null;
//        System.out.println(exp);
        while (!parts.isEmpty()) {
            String temp = parts.toString();
            if (temp.equals(last))
                throw new IllegalStateException("loop at " + exp + "\t\t" + parts);
            last = temp;


            if (thatObject == null && parts.size() == 1 && parts.get(0).equals(exp)) {
                thatObject = new Expression.VariableOrFieldOfThis(parts.remove(0));
                continue;
            }
            if (thatObject == null) {
                thatObject = prepare(parts.remove(0), model, functions, imports, isTemplate);
                continue;
            }

            //.concat("ololo")
            if (parts.size() >= 2 && parts.get(0).startsWith(".")
                    && ((parts.get(1).startsWith("(") && parts.get(1).endsWith(")")) || (parts.get(1).startsWith("{") && parts.get(1).endsWith("}")))) {
                methodName = parts.remove(0).substring(1);
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions, imports, isTemplate);
                    }
                }
                thatObject = new Function(thatObject, methodName, args);

                //?.concat("ololo")
            } else if (parts.size() >= 2 && parts.get(0).startsWith("?.")
                    && ((parts.get(1).startsWith("(") && parts.get(1).endsWith(")")) || (parts.get(1).startsWith("{") && parts.get(1).endsWith("}")))) {
                methodName = parts.remove(0).substring(2);
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions, imports, isTemplate);
                    }
                }
                thatObject = new Function(thatObject, methodName, args, true);

                //*.concat("ololo")
            } else if (parts.size() >= 1 && parts.get(0).startsWith("*.")) {
                String var = getTempVariableName();
                Expression[] args = new Expression[1];
                methodName = parts.remove(0).substring(1);
                String argsRaw;
                if (parts.size() >= 1 && parts.get(0).startsWith("(") && parts.get(0).endsWith(")"))
                    argsRaw = parts.remove(0);
                else
                    argsRaw = "";

                args[0] = prepare("{" + var + " -> " + var + methodName + argsRaw + "}", model, functions, imports, isTemplate);
                thatObject = new Function(thatObject, "collect", args);

                //("ololo")
            } else if (parts.get(0).startsWith("(") && parts.get(0).endsWith(")")) {
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions, imports, isTemplate);
                    }
                }
                if (methodName == null) {
                    if (Function.findMeta(null, thatObject.exp, false) != null) {
                        thatObject = new Function(Expression.Holder.NULL, thatObject.exp, args);
                        continue;
                    }
                    methodName = "execute";
                }

                if (thatObject instanceof UserFunction) {
                    UserFunction function = (UserFunction) thatObject;
                    function.setArgs(args);
                    function.setUserFunctions(functions);
                } else {
                    thatObject = new Function(thatObject, methodName, args);
                }

                //.x
            } else if (parts.get(0).startsWith(".")) {
                String field = parts.remove(0).substring(1);
                thatObject = new Function(thatObject, field);

                //?.x
            } else if (parts.get(0).startsWith("?.")) {
                String field = parts.remove(0).substring(2);
                thatObject = new Function(thatObject, field, true);

                //[0]
            } else if (parts.get(0).startsWith("[") && parts.get(0).endsWith("]")) {
                String argsRaw = parts.remove(0);
                argsRaw = argsRaw.substring(1, argsRaw.length() - 1);
                if (arrayClass == null)
                    thatObject = new Operation(thatObject, prepare(argsRaw, model, functions, imports, isTemplate), Operator.GET);
                else {
                    List<String> moreArgs = Collections.emptyList();
                    while (!parts.isEmpty() && parts.get(0).startsWith("[") && parts.get(0).endsWith("]")) {
                        String anotherDimentions = parts.remove(0);
                        anotherDimentions = anotherDimentions.substring(1, anotherDimentions.length() - 1);
                        if (moreArgs.isEmpty())
                            moreArgs = new ArrayList<String>(4);

                        moreArgs.add(anotherDimentions);
                    }
                    Expression[] args = new Expression[2 + moreArgs.size()];
                    args[0] = new Expression.Holder(arrayClass);
                    args[1] = prepare(argsRaw, model, functions, imports, isTemplate);
                    if (!moreArgs.isEmpty()) {
                        for (int i = 0; i < moreArgs.size(); i++) {
                            args[i + 2] = prepare(moreArgs.get(i), model, functions, imports, isTemplate);
                        }
                    }
                    thatObject = new Function(thatObject, methodName, args);
                    arrayClass = null;
                }

                //execute closure
            } else if (parts.size() == 1) {
                Expression[] args = null;
                String argsRaw = trimBrackets(parts.remove(0));
                if (argsRaw.length() > 0) {
                    List<String> arr = parseArgs(argsRaw);
                    args = new Expression[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        args[i] = prepare(arr.get(i), model, functions, imports, isTemplate);
                    }
                }
                thatObject = new Function(thatObject, "execute", args);
            }
        }

        return thatObject;
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

    private static Class findClass(String s, List<String> imports, Map model) {
        if (model instanceof ScriptEngine.Binding) {
            ScriptEngine.Binding binding = (ScriptEngine.Binding) model;
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

    private static List<String> parseArgs(String argsRaw) {
        ArrayList<String> l = new ArrayList<String>();
        Matcher m = COMMA.matcher(argsRaw);
        int last = 0;
        while (m.find()) {
            if (countOpenBrackets(argsRaw, last, m.start()) == 0 && !inString(argsRaw, last, m.start())) {
                l.add(argsRaw.substring(last, m.start()).trim());
                last = m.end();
            }
        }
        if (last > 0) {
            l.add(argsRaw.substring(last).trim());
        } else if (last == 0 && argsRaw.length() > 0) {
            l.add(argsRaw.trim());
        }

        int mapStart = -1;
        int mapEnd = -1;
        while (mapStart == -1) {
            for (int i = 0; i < l.size(); i++) {
                String arg = l.get(i);
                if (MAP_KEY_VALUE.matcher(arg).matches()) {
                    if (mapStart == -1)
                        mapStart = mapEnd = i;
                    else
                        mapEnd = i;
                } else if (mapStart != -1)
                    break;
            }

            if (mapStart != -1) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = mapStart; i <= mapEnd; i++) {
                    if (sb.length() > 1)
                        sb.append(", ");
                    sb.append(l.remove(mapStart));
                }
                sb.append("]");
                l.add(mapStart, sb.toString());
            } else
                break;

            //reset
            mapStart = -1;
            mapEnd = -1;
        }
        return l;
    }

    private static Map<String, String> parseMap(String s) {
        Map<String, String> m = new LinkedHashMap<String, String>();
        s = s.substring(1, s.length() - 1);
        boolean quotesSingle = false;
        boolean quotesDouble = false;
        StringBuilder sb = new StringBuilder();
        String key = null;
        boolean escape = false;
        int brackets = 0;
        for (int i = 0; i < s.length(); i++) {
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
        Expression ex = prepare(exp, model);
        return (T) ex.get(model);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model, Map<String, UserFunction> functions) {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model, functions);
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
