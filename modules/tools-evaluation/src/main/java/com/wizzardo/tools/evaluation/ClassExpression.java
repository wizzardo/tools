package com.wizzardo.tools.evaluation;


import com.wizzardo.tools.bytecode.ClassBuilder;
import com.wizzardo.tools.bytecode.DynamicProxy;
import com.wizzardo.tools.bytecode.DynamicProxyFactory;
import com.wizzardo.tools.misc.Unchecked;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class ClassExpression extends Expression {

    protected boolean isEnum;
    protected List<Expression> definitions;
    protected Map<String, Object> context = new HashMap<String, Object>();
    protected String packageName;
    protected String name;
    protected Class<?>[] interfaces;
    protected Class<?> proxyClass;

    public ClassExpression(String name, List<Expression> definitions) {
        this.name = name;
        this.definitions = definitions;
    }

    public ClassExpression(String name, List<Expression> definitions, Map<String, Object> context) {
        this.name = name;
        this.definitions = definitions;
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
        return new ClassExpression(name, l, new HashMap<String, Object>(context));
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
        ClassExpression instance = new ClassExpression(name, definitions);
        instance.context.put("this", instance);
        instance.init();


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
                    closure.getAgainst(instance.context, instance.context, args);
                    break;
                }
            } else if (expression instanceof ClassExpression) {
                instance.context.put(expression.toString(), expression);
            }
        }

        if (interfaces.length != 0) {
            if (getJavaClass() != null) {
                Object result = Unchecked.call(() -> proxyClass.newInstance());
                instance.context.put("this", result);

                for (Expression expression : definitions) {
                    if (expression instanceof DefineAndSet && ((DefineAndSet) expression).action instanceof Operation) {
                        Operation operation = (Operation) ((DefineAndSet) expression).action;
                        if (operation.rightPart() instanceof ClosureHolder)
                            continue;
                        throw new IllegalStateException("Define-and-set operation is not supported yet ");
                    } else if (expression instanceof Definition) {
                        instance.context.remove(((Definition) expression).name);
                    }
                }
                ((DynamicProxy) result).setHandler((that, method, args1) -> {
                    ClosureHolder closureHolder = findMethod(method, args1);
                    if (closureHolder == null)
                        throw new IllegalArgumentException("Cannot find method " + method + " " + Arrays.toString(args) + " in " + this);

                    ClosureExpression closure = (ClosureExpression) closureHolder.get(instance.context);
                    return closure.getAgainst(closure.context, result, args1);
                });
                return result;
            }

            Class<?>[] interfacesToImplement = new Class[interfaces.length + 1];
            System.arraycopy(interfaces, 0, interfacesToImplement, 0, interfaces.length);
            interfacesToImplement[interfacesToImplement.length - 1] = ClassExpressionProxy.class;
            return Proxy.newProxyInstance(
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

        return instance;
    }

    public ClosureHolder findMethod(String method, Object[] args) {
        for (int i = 0; i < definitions.size(); i++) {
            Expression e = definitions.get(i);
            if (e instanceof MethodDefinition) {
                MethodDefinition md = (MethodDefinition) e;
                if (md.name.equals(method) && md.action.closure.args.length == (args == null ? 0 : args.length))
                    return md.action;
            }
        }
        return null;
    }

    public Class<?> getJavaClass() {
        if (proxyClass != null)
            return proxyClass;

        if (DynamicProxy.SUPPORTED) {
            String fullname = ((packageName != null && !packageName.isEmpty()) ? packageName + "." : "") + name;
            ClassBuilder builder = DynamicProxyFactory.createBuilder(fullname, Object.class, interfaces);
            for (Class<?> anInterface : interfaces) {
                for (Method method : anInterface.getMethods()) {
                    if (builder.hasMethod(method))
                        continue;

                    DynamicProxyFactory.addHandlerCallForMethod(builder, method, false);
                }
            }

            for (Expression expression : definitions) {
                if (expression instanceof DefineAndSet && ((DefineAndSet) expression).action instanceof Operation) {
                    Operation operation = (Operation) ((DefineAndSet) expression).action;
                    if (operation.rightPart() instanceof ClosureHolder)
                        continue;
                    throw new IllegalStateException("Define-and-set operation is not supported yet ");
                } else if (expression instanceof Definition) {
                    builder.field(((Definition) expression).name, ((Definition) expression).type);
                }
            }
            proxyClass = DynamicProxyFactory.loadClass(fullname, builder.build());
        }

        return proxyClass;
    }

    public interface ClassExpressionProxy {
        ClassExpression getClassExpression();
    }

    protected void init() {
        for (Expression expression : definitions) {
            if (expression instanceof MethodDefinition){
                continue;
            }
            expression.get(context);
            if (expression instanceof Holder) {
                context.put(expression.exp, null);
            }
        }
    }
}
