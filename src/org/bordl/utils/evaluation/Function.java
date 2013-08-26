/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import org.bordl.utils.CollectionUtils;
import org.bordl.utils.WrappedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private String methodName;
    private Expression[] args;
    private String fieldName;
    private boolean hardcodeChecked = false;
    private boolean metaChecked = false;
    private CollectionUtils.Closure3<Object, Object, Map, Object[]> metaMethod;

    private static Map<Class, Map<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>>> metaMethods = new HashMap<Class, Map<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>>>();

    public Function(Expression thatObject, Method method, Expression[] args) {
        this.thatObject = thatObject;
        this.method = method;
        this.args = args;
    }

    public Function(Expression thatObject, String methodName, Expression[] args) {
        this.thatObject = thatObject;
        this.args = args;
        this.methodName = methodName;
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

    public Function(Expression thatObject, Field field) {
        this.thatObject = thatObject;
        this.field = field;
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
            return new Function(thatObject, field);
        }
        if (fieldName != null) {
            return new Function(thatObject, fieldName);
        }
        if (method != null) {
            return new Function(thatObject.clone(), method, args);
        }
        return new Function(thatObject.clone(), methodName, args);
    }

    public Object get(Map<String, Object> model) {
        if (hardcoded) {
            return result;
        }
        try {
            Object[] arr = null;
//        System.out.println("evaluate method: "+toString());

            Object instance = thatObject.get(model);
            if (!metaChecked) {
                chechMeta(instance);
                metaChecked = true;
            }

            if (args != null) {
                arr = new Object[args.length];
//            System.out.println("try resolve args:");
                for (int i = 0; i < arr.length; i++) {
//                System.out.println(i+"\t"+args[i]);
                    if (args[i] instanceof ClosureExpression)
                        arr[i] = args[i];
                    else
                        arr[i] = args[i].get(model);
                }
            }

            if (metaMethod != null) {
                return metaMethod.execute(instance, model, arr);
            }


            if (fieldName != null || field != null) {
                if (instance instanceof Map) {
                    return ((Map) instance).get(fieldName);
                }
                prepareField(instance);
                return field.get(instance);
            }
            if (EvalUtils.CONSTRUCTOR.equals(methodName)) {
                constructor = findConstructor(getClass(instance), arr);
//            System.out.println(constructor);
//            System.out.println(Arrays.toString(constructor.getParameterTypes()));
//            System.out.println(Arrays.toString(arr));
                return constructor.newInstance(arr);
            } else if (method == null) {
                method = findMethod(getClass(instance), methodName, arr);
            }
            if (method == null) {
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
            throw new WrappedException(e);
        }
    }

    private boolean chechMeta(Object instance) {
        if (args != null && instance != null) {
            Map<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>> methods;
            Class clazz = instance.getClass();
            CollectionUtils.Closure3<Object, Object, Map, Object[]> closure = null;
            while (clazz != null && ((methods = metaMethods.get(clazz)) == null || (closure = methods.get(methodName)) == null)) {
                clazz = clazz.getSuperclass();
            }
            if (closure != null) {
                metaMethod = closure;
                return true;
            }
            Class[] classes = instance.getClass().getInterfaces();
            closure = findMeta(classes);
            if (closure != null) {
                metaMethod = closure;
                return true;
            }
        }
        return false;
    }

    private CollectionUtils.Closure3<Object, Object, Map, Object[]> findMeta(Class[] classes) {
        CollectionUtils.Closure3<Object, Object, Map, Object[]> closure = null;
        Map<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>> methods;
        for (Class i : classes) {
            if (closure == null && ((methods = metaMethods.get(i)) == null || (closure = methods.get(methodName)) == null)) {
                closure = findMeta(i.getInterfaces());
            }
        }
        return closure;
    }

    public Field prepareField(Object instance) {
        if (field == null && fieldName != null) {
            Class clazz = instance instanceof Class ? (Class) instance : instance.getClass();
            while (clazz != null && field == null)
                try {
                    field = clazz.getDeclaredField(fieldName);
                    fieldName = null;
                } catch (NoSuchFieldException ignored) {
                    clazz = clazz.getSuperclass();
                }
            if (field == null)
                throw new WrappedException(new NoSuchFieldException(fieldName));
        }
        return field;
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
        outer:
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(method) && ((m.getParameterTypes().length == 0 && argsClasses == null) || m.getParameterTypes().length == argsClasses.length)) {
//                System.out.println("check args");
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    if (!(m.getParameterTypes()[i].equals(argsClasses[i])
                            || (boxing.containsKey(m.getParameterTypes()[i]) && boxing.get(m.getParameterTypes()[i]).equals(argsClasses[i]))
                            || (boxing.containsKey(m.getParameterTypes()[i]) && boxing.containsValue(argsClasses[i])))
                            ) {
//                        System.out.println(m.getParameterTypes()[i] + "\t" + (argsClasses[i]));
                        continue outer;
                    }
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

    public static void setMethod(Class clazz, String methodName, CollectionUtils.Closure3<Object, Object, Map, Object[]> c) {
        Map<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>> methods = metaMethods.get(clazz);
        if (methods == null) {
            methods = new HashMap<String, CollectionUtils.Closure3<Object, Object, Map, Object[]>>();
            metaMethods.put(clazz, methods);
        }
        methods.put(methodName, c);
    }
}
