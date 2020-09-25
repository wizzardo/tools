package com.wizzardo.tools.evaluation;


import java.util.*;

public class ClassExpression extends Expression {

    protected List<Expression> definitions;
    protected Map<String, Object> context = new HashMap<String, Object>();
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

    @Override
    public void setVariable(Variable v) {
        for (Expression e : definitions)
            e.setVariable(v);
    }

    @Override
    public Expression clone() {
        throw new UnsupportedOperationException("Not implemented yet.");
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
        return "class " + name + " " + definitions.toString();
    }

    public ClassExpression newInstance(Object[] objects) {
        ClassExpression instance = new ClassExpression(name, definitions);
        instance.init();
        return instance;
    }

    protected void init() {
        for (Expression expression : definitions) {
            expression.get(context);
        }
    }
}
