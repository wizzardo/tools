package com.wizzardo.tools.evaluation;

import java.util.Map;

public class WhileExpression extends Expression {
    private AsBooleanExpression condition;
    private Expression thenStatement;

    public WhileExpression(AsBooleanExpression condition, Expression thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    @Override
    public void setVariable(Variable v) {
        condition.setVariable(v);
        thenStatement.setVariable(v);
    }

    @Override
    public Expression clone() {
        return new WhileExpression((AsBooleanExpression) condition.clone(), thenStatement.clone());
    }

    @Override
    public Object get(Map<String, Object> model) {
        while ((Boolean) condition.get(model)) {
            thenStatement.get(model);
        }
        return null;
    }
}
