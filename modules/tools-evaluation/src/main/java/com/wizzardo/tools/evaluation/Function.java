/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.collections.CollectionTools;
import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Moxa
 */
class Function extends Expression {

    private Expression thatObject;
    private Method method;
    private Constructor constructor;
    private Field field;
    private Getter getter;
    private Setter setter;
    private String methodName;
    private Expression[] args;
    private String fieldName;
    private boolean hardcodeChecked = false;
    private boolean metaChecked = false;
    private boolean safeNavigation = false;
    private CollectionTools.Closure3<Object, Object, Map, Expression[]> metaMethod;

    private static Map<Class, Map<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>>> metaMethods = new HashMap<Class, Map<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>>>();

    public Function(Expression thatObject, Method method, Expression[] args) {
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
    }

    public Function(Expression thatObject, Method method, Expression[] args, boolean safeNavigation) {
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
        this.safeNavigation = safeNavigation;
    }

    public Function(Expression thatObject, String methodName, Expression[] args) {
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
    }

    public Function(Expression thatObject, String methodName, Expression[] args, boolean safeNavigation) {
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
        this.safeNavigation = safeNavigation;
    }

    public Function(Constructor constructor, Expression[] args) {
        this.args = args;
        this.constructor = constructor;
    }

    public Function(Expression object, Method method) {
        this.thatObject = object;
        this.method = method;
    }

    public Function(Expression thatObject, String fieldName) {
        this.thatObject = thatObject;
        this.fieldName = fieldName;
    }

    public Function(Expression thatObject, String fieldName, boolean safeNavigation) {
        this.thatObject = thatObject;
        this.fieldName = fieldName;
        this.safeNavigation = safeNavigation;
    }

    public Function(Expression thatObject, Field field) {
        this.thatObject = thatObject;
        this.field = field;
    }

    public Function(Expression thatObject, Field field, boolean safeNavigation) {
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
            return new Function(constructor, args);
        }
        if (field != null) {
            return new Function(thatObject, field, safeNavigation);
        }
        if (fieldName != null) {
            return new Function(thatObject, fieldName, safeNavigation);
        }
        if (method != null) {
            return new Function(thatObject.clone(), method, args, safeNavigation);
        }
        return new Function(thatObject.clone(), methodName, args, safeNavigation);
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

    public Object get(Map<String, Object> model) {
        if (hardcoded) {
            return result;
        }
        try {
            Object[] arr = null;
//        System.out.println("evaluate method: "+toString());

            Object instance = thatObject.get(model);

            if (safeNavigation && instance == null)
                return null;

            if (!metaChecked && instance != null) {
                checkMeta(instance instanceof Class ? (Class) instance : instance.getClass());
                metaChecked = true;
            }
            if (metaMethod != null) {
                return metaMethod.execute(instance, model, args);
            }

            if (args != null) {
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
            }

            if (instance instanceof ClosureExpression) {
                ClosureExpression exp = (ClosureExpression) instance;
                return exp.get(model, arr);
            }

            if (instance instanceof UserFunction) {
                UserFunction function = (UserFunction) instance;
                function.setArgs(args);
                return function.get(model);
            }

            if (fieldName != null || getter != null) {
                return getGetter(instance).get(instance);
            }
            if (EvalTools.CONSTRUCTOR.equals(methodName)) {
                constructor = findConstructor(getClass(instance), arr);
//            System.out.println(constructor);
//            System.out.println(Arrays.toString(constructor.getParameterTypes()));
//            System.out.println(Arrays.toString(arr));
                return constructor.newInstance(arr);
            } else if (method == null) {
                method = findMethod(getClass(instance), methodName, arr);
                if (method == null && instance.getClass() == Class.class)
                    method = findMethod(instance.getClass(), methodName, arr);
                if (method != null && Modifier.isPublic(method.getModifiers()))
                    method.setAccessible(true);
            }
            if (method == null) {
                try {
                    if (instance instanceof TemplateBuilder.GString) {
                        thatObject = new Function(thatObject, "toString", new Expression[0]);
                        return get(model);
                    } else if (!methodName.equals("execute")) {
                        thatObject = new Function(thatObject, methodName);
                        methodName = "execute";
                        return get(model);
                    }
                } catch (Exception e) {
                    if (!e.getClass().equals(NoSuchFieldException.class))
                        throw Unchecked.rethrow(e);
                }
//            System.out.println("can't find " + methodName + " for class " + thatObject.getClass(model) + "\t" + Arrays.toString(arr));
                throw new NoSuchMethodException("can't find method '" + methodName + "' for class " + getClass(instance) + " with args: " + Arrays.toString(arr));
            }
            Object result = method.invoke(instance, arr);
            if (!hardcodeChecked && (hardcoded = thatObject.hardcoded)) {
                hardcodeChecked = true;
                if (args != null)
                    for (Expression arg : args) {
                        hardcoded = arg.hardcoded;
                        if (!hardcoded)
                            break;
                    }
            }
            if (hardcoded)
                this.result = result;
            return result;
        } catch (Exception e) {
            throw Unchecked.rethrow(e);
        }
    }

    public Getter getGetter(Object instance) {
        if (getter != null)
            return getter;

        if (fieldName == null)
            return null;

        if (instance instanceof Map)
            return getter = new MapGetter(fieldName);


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

        throw Unchecked.rethrow(new NoSuchFieldException(fieldName));
    }

    public Setter getSetter(Object instance) {
        if (setter != null)
            return setter;

        if (fieldName == null)
            return null;

        if (instance instanceof Map)
            return setter = new MapSetter(fieldName);


        Class clazz = instance instanceof Class ? (Class) instance : instance.getClass();
        Field field = findField(clazz, fieldName);
        if (field != null && !Modifier.isPrivate(field.getModifiers()))
            return setter = new FieldSetter(field);

        String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = findMethod(clazz, methodName, 1);
        if (method != null)
            return setter = new MethodSetter(method);

        throw Unchecked.rethrow(new NoSuchFieldException(fieldName));
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

    protected Field findField(Class clazz, String name) {
        while (clazz != null)
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        return null;
    }

    private boolean checkMeta(Class clazz) {
        if (args != null && clazz != null) {
            Map<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>> methods;
            CollectionTools.Closure3<Object, Object, Map, Expression[]> closure = null;
            if ((methods = metaMethods.get(clazz)) != null && (closure = methods.get(methodName)) != null) {
                metaMethod = closure;
                return true;
            }
            closure = findMeta(clazz.getInterfaces());
            if (closure != null) {
                metaMethod = closure;
                return true;
            }
            return checkMeta(clazz.getSuperclass());
        }
        return false;
    }

    private CollectionTools.Closure3<Object, Object, Map, Expression[]> findMeta(Class[] classes) {
        CollectionTools.Closure3<Object, Object, Map, Expression[]> closure = null;
        Map<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>> methods;
        for (Class i : classes) {
            if (closure == null && ((methods = metaMethods.get(i)) == null || (closure = methods.get(methodName)) == null)) {
                closure = findMeta(i.getInterfaces());
            }
        }
        return closure;
    }

    private Class getClass(Object ob) {
        Class clazz = ob.getClass();
        if (clazz == Class.class) {
            return (Class) ob;
        }
        return clazz;
    }

    private Method findMethod(Class clazz, String method, Object[] args) {
        Class[] argsClasses = null;
        if (args != null) {
            argsClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    argsClasses[i] = args[i].getClass();
            }
        }
        try {
            return clazz.getMethod(method, argsClasses);
        } catch (NoSuchMethodException e) {
            //ignore
        }
        Method m = findExactMatch(clazz, method, argsClasses);
        if (m == null)
            m = findBoxedMatch(clazz, method, argsClasses);
        if (m == null)
            m = findNumberMatch(clazz, method, argsClasses);
        return m;
    }

    private Method findExactMatch(Class clazz, String method, Class[] argsClasses) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(method) && ((m.getParameterTypes().length == 0 && argsClasses == null) || m.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    arg = argsClasses[i];
                    param = m.getParameterTypes()[i];
                    if (arg != param && !param.isAssignableFrom(arg))
                        continue outer;
                }
                return m;
            }
        }
        return null;
    }

    private Method findBoxedMatch(Class clazz, String method, Class[] argsClasses) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : clazz.getMethods()) {
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

    private Method findNumberMatch(Class clazz, String method, Class[] argsClasses) {
        Class<?> arg;
        Class<?> param;
        outer:
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(method) && ((m.getParameterTypes().length == 0 && argsClasses == null) || m.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    arg = argsClasses[i];
                    param = m.getParameterTypes()[i];
                    if (param == char.class && arg == Character.class)
                        continue;
                    if (param == boolean.class && arg == Boolean.class)
                        continue;
                    int a = indexOfClass(arg, Boxed);
                    if (a < 0)
                        continue outer;
                    int p = indexOfClass(param, primitives);
                    if (p < 0)
                        continue outer;
                    if (p < a)
                        continue outer;
                }
                return m;
            }
        }
        return null;
    }

    private Constructor findConstructor(Class clazz, Object[] args) {
        Class[] argsClasses = null;
        if (args != null) {
            argsClasses = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null)
                    argsClasses[i] = args[i].getClass();
            }
        }
        try {
            return clazz.getConstructor(argsClasses);
        } catch (NoSuchMethodException e) {
            //ignore
        }
        outer:
        for (Constructor c : clazz.getConstructors()) {
            if (((c.getParameterTypes().length == 0 && argsClasses == null)
                    || c.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < c.getParameterTypes().length; i++) {
                    if (!(c.getParameterTypes()[i].equals(argsClasses[i])
                            || (boxing.containsKey(c.getParameterTypes()[i]) && boxing.get(c.getParameterTypes()[i]).equals(argsClasses[i]))
                            || (boxing.containsKey(c.getParameterTypes()[i]) && boxing.containsValue(argsClasses[i])))
                            ) {
//                        System.out.println(m.getParameterTypes()[i] + "\t" + (argsClasses[i]));
                        continue outer;
                    }
                }
                return c;
            }
        }
        return null;
    }

    private static final Map<Class, Class> boxing = new HashMap<Class, Class>() {
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

    private int indexOfClass(Class clazz, Class[] classes) {
        for (int i = 0; i < classes.length; i++) {
            if (clazz == classes[i])
                return i;
        }
        return -1;
    }

    private static final Class[] primitives = new Class[]{byte.class, short.class, char.class, int.class, long.class, float.class, double.class};
    private static final Class[] Boxed = new Class[]{Byte.class, Short.class, Character.class, Integer.class, Long.class, Float.class, Double.class};


    public Method getMethod() {
        return method;
    }

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
        return "function for: " + thatObject + " ." + (method == null ? methodName : method.getName()) + "(" + Arrays.toString(args) + ")";
    }

    public Field getField() {
        return field;
    }

    public static void setMethod(Class clazz, String methodName, CollectionTools.Closure3<Object, Object, Map, Expression[]> c) {
        Map<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>> methods = metaMethods.get(clazz);
        if (methods == null) {
            methods = new HashMap<String, CollectionTools.Closure3<Object, Object, Map, Expression[]>>();
            metaMethods.put(clazz, methods);
        }
        methods.put(methodName, c);
    }
}
