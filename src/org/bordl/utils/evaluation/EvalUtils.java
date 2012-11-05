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

    private static int countOpenBrackets(String s, int from, int to) {
        int n = 0;
        for (int i = from; i < to; i++) {
            if (s.charAt(i) == '(') {
                n++;
            } else if (s.charAt(i) == ')') {
                n--;
            }
        }
        return n;
    }

    public static ExpressionHolder prepare(String exp, Map<String, Object> model) throws Exception {
        return prepare(exp, model, new HashMap<String, UserFunction>());
    }

    public static ExpressionHolder prepare(String exp, Map<String, Object> model, Map<String, UserFunction> functions) throws Exception {
//        System.out.println("try to prepare: " + exp);
        if (exp == null) {
            return null;
        }
        exp = exp.trim();
        if (exp.length() == 0) {
            return null;
        }

        {
            Object obj = ExpressionHolder.parse(exp);
            if (obj != null) {
                return new ExpressionHolder(exp);
            }
            if (model != null && model.containsKey(exp)) {
                return new ExpressionHolder(exp);
            }
        }

        if (model == null) {
            model = new HashMap<String, Object>();
        }

        {
            Matcher m = logicalActions.matcher(exp);
            if (m.find()) {
                m.reset();
            } else {
                m = actions.matcher(exp);
            }
            List<String> exps = new ArrayList<String>();
            List<Operation> operations = new ArrayList<Operation>();
            int last = 0;
            Operation operation = null;
            ExpressionHolder lastExpressionHolder = null;
            boolean ternary = false;
            while (m.find()) {
                if (ternary) {
                    if (!m.group().equals(":")) {
                        continue;
                    }
                }
                if (countOpenBrackets(exp, last, m.start()) == 0) {
                    exps.add(exp.substring(last, m.start()).trim());
//                    lastExpressionHolder = new ExpressionHolder(exp.substring(last, m.start()));
                    lastExpressionHolder = prepare(ExpressionHolder.clean(exp.substring(last, m.start())), model, functions);
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
                        lastExpressionHolder = prepare(ExpressionHolder.clean(exp.substring(last, exp.length())), model, functions);
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
                    operation.rightPart(prepare(ExpressionHolder.clean(exp.substring(last)), model, functions));
                }

                ExpressionHolder eh = null;
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
                        n--;
                        while (n >= 0 && operations.get(n).operator().logical) {
                            n--;
                        }
                        n++;
                        operation = operations.get(n);
                    }

                    if (operation.operator() == Operator.TERNARY) {
                        ExpressionHolder holder = new ExpressionHolder(operations.remove(n + 1));
                        operation.rightPart(holder);
                    }

                    //System.out.println("operation: " + operation);
                    ExpressionHolder holder = new ExpressionHolder(operation);
                    if (n > 0) {
                        operations.get(n - 1).rightPart(holder);
                    }
                    if (n < operations.size() - 1) {
                        operations.get(n + 1).leftPart(holder);
                    }
                    operations.remove(n);
                    eh = holder;
                }
                return eh;
            }
        }

        ExpressionHolder thatObject = null;
        String methodName = null;
        {
            Pattern p = Pattern.compile("new ([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group().substring(4));
                if (clazz != null) {
                    thatObject = new ExpressionHolder(clazz);
                    exp = exp.substring(m.end());
                    methodName = CONSTRUCTOR;
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("([a-z]+\\.)*(\\b[A-Z][a-zA-Z\\d]+)");
            Matcher m = p.matcher(exp);
            if (m.find()) {
                Class clazz = findClass(m.group());
                if (clazz != null) {
                    thatObject = new ExpressionHolder(clazz);
                    exp = exp.substring(m.end());
                }
            }
        }

        if (thatObject == null) {
            Pattern p = Pattern.compile("^([a-z_]+\\d*[a-zA-Z_]*)\\(.+");
            Matcher m = p.matcher(exp);
            if (m.find()) {
//                System.out.println("find user function: "+m.group(1)+"\t from "+exp);
//                System.out.println("available functions: "+functions);
                thatObject=new ExpressionHolder(functions.get(m.group(1)).clone());
                exp=exp.substring(thatObject.getUserFunction().getName().length());
            }
        }


        Pattern p = Pattern.compile("[\\.\\(\\)]");
        Matcher m = p.matcher(exp);
        int last = 0;
        int brackets = 0;
        while (m.find()) {
//                System.out.println("last: " + last);
            if (m.group().equals("(")) {
                brackets++;
            } else if (m.group().equals(")")) {
                brackets--;
            }
//                System.out.println(brackets + ":\t" + exp.substring(last, m.start() + 1));
            if (brackets == 0 || (thatObject != null && methodName == null && brackets == 1)) {
//                    System.out.println("brackets==0");
                if (last == m.start()) {
                    last = m.end();
                    if (thatObject != null && methodName != null) {
                        Function function = new Function(thatObject, methodName, null);
                        thatObject = new ExpressionHolder(function);
                        methodName = null;
                    }
                    continue;
                }
                if (thatObject == null) {
//                    System.out.println("thatObject: " + exp.substring(last, m.start() + 1));
                    thatObject = new ExpressionHolder(prepare(ExpressionHolder.clean(exp.substring(last, m.start())), model, functions));
                } else if (methodName == null && !thatObject.isUserFunction()) {
                    methodName = exp.substring(last, m.start());
//                    System.out.println("methodName: " + methodName);
                } else {
//                    System.out.println("prepare args: " + exp.substring(last, m.start()));
                    String argsRaw = ExpressionHolder.clean(exp.substring(last, m.start()));
                    ExpressionHolder[] args = null;
                    if (argsRaw.length() > 0) {
                        String[] arr = parseArgs(argsRaw);
                        args = new ExpressionHolder[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            args[i] = prepare(ExpressionHolder.clean(arr[i]), model, functions);
                        }
                    }
                    if (thatObject.isUserFunction()) {
//                        System.out.println("set args: "+Arrays.toString(args));
                        thatObject.getUserFunction().setArgs(args);
                        thatObject.getUserFunction().setUserFunctions(functions);
                    } else {
                        Function function = new Function(thatObject, methodName, args);
//                        System.out.println("function: " + function);
                        thatObject = new ExpressionHolder(function);
                        methodName = null;
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
        if (field.length() > 0 && !field.equals("null")) {
            Function function = new Function(thatObject, field);
            thatObject = new ExpressionHolder(function);
        }
        if (methodName != null) {
            Function function = new Function(thatObject, methodName, null);
            thatObject = new ExpressionHolder(function);
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

    private static String[] parseArgs(String argsRaw) {
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
        return l.toArray(new String[l.size()]);
    }

    public static Object evaluate(String exp, Map<String, Object> model) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        return prepare(exp, model).get(model);
    }

    public static Object evaluate(String exp, Map<String, Object> model, Map<String, UserFunction> functions) throws Exception {
//        System.out.println("evaluate: " + exp + "\t" + model);
        return prepare(exp, model, functions).get(model);
    }


    private static final Pattern actions = Pattern.compile("[\\+\\-/*\\%=\\?!:><]{1,2}");
    private static final Pattern logicalActions = Pattern.compile("[&\\|]{1,2}");

}
