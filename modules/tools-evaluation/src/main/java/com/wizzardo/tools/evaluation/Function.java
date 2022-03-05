/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Moxa
 */
public class Function extends Expression {


    public interface MethodInvoker {
        Object map(Object instance, Object[] args);

        boolean canInvoke(Object instance);
    }

    protected static final Expression[] EMPTY_ARGS = new Expression[0];
    protected static Map<Class, Map<String, MethodInvoker>> metaMethods = new HashMap<Class, Map<String, MethodInvoker>>();

    protected static Map<Class, Method[]> methodsCache = new ConcurrentHashMap<Class, Method[]>(256, 1);

    protected Expression thatObject;
    protected MethodInvoker method;
    protected Mapper<Object[], Object> constructor;
    protected Field field;
    protected Getter getter;
    protected Setter setter;
    protected String methodName;
    protected Expression[] args;
    protected List<Mapper<Object[], Object[]>> argsMappers;
    protected String fieldName;
    protected boolean hardcodeChecked = false;
    protected boolean safeNavigation = false;

    public Function(Expression thatObject, MethodInvoker method, Expression[] args, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
    }

    public Function(Expression thatObject, MethodInvoker method, Expression[] args, boolean safeNavigation, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
        this.safeNavigation = safeNavigation;
    }

    protected Function(Expression thatObject, MethodInvoker method, Expression[] args, boolean safeNavigation, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
        this.safeNavigation = safeNavigation;
    }

    public Function(Expression thatObject, String methodName, Expression[] args, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
    }

    protected Function(Expression thatObject, String methodName, Expression[] args, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
    }

    public Function(Expression thatObject, String methodName, Expression[] args, boolean safeNavigation, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
        this.safeNavigation = safeNavigation;
    }

    protected Function(Expression thatObject, String methodName, Expression[] args, boolean safeNavigation, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
        this.safeNavigation = safeNavigation;
    }

    public Function(Mapper<Object[], Object> constructor, Expression[] args, EvaluationContext context) {
        super(context);
        this.args = args;
        this.constructor = constructor;
    }

    protected Function(Mapper<Object[], Object> constructor, Expression[] args, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.args = args;
        this.constructor = constructor;
    }

    public Function(Expression object, MethodInvoker method, EvaluationContext context) {
        super(context);
        this.thatObject = object;
        this.method = method;
    }

    public Function(Expression thatObject, String fieldName, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.fieldName = fieldName;
    }
    protected Function(Expression thatObject, String fieldName, String file, int lineNumber, int linePosition) {
        super(file, lineNumber,linePosition);
        this.thatObject = thatObject;
        this.fieldName = fieldName;
    }

    public Function(Expression thatObject, String fieldName, boolean safeNavigation, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.fieldName = fieldName;
        this.safeNavigation = safeNavigation;
    }

    protected Function(Expression thatObject, String fieldName, boolean safeNavigation, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.thatObject = thatObject;
        this.fieldName = fieldName;
        this.safeNavigation = safeNavigation;
    }

    public Function(Expression thatObject, Field field, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.field = field;
    }

    public Function(Expression thatObject, Field field, boolean safeNavigation, EvaluationContext context) {
        super(context);
        this.thatObject = thatObject;
        this.field = field;
        this.safeNavigation = safeNavigation;
    }

    protected Function(Expression thatObject, Field field, boolean safeNavigation, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.thatObject = thatObject;
        this.field = field;
        this.safeNavigation = safeNavigation;
    }

    @Override
    public void setVariable(Variable v) {
        if (thatObject != null)
            thatObject.setVariable(v);
        if (args != null)
            for (Expression e : args)
                e.setVariable(v);
    }

    @Override
    public Function clone() {
        Expression[] args = null;
        if (this.args != null) {
            args = new Expression[this.args.length];
            for (int i = 0; i < args.length; i++) {
                args[i] = this.args[i].clone();
            }
        }
        if (constructor != null) {
            return new Function(constructor, args, file, lineNumber, linePosition);
        }
        if (field != null) {
            return new Function(thatObject, field, safeNavigation, file, lineNumber, linePosition);
        }
        if (fieldName != null) {
            return new Function(thatObject, fieldName, safeNavigation, file, lineNumber, linePosition);
        }
        if (method != null) {
            return new Function(thatObject.clone(), method, args, safeNavigation, file, lineNumber, linePosition);
        }
        return new Function(thatObject.clone(), methodName, args, safeNavigation, file, lineNumber, linePosition);
    }

    private ThreadLocal<Object[]> tempArray = new ThreadLocal<Object[]>();

    interface Getter {
        Object get(Object instance);
    }

    static class FieldGetter implements Getter {
        final Field field;

        FieldGetter(Field field) {
            this.field = field;
        }

        @Override
        public Object get(Object instance) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }

    static class MethodGetter implements Getter {
        final Method method;

        MethodGetter(Method method) {
            this.method = method;
        }

        @Override
        public Object get(Object instance) {
            try {
                return method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            } catch (InvocationTargetException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }

    static class MapGetter implements Getter {
        final Object fieldName;

        MapGetter(Object fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public Object get(Object instance) {
            return ((Map) instance).get(fieldName);
        }
    }

    interface Setter {
        void set(Object instance, Object value);
    }

    static class FieldSetter implements Setter {
        final Field field;

        FieldSetter(Field field) {
            this.field = field;
        }

        @Override
        public void set(Object instance, Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }

    static class MethodSetter implements Setter {
        final Method method;

        MethodSetter(Method method) {
            this.method = method;
        }

        @Override
        public void set(Object instance, Object value) {
            try {
                method.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw Unchecked.rethrow(e);
            } catch (InvocationTargetException e) {
                throw Unchecked.rethrow(e);
            }
        }
    }

    static class MapSetter implements Setter {
        final Object fieldName;

        MapSetter(Object fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public void set(Object instance, Object value) {
            ((Map) instance).put(fieldName, value);
        }
    }

    protected Object doExecute(Map<String, Object> model) {
        if (hardcoded) {
            return result;
        }
        try {
            Object[] arr = null;
//        System.out.println("evaluate method: "+toString());

            Object instance;
            if (thatObject instanceof ClosureLookup) {
                if (args != null)
                    arr = resolveArgs(model);

                instance = ((ClosureLookup) thatObject).get(model, arr);

                if (!(instance instanceof Expression)) {
                    methodName = ((ClosureLookup) thatObject).functionName;
                    if (instance == null)
                        instance = model.get("this");
                }
            } else
                instance = thatObject.get(model);

            if (instance == null && thatObject instanceof VariableOrFieldOfThis) {
                if (VariableOrFieldOfThis.hasThis(model))
                    instance = ((VariableOrFieldOfThis) thatObject).function.get(model);
            }

            if (safeNavigation && instance == null)
                return null;

            if (args != null) {
                arr = resolveArgs(model);
            }

            if (instance instanceof ClosureExpression) {
                ClosureExpression exp = (ClosureExpression) instance;
                return exp.getAgainst(null, exp.context, arr);
            }

            if (instance instanceof UserFunction) {
                UserFunction function = (UserFunction) instance;
                function.setArgs(args);
                return function.get(model);
            }

            if (fieldName != null || getter != null) {
                return getGetter(instance).get(instance);
            }
            if (constructor != null || EvalTools.CONSTRUCTOR.equals(methodName)) {
                if (constructor == null) {
                    argsMappers = new ArrayList<Mapper<Object[], Object[]>>(args == null ? 0 : args.length);
                    constructor = findConstructor(getClass(instance), arr, instance, argsMappers);
                }
//            System.out.println(constructor);
//            System.out.println(Arrays.toString(constructor.getParameterTypes()));
//            System.out.println(Arrays.toString(arr));

                if (!argsMappers.isEmpty()) {
                    for (Mapper<Object[], Object[]> mapper : argsMappers) {
                        arr = mapper.map(arr);
                    }
                }

                return constructor.map(arr);
            }
            if (method == null || !method.canInvoke(instance)) {
                argsMappers = new ArrayList<Mapper<Object[], Object[]>>(args == null ? 0 : args.length);
                String methodName = this.methodName;
                if ("super".equals(thatObject.exp)) {
                    methodName = "super_" + methodName;
                }
                method = findMethod(getClass(instance), instance, methodName, arr, argsMappers);
                if (method == null && instance != null && instance.getClass() == Class.class)
                    method = findMethod(instance.getClass(), instance, methodName, arr, argsMappers);

            }
            if (method == null) {
                if (instance instanceof TemplateBuilder.GString) {
                    thatObject = new Function(thatObject, "toString", new Expression[0], file, lineNumber, linePosition);
                    return get(model);
                } else if (!methodName.equals("%execute%")) {
                    Expression prevThatObject = thatObject;
                    thatObject = new Function(thatObject, methodName, file, lineNumber, linePosition);
                    String methodNameHolder = methodName;
                    methodName = "%execute%";
                    try {
                        return get(model);
                    } catch (Exception e) {
                        if (e instanceof CallStack.EvaluationException)
                            e = (Exception) e.getCause();
                        if (!e.getClass().equals(NoSuchFieldException.class))
                            throw Unchecked.rethrow(e);
                        else {
                            methodName = methodNameHolder;
                            thatObject = prevThatObject;
                        }
                    }
                }
//            System.out.println("can't find " + methodName + " for class " + thatObject.getClass(model) + "\t" + Arrays.toString(arr));
                Class aClass = getClass(instance);
                throw new NoSuchMethodException((aClass != null ? aClass.getCanonicalName() : "null") + "." + methodName + "(" + (arr == null ? "" : Arrays.toString(arr)) + "), at " + this);
            }

            if (!argsMappers.isEmpty()) {
                for (Mapper<Object[], Object[]> mapper : argsMappers) {
                    arr = mapper.map(arr);
                }
            }
//            Object result = method.invoke(instance, arr);
            Object result = method.map(instance, arr);
//            if (!hardcodeChecked && (hardcoded = thatObject.hardcoded)) {
//                hardcodeChecked = true;
//                if (args != null)
//                    for (Expression arg : args) {
//                        hardcoded = arg.hardcoded;
//                        if (!hardcoded)
//                            break;
//                    }
//            }
//            if (hardcoded)
//                this.result = result;
            return result;
        } catch (Exception e) {
            throw Unchecked.rethrow(e);
        }
    }

    protected Object[] resolveArgs(Map<String, Object> model) {
        Object[] arr;
        if ((arr = tempArray.get()) == null) {
            arr = new Object[args.length];
            tempArray.set(arr);
        }
//            System.out.println("try resolve args:");
        for (int i = 0; i < arr.length; i++) {
//                System.out.println(i+"\t"+args[i]);
            if (args[i] instanceof ClosureExpression)
                arr[i] = args[i];
            else {
                Object o = args[i].get(model);
                if (o instanceof TemplateBuilder.GString)
                    o = o.toString();

                arr[i] = o;
            }
        }
        return arr;
    }

    public Getter getGetter(Object instance) {
        if (getter != null)
            return getter;

        if (fieldName == null)
            return null;

        if (instance == null)
            throw new NullPointerException("Cannot get getter '" + fieldName + "' on null: " + thatObject.toString());

        if (instance instanceof Map)
            return getter = new MapGetter(fieldName);

        if (instance instanceof ClassExpression)
            return getter = new Getter() {
                @Override
                public Object get(Object instance) {
                    return ((ClassExpression) instance).context.get(fieldName);
                }
            };

        if (instance instanceof Class && fieldName.equals("class"))
            return new Getter() {
                @Override
                public Object get(Object instance) {
                    return instance;
                }
            };

        if (instance instanceof ClassExpression.ClassExpressionProxy) {
            return getGetter(((ClassExpression.ClassExpressionProxy) instance).getClassExpression());
        }

        Class clazz = instance instanceof Class ? (Class) instance : instance.getClass();
        Field field = findField(clazz, fieldName);
        if (field != null && Modifier.isPublic(field.getModifiers()))
            return getter = new FieldGetter(field);

        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = findMethod(clazz, methodName, 0);
        if (method != null)
            return getter = new MethodGetter(method);

        if (instance instanceof Class) {
            clazz = instance.getClass();
            method = findMethod(clazz, methodName, 0);
            if (method != null)
                return getter = new MethodGetter(method);
        }

        if (instance.getClass().isArray() && fieldName.equals("length")) {
            return new Getter() {
                @Override
                public Object get(Object instance) {
                    return Array.getLength(instance);
                }
            };
        }

        if (field != null) {
            field.setAccessible(true);
            return getter = new FieldGetter(field);
        }

        throw Unchecked.rethrow(new NoSuchFieldException("Cannot find getter " + thatObject.toString() + "." + fieldName));
    }

    public Setter getSetter(Object instance) {
        if (setter != null)
            return setter;

        if (fieldName == null)
            return null;

        if (instance instanceof Map)
            return setter = new MapSetter(fieldName);

        if (instance instanceof ClassExpression)
            return setter = new Setter() {
                @Override
                public void set(Object instance, Object value) {
                    ((ClassExpression) instance).context.put(fieldName, value);
                }
            };


        Class clazz = instance instanceof Class ? (Class) instance : instance.getClass();
        Field field = findField(clazz, fieldName);
        if (field != null && !Modifier.isPrivate(field.getModifiers()))
            return setter = new FieldSetter(field);

        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = findMethod(clazz, methodName, 1);
        if (method != null)
            return setter = new MethodSetter(method);

//        throw Unchecked.rethrow(new NoSuchFieldException(fieldName));
        return null;
    }

    protected Method findMethod(Class clazz, String name, int paramsCount) {
        while (clazz != null) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name) && paramsCount == m.getParameterTypes().length) {
                    if (Modifier.isPublic(m.getModifiers()))
                        m.setAccessible(true);
                    return m;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    protected static Field findField(Class clazz, String name) {
        while (clazz != null)
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        return null;
    }

    protected static MethodInvoker findMeta(Class clazz, String methodName, boolean recursively) {
        Map<String, MethodInvoker> methods;
        MethodInvoker closure;
        if ((methods = metaMethods.get(clazz)) != null && (closure = methods.get(methodName)) != null) {
            return closure;
        }

        if (clazz != null && (closure = findMetaInterfaces(clazz.getInterfaces(), methodName)) != null) {
            return closure;
        }

        if (recursively && clazz != null)
            return findMeta(clazz.getSuperclass(), methodName, true);
        return null;
    }

    protected static MethodInvoker findMetaInterfaces(Class[] classes, String methodName) {
        MethodInvoker closure = null;
        Map<String, MethodInvoker> methods;
        for (Class i : classes) {
            if (closure == null && ((methods = metaMethods.get(i)) == null || (closure = methods.get(methodName)) == null)) {
                closure = findMetaInterfaces(i.getInterfaces(), methodName);
            }
        }
        return closure;
    }

    private Class getClass(Object ob) {
        if (ob == null)
            return null;

        Class clazz = ob.getClass();
        if (clazz == Class.class) {
            return (Class) ob;
        }
        return clazz;
    }

    static MethodInvoker findMethod(Class clazz, Object instance, String method, Object[] args, List<Mapper<Object[], Object[]>> argsMappers) {
        Class[] argsClasses;
        if (args != null) {
            argsClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    argsClasses[i] = args[i].getClass();
            }
        } else
            argsClasses = new Class[0];

        if (ClassExpression.class.equals(clazz)) {
            ClassExpression cl = (ClassExpression) instance;
            MethodDefinition methodDefinition = cl.findMethod(method, args);
            ClosureExpression closure = null;

            if (methodDefinition != null) {
                closure = (ClosureExpression) methodDefinition.action.get(cl.context);
            } else {
                Object o = cl.context.get(method);
                if (o instanceof ClosureExpression)
                    closure = ((ClosureExpression) o);
            }

            if (closure == null)
                throw new IllegalArgumentException("Cannot find method " + method + " " + Arrays.toString(args) + " in " + cl);

            ClosureExpression finalClosure = closure;
            String finalMethod = method;
            return new EvalTools.ClosureInvoker() {
                @Override
                public Object map(Object instance, Object[] args) {
                    return finalClosure.getAgainst(null, finalClosure.context, args);
                }

                @Override
                public String toString() {
                    return finalMethod;
                }
            };
        }

        if (method.equals("%execute%"))
            method = "execute";

//        try {
//            return wrapMethod(clazz.getMethod(method, argsClasses));
//        } catch (NoSuchMethodException e) {
//            //ignore
//        }

        while (clazz != null) {
            Method[] methods = getMethods(clazz);

            MethodInvoker meta = findMeta(clazz, method, false);
            if (meta != null) {
                return meta;
            }

            Method m = findMethod(methods, method, argsClasses, argsMappers);

            if (m == null)
                m = findDefaultMethod(clazz, method, argsClasses, argsMappers);

            if (m != null)
                return wrapMethod(m);

            clazz = clazz.getSuperclass();
        }

        return findMeta(null, method, false);
    }

    private static Method findMethod(Method[] methods, String method, Class[] argsClasses, final List<Mapper<Object[], Object[]>> argsMappers) {
        Method m = findExactMatch(methods, method, argsClasses);
        if (m == null)
            m = findBoxedMatch(methods, method, argsClasses);
        if (m == null)
            m = findNumberMatch(methods, method, argsClasses, argsMappers);
        return m;
    }

    private static Method findDefaultMethod(Class cl, String method, Class[] argsClasses, final List<Mapper<Object[], Object[]>> argsMappers) {
        for (Class anInterface : cl.getInterfaces()) {
            Method m = findMethod(getMethods(anInterface), method, argsClasses, argsMappers);
            if (m != null)
                return m;

            m = findDefaultMethod(anInterface, method, argsClasses, argsMappers);
            if (m != null)
                return m;
        }
        return null;
    }

    private static MethodInvoker wrapMethod(final Method m) {
        m.setAccessible(true);

        return new MethodInvoker() {
            @Override
            public Object map(Object instance, Object[] args) {
                try {
                    return m.invoke(instance, args);
                } catch (IllegalAccessException e) {
                    throw Unchecked.rethrow(e);
                } catch (InvocationTargetException e) {
                    throw Unchecked.rethrow(e);
                } catch (ClassCastException e) {
                    throw Unchecked.rethrow(e);
                }
            }

            @Override
            public boolean canInvoke(Object instance) {
                return m.getDeclaringClass().isAssignableFrom(instance.getClass());
            }

            @Override
            public String toString() {
                return m.getName();
            }
        };
    }

    private static Method findExactMatch(Method[] methods, String method, Class[] argsClasses) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : methods) {
            if (m.getName().equals(method) && ((m.getParameterTypes().length == 0 && argsClasses == null) || m.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    arg = argsClasses[i];
                    param = m.getParameterTypes()[i];
                    if (arg == null && !param.isPrimitive())
                        continue;
                    if (arg != param && (arg == null || !param.isAssignableFrom(arg)))
                        continue outer;
                }
                return m;
            }
        }
        return null;
    }

    private static Method findBoxedMatch(Method[] methods, String method, Class[] argsClasses) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : methods) {
            if (m.getName().equals(method) && ((m.getParameterTypes().length == 0 && argsClasses == null) || m.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    arg = argsClasses[i];
                    param = m.getParameterTypes()[i];
                    if (param.isPrimitive() && boxing.get(param) == arg)
                        continue;
                    continue outer;
                }
                return m;
            }
        }
        return null;
    }

    private static Method findNumberMatch(Method[] methods, String method, Class[] argsClasses, final List<Mapper<Object[], Object[]>> argsMappers) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : methods) {
            final Class<?>[] parameterTypes = m.getParameterTypes();
            if (m.getName().equals(method) && ((parameterTypes.length == 0 && argsClasses == null) || parameterTypes.length == argsClasses.length ||
                    (m.isVarArgs() && parameterTypes.length <= argsClasses.length + 1))) {
                if (!argsMappers.isEmpty())
                    argsMappers.clear();

                for (int i = 0; i < argsClasses.length; i++) {
                    arg = argsClasses[i];

                    if (i >= parameterTypes.length - 1 && m.isVarArgs()) {
                        param = parameterTypes[parameterTypes.length - 1].getComponentType();
                    } else {
                        param = parameterTypes[i];
                    }

                    if (arg == null && !param.isPrimitive())
                        continue;
                    if (arg == param || arg != null && param.isAssignableFrom(arg))
                        continue;
                    if (param == char.class && arg == Character.class)
                        continue;
                    if (param == boolean.class && arg == Boolean.class)
                        continue;
                    if (arg == ClosureExpression.class && isSAMInterface(param)) {
                        final int index = i;
                        final Class<?> samInterface = param;
                        argsMappers.add(wrapClosureArgAsProxy(index, samInterface));
                        continue;
                    }
                    int a = indexOfClass(arg, Boxed);
                    if (a < 0)
                        continue outer;
                    int p = indexOfClass(param, primitives);
                    if (p < 0)
                        continue outer;
                    if (p < a)
                        continue outer;
                }

                if (m.isVarArgs()) {
                    argsMappers.add(new Mapper<Object[], Object[]>() {
                        @Override
                        public Object[] map(Object[] objects) {
                            int varArgSize = (objects == null ? 0 : objects.length) - (parameterTypes.length - 1);
                            int argsSize = parameterTypes.length;

                            Object[] args = new Object[argsSize];
                            Class<?> arrayType = parameterTypes[parameterTypes.length - 1].getComponentType();
                            Object vararg = Array.newInstance(arrayType, varArgSize);


                            if (argsSize > 1) {
                                System.arraycopy(objects, 0, args, 0, argsSize - 1);
                            }
                            if (varArgSize > 0) {
                                if (arrayType.isPrimitive()) {
                                    for (int i = 0; i < varArgSize; i++) {
                                        int fromIndex = argsSize - 1 + i;
                                        Array.set(vararg, i, objects[fromIndex]);
                                    }
                                } else
                                    System.arraycopy(objects, argsSize - 1, vararg, 0, varArgSize);
                            }

                            args[argsSize - 1] = vararg;

                            return args;
                        }
                    });
//                    for (int i = parameterTypes.length-1; i < ar; i++) {
//
//                    }
                }
                return m;
            }
        }

        if (!argsMappers.isEmpty())
            argsMappers.clear();

        return null;
    }

    protected static boolean isSAMInterface(Class<?> param) {
        if (!param.isInterface())
            return false;

        Method[] methods = param.getMethods();
        int count = 0;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ((method.getModifiers() & Modifier.ABSTRACT) == 0)
                continue;
            if (method.getName().equals("equals") && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Object.class))
                continue;
            if (method.getName().equals("hashCode") && method.getParameterTypes().length == 0)
                continue;
            if (method.getName().equals("toString") && method.getParameterTypes().length == 0)
                continue;
            if (method.getName().equals("clone") && method.getParameterTypes().length == 0)
                continue;
            if (method.getName().equals("finalize") && method.getParameterTypes().length == 0)
                continue;

            count++;

            if (count > 1)
                return false;
        }

        return true;
    }

    private static Mapper<Object[], Object[]> wrapClosureArgAsProxy(final int index, final Class<?> samInterface) {
        return new Mapper<Object[], Object[]>() {
            @Override
            public Object[] map(Object[] objects) {
                objects[index] = wrapClosureAsProxy((ClosureExpression) objects[index], samInterface);
                return objects;
            }
        };
    }

    protected static Object wrapClosureAsProxy(final ClosureExpression closure, final Class<?> samInterface) {
//        final Variable[] closureArgs = new Variable[closure.getParametersCount()];
//        for (int j = 0; j < closureArgs.length; j++) {
//            closure.setVariable(closureArgs[j] = new Variable(closure.getParameterName(j), null));
//        }
        return Proxy.newProxyInstance(
                samInterface.getClassLoader(),
                new Class[]{samInterface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
//                        for (int j = 0; j < closureArgs.length && args != null && j < args.length; j++) {
//                            closureArgs[j].set(args[j]);
//                        }
                        return closure.getAgainst(closure.context, closure.context.get("this"), args);
                    }
                });
    }

    private <T> Mapper<Object[], T> findConstructor(Class<T> clazz, Object[] args, Object instance, List<Mapper<Object[], Object[]>> argsMappers) {
        if (clazz.equals(ClassExpression.class)) {
            final ClassExpression cl = (ClassExpression) instance;
            return new Mapper<Object[], T>() {
                @Override
                public T map(Object[] objects) {
                    return (T) cl.newInstance(objects);
                }

                @Override
                public String toString() {
                    return "new " + cl.toString();
                }
            };
        }

        return wrapConstructor(findConstructor(clazz, args, argsMappers));
    }

    protected <T> Constructor<T> findConstructor(Class<T> clazz, Object[] args, List<Mapper<Object[], Object[]>> argsMappers) {
        Class<?>[] argsClasses = null;
        if (args != null) {
            argsClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    argsClasses[i] = args[i].getClass();
            }
        }
        if (clazz.equals(ClassExpression.class)) {
            return null;
        }

        try {
            return clazz.getConstructor(argsClasses);
        } catch (NoSuchMethodException ignored) {
        }
        outer:
        for (Constructor c : clazz.getConstructors()) {
            Class<?>[] parameterTypes = c.getParameterTypes();
            if (((parameterTypes.length == 0 && argsClasses == null) || parameterTypes.length == argsClasses.length)) {
//                System.out.println("check args");
                if (!argsMappers.isEmpty())
                    argsMappers.clear();

                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> param = parameterTypes[i];
                    Class<?> arg = argsClasses[i];
                    if (arg == ClosureExpression.class && isSAMInterface(param)) {
                        argsMappers.add(wrapClosureArgAsProxy(i, param));
                        continue;
                    }
                    if (!(param.isAssignableFrom(arg)
                            || (boxing.containsKey(param) && boxing.get(param).equals(arg))
                            || (boxing.containsKey(param) && boxing.containsValue(arg)))
                    ) {
//                        System.out.println(m.getParameterTypes()[i] + "\t" + (argsClasses[i]));
                        continue outer;
                    }
                }
                return c;
            }
        }

        if (!argsMappers.isEmpty())
            argsMappers.clear();

        throw new NullPointerException("Cannot find constructor for " + clazz + " with args: " + Arrays.toString(argsClasses));
    }

    private <T> Mapper<Object[], T> wrapConstructor(final Constructor<T> c) {
        return new Mapper<Object[], T>() {
            @Override
            public T map(Object[] args) {
                try {
                    return c.newInstance(args);
                } catch (InstantiationException e) {
                    throw Unchecked.rethrow(e);
                } catch (IllegalAccessException e) {
                    throw Unchecked.rethrow(e);
                } catch (InvocationTargetException e) {
                    throw Unchecked.rethrow(e);
                }
            }

            @Override
            public String toString() {
                return c.toString();
            }
        };
    }

    protected static Method[] getMethods(Class cl) {
        Method[] methods = methodsCache.get(cl);
        if (methods == null) {
            methods = cl.getDeclaredMethods();
            methodsCache.put(cl, methods);
        }

        return methods;
    }

    protected static final Map<Class, Class> boxing = new HashMap<Class, Class>() {
        {
            put(int.class, Integer.class);
            put(double.class, Double.class);
            put(float.class, Float.class);
            put(long.class, Long.class);
            put(boolean.class, Boolean.class);
            put(byte.class, Byte.class);
            put(char.class, Character.class);
            put(short.class, Short.class);
        }
    };

    protected static int indexOfClass(Class clazz, Class[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (clazz == classes[i])
                return i;
        }
        return -1;
    }

    protected static final Class[] primitives = new Class[]{byte.class, short.class, char.class, int.class, long.class, float.class, double.class};
    protected static final Class[] Boxed = new Class[]{Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class};


    public Expression getThatObject() {
        return thatObject;
    }

    public Expression[] getArgs() {
        return args;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(thatObject);
        sb.append(".");
        if (getter != null || fieldName != null)
            sb.append(fieldName);
        else {
            sb.append(method == null ? methodName : method.toString());
            sb.append("(");
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (i > 0)
                        sb.append(", ");
                    sb.append(args[i]);
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public Field getField() {
        return field;
    }

    public static void setMethod(Class clazz, String methodName, MethodInvoker c) {
        Map<String, MethodInvoker> methods = metaMethods.get(clazz);
        if (methods == null) {
            methods = new HashMap<String, MethodInvoker>();
            metaMethods.put(clazz, methods);
        }
        methods.put(methodName, c);
    }
}
