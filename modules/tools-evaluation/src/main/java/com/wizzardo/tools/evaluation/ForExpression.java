package com.wizzardo.tools.evaluation;

import java.util.Map;

public class ForExpression extends Expression {
    private Expression definition;
    private AsBooleanExpression condition;
    private Expression iterator;
    private Expression thenStatement;

    public ForExpression(Expression definition, AsBooleanExpression condition, Expression iterator, Expression thenStatement) {
        this.definition = definition;
        this.condition = condition;
        this.iterator = iterator;
        this.thenStatement = thenStatement;
    }

    @Override
    public void setVariable(Variable v) {
        definition.setVariable(v);
        condition.setVariable(v);
        iterator.setVariable(v);
        thenStatement.setVariable(v);
    }

    @Override
    public Expression clone() {
        return new ForExpression(definition.clone(), (AsBooleanExpression) condition.clone(), iterator.clone(), thenStatement.clone());
    }

    @Override
    public Object get(Map<String, Object> model) {
        definition.get(model);
        try {
            while ((Boolean) condition.get(model)) {
                Object o = thenStatement.get(model);
                if (o != null && o instanceof ReturnResultHolder)
                    return o;
                iterator.get(model);
            }
            return null;
        } finally {
            if (definition instanceof Operation && ((Operation) definition).leftPart() instanceof Holder) {
                String exp = ((Operation) definition).leftPart().exp;
                model.remove(exp);
            }
        }
    }
}
