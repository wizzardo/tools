package com.wizzardo.tools.evaluation;


import com.wizzardo.tools.bytecode.ClassBuilder;
import com.wizzardo.tools.bytecode.DynamicProxy;
import com.wizzardo.tools.bytecode.DynamicProxyFactory;
import com.wizzardo.tools.bytecode.Handler;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.*;
import java.util.*;

public class ClassExpression extends Expression {

    protected boolean isEnum;
    protected List<Expression> definitions;
    protected List<Expression> definitionsStatic;
    protected Map<String, Object> context;
    protected String packageName;
    protected String name;
    protected Class<?>[] interfaces;
    protected Class<?> superClass;
    protected Class<?> proxyClass;

    public ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Class<?> superClass, Class<?>[] interfaces, EvaluationContext evaluationContext) {
        this(name, definitions, definitionsStatic, superClass, interfaces, null, evaluationContext);
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Class<?> superClass, Class<?>[] interfaces, Class<?> proxyClass, EvaluationContext evaluationContext) {
        this(name, definitions, definitionsStatic, superClass, interfaces, proxyClass, new HashMap<>(), evaluationContext);
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Class<?> superClass, Class<?>[] interfaces, Class<?> proxyClass, Map<String, Object> context, EvaluationContext evaluationContext) {
        super(evaluationContext);
        this.name = name;
        this.definitions = definitions;
        this.definitionsStatic = definitionsStatic;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.proxyClass = proxyClass;
        this.context = context;
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Class<?> superClass, Class<?>[] interfaces, Class<?> proxyClass, Map<String, Object> context, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.name = name;
        this.definitions = definitions;
        this.definitionsStatic = definitionsStatic;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.proxyClass = proxyClass;
        this.context = context;
    }

    public List<Expression> getDefinitions() {
        return definitions;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public void setVariable(Variable v) {
    }

    public Class<?>[] getInterfaces() {
        if (interfaces == null)
            return new Class[0];

        return Arrays.copyOf(interfaces, interfaces.length);
    }

    @Override
    public Expression clone() {
        List<Expression> l = new ArrayList<Expression>(definitions.size());
        for (Expression expression : definitions) {
            l.add(expression.clone());
        }
        List<Expression> definitionsStatic = new ArrayList<Expression>(this.definitionsStatic.size());
        for (Expression expression : this.definitionsStatic) {
            definitionsStatic.add(expression.clone());
        }
        return new ClassExpression(name, l, definitionsStatic, superClass, interfaces, proxyClass, new HashMap<>(context), file, lineNumber, linePosition);
    }

    @Override
    public Object get(Map<String, Object> model) {
//        HashMap<String, Object> local = new HashMap<String, Object>(model);
//        Object ob = null;
//        for (Expression expression : definitions) {
//            ob = expression.get(model);
//        }
        return this;
    }

    @Override
    public String toString() {
        return "class " + name;// + " " + definitions.toString();
    }

    public Object newInstance(Object[] args) {
        ClassExpression instance = new ClassExpression(name, definitions, definitionsStatic, superClass, interfaces, proxyClass, new HashMap<>(), file, lineNumber, linePosition);
        instance.context.put("this", instance);
        instance.init();
        Object result = instance;

        if (interfaces.length != 0 || superClass != Object.class) {
            if (getJavaClass() != null) {
                result = Unchecked.call(() -> proxyClass.newInstance());
                instance.context.put("this", result);
                Field[] fields = proxyClass.getDeclaredFields();

                for (Expression expression : definitions) {
                    if (expression instanceof DefineAndSet && ((DefineAndSet) expression).action instanceof Operation) {
                        DefineAndSet defineAndSet = (DefineAndSet) expression;
                        Operation operation = (Operation) defineAndSet.action;
                        if (operation.rightPart() instanceof ClosureHolder)
                            continue;

                        if (Arrays.stream(fields).anyMatch(it -> it.getName().equals(defineAndSet.name))) {
                            instance.context.remove(defineAndSet.name);
                            continue;
                        }
                        throw new IllegalStateException("Define-and-set operation is not supported yet ");
                    } else if (expression instanceof Definition) {
                        instance.context.remove(((Definition) expression).name);
                    }
                }
                DynamicProxy proxy = (DynamicProxy) result;
                proxy.setHandler((that, method, args1) -> {
                    MethodDefinition md = findMethod(method, args1);
                    if (md == null)
                        throw new IllegalArgumentException("Cannot find method " + method + " " + Arrays.toString(args1) + " in " + this);

                    ClosureExpression closure = (ClosureExpression) md.action.get(instance.context);
                    Object r = closure.getAgainst(closure.context, proxy, args1);
                    if (r instanceof ClosureExpression && !md.returnType.isAssignableFrom(r.getClass()) && Function.isSAMInterface(md.returnType)) {
                        return Function.wrapClosureAsProxy((ClosureExpression) r, md.returnType);
                    }
                    return r;
                });
            } else {
                Class<?>[] interfacesToImplement = new Class[interfaces.length + 1];
                System.arraycopy(interfaces, 0, interfacesToImplement, 0, interfaces.length);
                interfacesToImplement[interfacesToImplement.length - 1] = ClassExpressionProxy.class;
                result = Proxy.newProxyInstance(
                        interfaces[0].getClassLoader(),
                        interfacesToImplement,
                        (proxy, method, args1) -> {
//                            System.out.println(method+" "+Arrays.toString(args));
                            if (method.getName().equals("getClassExpression"))
                                return instance;

                            ClosureExpression closure = (ClosureExpression) instance.context.get(method.getName());
                            return closure.get(closure.context, args1);
                        });
            }
        }

        for (Expression expression : definitions) {
            if (expression instanceof DefineAndSet && ((DefineAndSet) expression).action instanceof Operation) {
                Operation operation = (Operation) ((DefineAndSet) expression).action;
                if (operation.operator() == Operator.EQUAL && operation.leftPart().exp.equals(name)) {
                    ClosureHolder closureHolder = (ClosureHolder) operation.rightPart();
                    ClosureExpression closure = closureHolder.closure;
                    int length = args == null ? 0 : args.length;
                    if (closure.args.length == length) {
                        closure.getAgainst(instance.context, instance.context, args);
                        break;
                    }
                }
            } else if (expression instanceof MethodDefinition && ((MethodDefinition) expression).name.equals(name)) {
                ClosureHolder closureHolder = ((MethodDefinition) expression).action;
                ClosureExpression closure = closureHolder.closure;
                int length = args == null ? 0 : args.length;
                if (closure.args.length == length) {
                    if (result instanceof ClassExpression)
                        closure.getAgainst(instance.context, instance.context, args);
                    else
                        closure.getAgainst(instance.context, result, args);
                    break;
                }
            } else if (expression instanceof ClassExpression) {
                instance.context.put(expression.toString(), expression);
            }
        }

        return result;
    }

    public MethodDefinition findMethod(String method, Object[] args) {
        if ("this".equals(method))
            method = name;

        MethodDefinition md = findMethod(definitions, method, args);
        if (md != null)
            return md;

        return findMethod(definitionsStatic, method, args);
    }

    protected MethodDefinition findMethod(List<Expression> definitions, String method, Object[] args) {
        outer:
        for (int i = 0; i < definitions.size(); i++) {
            Expression e = definitions.get(i);
            if (e instanceof MethodDefinition) {
                MethodDefinition md = (MethodDefinition) e;
                if (!md.name.equals(method))
                    continue;
                if (md.action.closure.args.length != (args == null ? 0 : args.length))
                    continue;
                for (int j = 0; j < md.action.closure.args.length; j++) {
                    Pair<String, Class> pair = md.action.closure.args[j];
                    Class param = pair.value;
                    Object arg = args[j];
                    if (arg == null) {
                        if (param.isPrimitive())
                            continue outer;
                    } else {
                        Class<?> argClass = arg.getClass();
                        if (param.isPrimitive()) {
                            if (Function.boxing.get(param) == argClass)
                                continue;
                            int a = Function.indexOfClass(argClass, Function.Boxed);
                            if (a < 0)
                                continue outer;
                            int p = Function.indexOfClass(param, Function.primitives);
                            if (p < 0)
                                continue outer;
                            if (p < a)
                                continue outer;
                        } else if (argClass != param && !param.isAssignableFrom(argClass)) {
                            continue outer;
                        }
                    }
                }
                return md;
            }
        }
        return null;
    }

    public Class<?> getJavaClass() {
        if (proxyClass != null)
            return proxyClass;

        if (DynamicProxy.SUPPORTED) {
            String fullname = ((packageName != null && !packageName.isEmpty()) ? packageName + "." : "") + name + "Proxy";
            ClassBuilder builder = new ClassBuilder()
                    .setSuperClass(superClass)
                    .setClassFullName(fullname)
                    .implement(DynamicProxy.class)
                    .field("handler", Handler.class)
                    .fieldSetter("handler");

            builder.implement(interfaces);
            builder.implement(superClass.getInterfaces());

            for (Class<?> anInterface : interfaces) {
                for (Method method : anInterface.getMethods()) {
                    if (builder.hasMethod(method))
                        continue;

                    DynamicProxyFactory.addHandlerCallForMethod(builder, method, false);
                }
            }

            Method[] superMethods = superClass.getMethods();
            for (Class<?> anInterface : superClass.getInterfaces()) {
                for (Method method : anInterface.getMethods()) {
                    if (builder.hasMethod(method))
                        continue;

                    if (Arrays.stream(superMethods).anyMatch(it -> it.getName().equals(method.getName())
                            && !Modifier.isAbstract(it.getModifiers())
                            && it.getParameterCount() == method.getParameterCount()
                            && Arrays.equals(it.getParameterTypes(), method.getParameterTypes())
                    )) {
                        continue;
                    }

                    DynamicProxyFactory.addHandlerCallForMethod(builder, method, false);
                }
            }

            for (Method method : DynamicProxy.class.getMethods()) {
                if (builder.hasMethod(method))
                    continue;

                DynamicProxyFactory.addHandlerCallForMethod(builder, method, false);
            }

            for (Expression expression : definitions) {
                if (expression instanceof DefineAndSet && ((DefineAndSet) expression).action instanceof Operation) {
                    DefineAndSet defineAndSet = (DefineAndSet) expression;
                    Operation operation = (Operation) defineAndSet.action;
                    if (operation.rightPart() instanceof ClosureHolder)
                        continue;

                    Class type = defineAndSet.type == null ? Object.class : defineAndSet.type;
                    if (operation.rightPart() instanceof Function) {
                        Function function = (Function) operation.rightPart();
                        if (EvalTools.CONSTRUCTOR.equals(function.methodName)) {
                            Class<?> clazz = (Class) function.thatObject.get(Collections.emptyMap());
                            Object[] args = function.args == null ? new Object[0] : function.resolveArgs(Collections.emptyMap());
                            Constructor<?> constructor = function.findConstructor(clazz, args, new ArrayList<>());
                            builder.fieldCallConstructor(defineAndSet.name, type, constructor, args);
                            continue;
                        }
                        Object value = function.get(Collections.emptyMap());
                        if (value != null) {
                            Class<?> valueClass = value.getClass();
                            if (type.isAssignableFrom(valueClass) || Function.boxing.get(type) == valueClass) {
                                builder.field(defineAndSet.name, type, value);
                                continue;
                            }
                        }
                    }
                    if (operation.rightPart() instanceof Holder && operation.rightPart().isHardcoded()) {
                        Object value = operation.rightPart().get();
                        if (value != null) {
                            Class<?> valueClass = value.getClass();
                            if (type.isAssignableFrom(valueClass) || Function.boxing.get(type) == valueClass) {
                                builder.field(defineAndSet.name, type, value);
                                continue;
                            }
                        }
                    }

                    throw new IllegalStateException("Cannot initialize variable " + defineAndSet.type + " " + defineAndSet.name + " with " + defineAndSet.action);
                } else if (expression instanceof Definition) {
                    Definition definition = (Definition) expression;
                    Class type = definition.type == null ? Object.class : definition.type;
                    builder.field(definition.name, type);
                } else if (expression instanceof MethodDefinition) {
                    MethodDefinition md = (MethodDefinition) expression;
                    Class[] args = new Class[md.action.closure.args.length];
                    for (int i = 0; i < md.action.closure.args.length; i++) {
                        args[i] = md.action.closure.args[i].value;
                    }
                    if (builder.hasMethod(md.name, args, md.returnType))
                        continue;

                    DynamicProxyFactory.addHandlerCallForMethod(builder, md.name, args, md.returnType);
                }
            }

            Constructor<?>[] constructors = superClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                builder.withSuperConstructor(constructor.getParameterTypes());
            }
            proxyClass = DynamicProxyFactory.loadClass(fullname, builder.build());
        }

        return proxyClass;
    }

    public interface ClassExpressionProxy {
        ClassExpression getClassExpression();
    }

    protected void init() {
        init(definitions);
    }

    protected void initStatic() {
        init(definitionsStatic);
    }

    protected void init(List<Expression> definitions) {
        for (Expression expression : definitions) {
            if (expression instanceof MethodDefinition) {
                continue;
            }
            expression.get(context);
            if (expression instanceof Holder) {
                context.put(expression.exp, null);
            }
        }
    }
}
