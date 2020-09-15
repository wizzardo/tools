package com.wizzardo.tools.evaluation;

import java.util.Map;

/**
 * @author: moxa
 * Date: 11/20/13
 */
public class IfExpression extends Expression {
    private AsBooleanExpression condition;
    private Expression thenStatement;

    public IfExpression(AsBooleanExpression condition, Expression thenStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    public IfExpression(AsBooleanExpression condition, Expression thenStatement, Expression elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    private Expression elseStatement;

    @Override
    public void setVariable(Variable v) {
        condition.setVariable(v);
        thenStatement.setVariable(v);
        if (elseStatement != null)
            elseStatement.setVariable(v);
    }

    @Override
    public Expression clone() {
        if (elseStatement != null)
            return new IfExpression((AsBooleanExpression) condition.clone(), thenStatement.clone(), elseStatement.clone());
        else
            return new IfExpression((AsBooleanExpression) condition.clone(), thenStatement.clone());
    }

    @Override
    public Object get(Map<String, Object> model) {
        if ((Boolean) condition.get(model)) {
            return thenStatement.get(model);
        } else if (elseStatement != null) {
            return elseStatement.get(model);
        }
        return null;
    }
}
