package com.wizzardo.tools.evaluation;

import java.util.Map;

public class ForExpression extends Expression {
    private Expression definition;
    private AsBooleanExpression condition;
    private Expression iterator;
    private Expression thenStatement;

    public ForExpression(Expression definition, AsBooleanExpression condition, Expression iterator, Expression thenStatement, EvaluationContext context) {
        super(context);
        this.definition = definition;
        this.condition = condition;
        this.iterator = iterator;
        this.thenStatement = thenStatement;
    }

    protected ForExpression(Expression definition, AsBooleanExpression condition, Expression iterator, Expression thenStatement, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
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
        return new ForExpression(definition.clone(), (AsBooleanExpression) condition.clone(), iterator.clone(), thenStatement.clone(), file, lineNumber, linePosition);
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
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
            Expression def = definition;
            if (def instanceof DefineAndSet) {
                model.remove(((DefineAndSet) def).name);
            } else if (def instanceof Operation && ((Operation) def).leftPart() instanceof Holder) {
                String exp = ((Operation) def).leftPart().exp;
                model.remove(exp);
            }
        }
    }
}
