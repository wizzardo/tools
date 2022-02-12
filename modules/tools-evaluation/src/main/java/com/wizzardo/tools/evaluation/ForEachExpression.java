package com.wizzardo.tools.evaluation;

import java.util.Iterator;
import java.util.Map;

public class ForEachExpression extends Expression {
    private Expression definition;
    private Expression iterable;
    private Expression thenStatement;

    public ForEachExpression(Expression definition, Expression iterable, Expression thenStatement, EvaluationContext context) {
        super(context);
        this.definition = definition;
        this.iterable = iterable;
        this.thenStatement = thenStatement;
    }

    protected ForEachExpression(Expression definition, Expression iterable, Expression thenStatement, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.definition = definition;
        this.iterable = iterable;
        this.thenStatement = thenStatement;
    }

    @Override
    public void setVariable(Variable v) {
        definition.setVariable(v);
        iterable.setVariable(v);
        thenStatement.setVariable(v);
    }

    @Override
    public Expression clone() {
        return new ForEachExpression(definition.clone(), iterable.clone(), thenStatement.clone(), file, lineNumber, linePosition);
    }

    @Override
    public Object get(Map<String, Object> model) {
        String variable = definition.exp;
        try {
            Iterable it = (Iterable) iterable.get(model);
            Iterator iterator = it.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                model.put(variable, next);
                Object o = thenStatement.get(model);
                if (o != null && o instanceof ReturnResultHolder)
                    return o;
            }
            return null;
        } finally {
            model.remove(variable);
        }
    }
}
