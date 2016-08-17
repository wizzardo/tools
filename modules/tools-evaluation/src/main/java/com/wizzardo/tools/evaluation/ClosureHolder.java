package com.wizzardo.tools.evaluation;

import java.util.Map;

/**
 * Created by wizzardo on 29/08/16.
 */
public class ClosureHolder extends Expression {

    protected final ClosureExpression closure;

    public ClosureHolder(ClosureExpression closure) {
        this.closure = closure;
    }

    @Override
    public void setVariable(Variable v) {
        closure.setVariable(v);
    }

    @Override
    public Expression clone() {
        return this;
    }

    @Override
    public Object get(Map<String, Object> model) {
        return closure;
    }
}
