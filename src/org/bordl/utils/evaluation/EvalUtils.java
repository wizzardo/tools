/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Moxa
 */
public class EvalUtils {
    static final String CONSTRUCTOR = "%constructor%";
    static EvaluatingStrategy defaultEvaluatingStrategy;

    private static int countOpenBrackets(String s, int from, int to) {
        int n = 0;
        for (int i = from; i < to; i++) {
            if (s.charAt(i) == '(' || s.charAt(i) == '[' || s.charAt(i) == '{') {
                n++;
            } else if (s.charAt(i) == ')' || s.charAt(i) == ']' || s.charAt(i) == '}') {
                n--;
            }
        }
        return n;
    }

    private static boolean inString(String s, int from, int to) {
        boolean inString = false;
        char quote = 0;
        for (int i = from; i < to; i++) {
            if (!inString) {
                if ((s.charAt(i) == '\'' || s.charAt(i) == '\"') && (i == 0 || (i > 1 && s.charAt(i - 1) != '\\'))) {
                    quote = s.charAt(i);
                    inString = true;
                }
            } else if ((s.charAt(i) == quote) && i > 1 && s.charAt(i - 1) != '\\') {
                inString = false;
            }
        }
        return inString;
    }

    public static enum EvaluatingStrategy {
        DEFAULT_JAVA, FLOAT, DOUBLE
    }

    public static void setDefaultEvaluatingStrategy(EvaluatingStrategy defaultEvaluatingStrategy) {
        EvalUtils.defaultEvaluatingStrategy = defaultEvaluatingStrategy;
    }

    public static Expression prepare(String exp) {
        return prepare(exp, null, new HashMap<String, UserFunction>());
    }

    public static Expression prepare(String exp, Map<String, Object> model) {
        return prepare(exp, model, new HashMap<String, UserFunction>());
    }

    private static Pattern brackets = Pattern.compile("[\\(\\)]");

    public static String trimBrackets(String s) {
        int db = s.indexOf("((");
        if (db != -1) {
            String t = s.substring(db + 2);
            Matcher m = brackets.matcher(t);
            int brackets = 2;
            while (m.find()) {
                if (m.group().equals("(")) {
                    brackets++;
                } else if (m.group().equals(")")) {
                    brackets--;
                }
                if (brackets == 0 && m.start() > 0 && t.charAt(m.start() - 1) == ')') {
                    return trimBrackets(s.substring(0, db + 1) + t.substring(0, m.start() - 1) + t.substring(m.start()));
                }
            }
        }

        if (s.startsWith("(") && s.endsWith(")")) {
            Matcher m = brackets.matcher(s);
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
                case ':':
                    if (!quotesSingle && !quotesDouble) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private static boolean isList(String s) {
        if (!s.startsWith("[") || !s.endsWith("]")) {
            return false;
        }
        return true;
    }

    public static Expression prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions) {
//        System.out.println("try to prepare: " + exp);
        if (exp == null) {
            return null;
        }
        exp = exp.trim();
        String trimmed = trimBrackets(exp);
        while (trimmed != exp) {
            exp = trimmed;
            trimmed = trimBrackets(exp);
        }
        if (exp.length() == 0) {
            return null;
        }

        if (model == null) {
            model = new HashMap<String, Object>();
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
                return new Expression.Holder(exp);
            }
            {
                Pattern p = Pattern.compile("def +([a-z]+[a-zA-Z_\\d]*)$");
                Matcher m = p.matcher(exp);
                if (m.find()) {
                    model.put(m.group(1), null);
                    return new Expression.Holder(m.group(1));
                }
            }
        }

        {
            Matcher m = actions.matcher(exp);
            List<String> exps = new ArrayList<String>();
            List<Operation> operations = new ArrayList<Operation>();
            int last = 0;
            Operation operation = null;
            Expression lastExpressionHolder = null;
            boolean ternary = false;
            int ternaryInner = 0;
            while (m.find()) {
                if (ternary) {
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
//                System.out.println(m.group());
                if (countOpenBrackets(exp, last, m.start()) == 0 && !inString(exp, last, m.start())) {
                    exps.add(exp.substring(last, m.start()).trim());
//                    lastExpressionHolder = new ExpressionHolder(exp.substring(last, m.start()));
                    lastExpressionHolder = prepare(exp.substring(last, m.start()), model, functions);
                    if (operation != null) {
                        //complete last operation
                        operation.end(m.start());
//                        operation.rightPart(new ExpressionHolder(exp.substring(last, operation.end())));
                        operation.rightPart(lastExpressionHolder);
                    }
                    operation = new Operation(lastExpressionHolder, Operator.get(m.group()), last, m.end());
                    operations.add(operation);
                    //add operation to list
                    last = m.end();
                    if (ternary) {
//                        lastExpressionHolder = new ExpressionHolder(exp.substring(last, exp.length()));
                        lastExpressionHolder = prepare(exp.substring(last, exp.length()), model, functions);
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
//                    operation.rightPart(new ExpressionHolder(exp.substring(last)));
                    operation.rightPart(prepare(exp.substring(last), model, functions));
                }

                Expression eh = null;
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
        }

        {
            if (exp.equals("[]")) {
                return new Expression.CollectionExpression(new ArrayList());
            }
            if (exp.equals("[:]")) {
                return new Expression.MapExpression(new LinkedHashMap());
            }
            if (isMap(exp)) {
                Map<String, Expression> map = new LinkedHashMap<String, Expression>();
                for (Map.Entry<String, String> entry : parseMap(exp).entrySet()) {
                    map.put(entry.getKey(), prepare(entry.getValue(), model, functions));
                }
                return new Expression.MapExpression(map);
            }
            if (isList(exp)) {
                ArrayList l = new ArrayList();
                exp = exp.substring(1, exp.length() - 1);
                List<String> arr = parseArgs(exp);
                for (int i = 0; i < arr.size(); i++) {
                    l.add(prepare(arr.get(i), model, functions));
                }
                return new Expression.CollectionExpression(l);
            }
        }

        Expression thatObject = null;
        String methodName = null;
        {
            Pattern p = Pattern.compile("new ([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group().substring(4));
                if (clazz != null) {
                    thatObject = new Expression.Holder(clazz);
                    exp = exp.substring(m.end());
                    methodName = CONSTRUCTOR;
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("([a-z]+[a-zA-Z\\d]*)\\[");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                thatObject = new Expression.Holder(m.group(1));
                exp = exp.substring(m.group(1).length());
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group());
                if (clazz != null) {
                    thatObject = new Expression.Holder(clazz);
                    exp = exp.substring(m.end());
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("^([a-z_]+\\w*)\\(.+");
            Matcher m = p.matcher(exp);
            if (m.find()) {
//                System.out.println("find user function: " + m.group(1) + "\t from " + exp);
//                System.out.println("available functions: " + functions);
                UserFunction function = functions.get(m.group(1)).clone();
                thatObject = function;
                exp = exp.substring(function.getName().length());
            }
        }


        Pattern p = Pattern.compile("[\\.\\(\\)\\[\\]]");
        Matcher m = p.matcher(exp);
        int last = 0;
        int brackets = 0;
        while (m.find()) {
//                System.out.println("last: " + last);
            if (m.group().equals("(") || m.group().equals("[")) {
                brackets++;
            } else if (m.group().equals(")") || m.group().equals("]")) {
                brackets--;
            }
//                System.out.println(brackets + ":\t" + exp.substring(last, m.start() + 1));
            if (brackets == 0 || (thatObject != null && methodName == null && brackets == 1)) {
//                    System.out.println("brackets==0");
                if (last == m.start() && !m.group().equals("[")) {
                    last = m.end();
                    if (thatObject != null && methodName != null) {
                        Function function = new Function(thatObject, methodName, null);
                        thatObject = function;
                        methodName = null;
                    }
                    continue;
                }
                if (thatObject == null) {
//                    System.out.println("thatObject: " + exp.substring(last, m.start()));
//                    System.out.println("thatObject2: " + exp.substring(0, m.start()));
                    if (last == 1 && m.group().endsWith("]")) { // init map
//                        thatObject = new Expression(prepare(Expression.clean(exp.substring(last - 1, m.end())), model, functions));
                        thatObject = prepare(exp.substring(last - 1, m.end()), model, functions);
                    } else {                                     // index or ordinary brackets
//                        thatObject = new Expression(prepare(Expression.clean(exp.substring(last, m.start())), model, functions));
                        thatObject = prepare(exp.substring(last, m.start()), model, functions);
                    }
                } else if (methodName == null && !(thatObject instanceof UserFunction)) {
                    if (m.group().equals(".")) { //chain of maps
//                        thatObject = new Expression(new Function(thatObject, exp.substring(last, m.start())));
                        thatObject = new Function(thatObject, exp.substring(last, m.start()));
                        methodName = null;
                    } else {
                        methodName = exp.substring(last, m.start());
//                    System.out.println("methodName: " + methodName);
                    }
                } else {
                    if (countOpenBrackets(exp, last, m.start()) != 0) {
                        continue;
                    }
//                    System.out.println("prepare args: " + exp.substring(last, m.start()));
                    String argsRaw = exp.substring(last, m.start());
                    if (m.group().equals(".") && argsRaw.matches("\\d+")) {
                        continue;
                    }
                    if (methodName != null && methodName.length() == 0 && m.group().equals("]")) {
//                        thatObject = new Expression(new Operation(thatObject, new Expression(prepare(argsRaw, model, functions)), Operator.GET));
                        thatObject = new Operation(thatObject, prepare(argsRaw, model, functions), Operator.GET);
                        methodName = null;
                    } else {
                        Expression[] args = null;
                        if (argsRaw.length() > 0) {
                            List<String> arr = parseArgs(argsRaw);
                            args = new Expression[arr.size()];
                            for (int i = 0; i < arr.size(); i++) {
                                args[i] = prepare(arr.get(i), model, functions);
                            }
                        }
                        if (thatObject instanceof UserFunction) {
//                        System.out.println("set args: "+Arrays.toString(args));
                            UserFunction function = (UserFunction) thatObject;
                            function.setArgs(args);
                            function.setUserFunctions(functions);
                        } else {
                            Function function = new Function(thatObject, methodName, args);
//                        System.out.println("function: " + function);
                            thatObject = function;
                            methodName = null;
                        }
                    }
                }
//                    System.out.println("ololololo " + brackets);
                last = m.end();
            }
            if (last == m.start()) {
                last = m.end();
            }
        }
        String field = exp.substring(last).trim();
        if (field.length() > 0 && !field.equals("null") && !field.equals(")")) {
//            if (thatObject == null)
//                thatObject = new Expression.Holder(field);
//            else
            thatObject = new Function(thatObject, field);
        }
        if (methodName != null) {
            thatObject = new Function(thatObject, methodName, null);
        }
        return thatObject;
    }


    private static Class findClass(String s) {
        return findClass(s, null);
    }

    private static Class findClass(String s, String... imports) {
        try {
            return ClassLoader.getSystemClassLoader().loadClass(s);
        } catch (ClassNotFoundException e) {
            //ignore
        }
        try {
            return ClassLoader.getSystemClassLoader().loadClass("java.lang." + s);
        } catch (ClassNotFoundException e) {
            //ignore
        }
        if (imports != null) {
            for (String imp : imports) {
                if (imp.endsWith("." + s))
                    try {
                        return ClassLoader.getSystemClassLoader().loadClass(imp);
                    } catch (ClassNotFoundException e) {
                        //ignore
                    }
            }
        }
        return null;
    }

    private static List<String> parseArgs(String argsRaw) {
        ArrayList<String> l = new ArrayList<String>();
        Pattern p = Pattern.compile(",");
        Matcher m = p.matcher(argsRaw);
        int last = 0;
        while (m.find()) {
            if (countOpenBrackets(argsRaw, last, m.start()) == 0) {
                l.add(argsRaw.substring(last, m.start()));
                last = m.end();
            }
        }
        if (last > 0) {
            l.add(argsRaw.substring(last));
        } else if (last == 0 && argsRaw.length() > 0) {
            l.add(argsRaw);
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
                    if (!escape) {
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
    public static <T> T evaluate(String exp, Map<String, Object> model) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model);
        return (T) ex.get(model);
    }

    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String exp, Map<String, Object> model, Map<String, UserFunction> functions) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        Expression ex = prepare(exp, model, functions);
        return (T) ex.get(model);
    }


    private static final Pattern actions = Pattern.compile("\\+\\+|--|\\.\\.|\\*=?|/=?|\\+=?|-=?|:|<<|<=?|>=?|==?|%|!=?|\\?|&&?|\\|\\|?");

}
