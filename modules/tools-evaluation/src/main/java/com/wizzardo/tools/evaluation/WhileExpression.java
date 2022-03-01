package com.wizzardo.tools.evaluation;

import java.util.Map;

public class WhileExpression extends Expression {
    private AsBooleanExpression condition;
    private Expression thenStatement;

    public WhileExpression(AsBooleanExpression condition, Expression thenStatement, EvaluationContext context) {
        super(context);
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    protected WhileExpression(AsBooleanExpression condition, Expression thenStatement, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
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
        return new WhileExpression((AsBooleanExpression) condition.clone(), thenStatement.clone(), file, lineNumber, linePosition);
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
        while ((Boolean) condition.get(model)) {
            Object o = thenStatement.get(model);
            if (o != null && o instanceof ReturnResultHolder)
                return o;
        }
        return null;
    }

    @Override
    public String toString() {
        return condition.toString();
    }
}
