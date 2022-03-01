package com.wizzardo.tools.evaluation;

import java.util.Map;

/**
 * @author: moxa
 * Date: 11/20/13
 */
public class IfExpression extends Expression {
    private AsBooleanExpression condition;
    private Expression thenStatement;

    public IfExpression(AsBooleanExpression condition, Expression thenStatement, EvaluationContext context) {
        super(context);
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    public IfExpression(AsBooleanExpression condition, Expression thenStatement, Expression elseStatement, EvaluationContext context) {
        super(context);
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    protected IfExpression(AsBooleanExpression condition, Expression thenStatement, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.condition = condition;
        this.thenStatement = thenStatement;
    }

    private IfExpression(AsBooleanExpression condition, Expression thenStatement, Expression elseStatement, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
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
            return new IfExpression((AsBooleanExpression) condition.clone(), thenStatement.clone(), elseStatement.clone(), file, lineNumber, linePosition);
        else
            return new IfExpression((AsBooleanExpression) condition.clone(), thenStatement.clone(), file, lineNumber, linePosition);
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
        if ((Boolean) condition.get(model)) {
            return thenStatement.get(model);
        } else if (elseStatement != null) {
            return elseStatement.get(model);
        }
        return null;
    }

    @Override
    public String toString() {
        return condition.toString();
    }
}
