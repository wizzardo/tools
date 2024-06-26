package com.wizzardo.tools.evaluation;


import com.wizzardo.tools.bytecode.*;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.*;
import java.util.*;

public class ClassExpression extends Expression implements Type {

    protected boolean isEnum;
    protected List<Expression> definitions;
    protected List<Expression> definitionsStatic;
    protected EvaluationContext context;
    protected String packageName;
    protected String name;
    protected Type[] interfaces;
    protected Class<?>[] interfacesRaw;
    protected Type superClass;
    protected Class<?> proxyClass;

    public ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Type superClass, Type[] interfaces, EvaluationContext parsingContext) {
        this(name, definitions, definitionsStatic, superClass, interfaces, null, parsingContext);
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Type superClass, Type[] interfaces, Class<?> proxyClass, EvaluationContext parsingContext) {
        this(name, definitions, definitionsStatic, superClass, interfaces, proxyClass, new EvaluationContext(), parsingContext);
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Type superClass, Type[] interfaces, Class<?> proxyClass, EvaluationContext context, EvaluationContext parsingContext) {
        super(parsingContext);
        this.name = name;
        this.definitions = definitions;
        this.definitionsStatic = definitionsStatic;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.interfacesRaw = typesToClasses(interfaces);
        this.proxyClass = proxyClass;
        this.context = context;
    }

    protected ClassExpression(String name, List<Expression> definitions, List<Expression> definitionsStatic, Type superClass, Type[] interfaces, Class<?> proxyClass, EvaluationContext context, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.name = name;
        this.definitions = definitions;
        this.definitionsStatic = definitionsStatic;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.interfacesRaw = typesToClasses(interfaces);
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
        if (interfacesRaw == null)
            return new Class[0];

        return Arrays.copyOf(interfacesRaw, interfacesRaw.length);
    }

    public Type[] getGenericInterfaces() {
        if (interfaces == null)
            return new Type[0];

        return Arrays.copyOf(interfaces, interfaces.length);
    }

    public Type getGenericSuperClass(){
        return superClass;
    }

    public Class<?> getSuperClass() {
        return typeToClass(superClass);
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
        return new ClassExpression(name, l, definitionsStatic, superClass, interfaces, proxyClass, new EvaluationContext((Map<String, Object>) context), file, lineNumber, linePosition);
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
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

    protected Class<?> typeToClass(Type t) {
        if (t instanceof Class)
            return (Class<?>) t;
        if (t instanceof ParameterizedType)
            return typeToClass(((ParameterizedType) t).getRawType());

        throw new IllegalArgumentException();
    }

    private Class<?>[] typesToClasses(Type[] types) {
        Class<?>[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            classes[i] = typeToClass(types[i]);
        }
        return classes;
    }

    public Object newInstance(Object[] args) {
        ClassExpression instance = new ClassExpression(name, definitions, definitionsStatic, superClass, interfaces, proxyClass, context.createLocalContext(), file, lineNumber, linePosition);
        instance.context.put("this", instance);
        instance.init();
        Object result = instance;

        if (interfaces.length != 0 || superClass != Object.class || proxyClass != null) {
            if (getJavaClass() != null) {
                result = Unchecked.call(() -> proxyClass.newInstance());
                instance.context.put("this", result);
                Field[] fields = proxyClass.getDeclaredFields();
                ((WithClassExpression) result).setClassExpression(instance);

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
                Class<?>[] interfacesToImplement = new Class[interfacesRaw.length + 1];
                System.arraycopy(interfacesRaw, 0, interfacesToImplement, 0, interfacesRaw.length);
                interfacesToImplement[interfacesToImplement.length - 1] = WithClassExpression.class;
                result = Proxy.newProxyInstance(
                        interfacesRaw[0].getClassLoader(),
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
            if (expression instanceof MethodDefinition) {
                ((MethodDefinition) expression).action.closure.setContext(instance.context);
            }

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
        if ("this".equals(method) || "%execute%".equals(method))
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
                            if (arg instanceof ClosureExpression && Function.isSAMInterface(param))
                                continue;
                            continue outer;
                        }
                    }
                }
                return md;
            }
        }
        return null;
    }

    public interface WithClassExpression {
        void setClassExpression(ClassExpression classExpression);

        ClassExpression getClassExpression();
    }

    public Class<?> getJavaClass() {
        if (proxyClass != null)
            return proxyClass;

        if (DynamicProxy.SUPPORTED) {
            String fullname = ((packageName != null && !packageName.isEmpty()) ? packageName + "." : "") + name;
            Class<?> superClazz = typeToClass(superClass);
            ClassBuilder builder = new ClassBuilder()
                    .setSuperClass(superClazz)
                    .setClassFullName(fullname)
                    .implement(DynamicProxy.class)
                    .field("handler", Handler.class, null, AccessFlags.ACC_STATIC)
                    .fieldSetter("handler")
                    .implement(WithClassExpression.class)
                    .field("classExpression", ClassExpression.class, null, AccessFlags.ACC_STATIC)
                    .fieldSetter("classExpression")
                    .fieldGetter("classExpression");

            builder.implement(interfacesRaw);
            builder.implement(superClazz.getInterfaces());

            for (Class<?> anInterface : interfacesRaw) {
                for (Method method : anInterface.getMethods()) {
                    if (builder.hasMethod(method))
                        continue;

                    DynamicProxyFactory.addHandlerCallForMethod(builder, method, false);
                }
            }

            Method[] superMethods = superClazz.getMethods();
            for (Class<?> anInterface : superClazz.getInterfaces()) {
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

                            if (value instanceof Number) {
                                if (type == int.class || type == Integer.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).intValue());
                                    continue;
                                }
                                if (type == long.class || type == Long.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).longValue());
                                    continue;
                                }
                                if (type == float.class || type == Float.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).floatValue());
                                    continue;
                                }
                                if (type == double.class || type == Double.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).doubleValue());
                                    continue;
                                }
                                if (type == short.class || type == Short.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).shortValue());
                                    continue;
                                }
                                if (type == byte.class || type == Byte.class) {
                                    builder.field(defineAndSet.name, type, ((Number) value).byteValue());
                                    continue;
                                }
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
                    Optional<Method> superMethod = Arrays.stream(superMethods).filter(it -> it.getName().equals(md.name) && Arrays.equals(it.getParameterTypes(), args)).findFirst();
                    if (superMethod.isPresent()) {
                        DynamicProxyFactory.addCallSuperMethod(builder, superMethod.get());
                    }
                }
            }

            Constructor<?>[] constructors = superClazz.getConstructors();
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
        for (Expression expression : definitionsStatic) {
            if (expression instanceof MethodDefinition) {
                ((MethodDefinition) expression).action.closure.setContext(context);
            }
        }
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
