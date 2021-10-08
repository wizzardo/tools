package com.wizzardo.tools.evaluation;


import java.util.*;

public class ClassExpression extends Expression {

    protected boolean isEnum;
    protected List<Expression> definitions;
    protected Map<String, Object> context = new HashMap<String, Object>();
    protected String packageName;
    protected String name;

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

    public ClassExpression newInstance(Object[] args) {
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
            }
        }

        return instance;
    }

    protected void init() {
        for (Expression expression : definitions) {
            expression.get(context);
            if (expression instanceof Holder) {
                context.put(expression.exp, null);
            }
        }
    }
}
