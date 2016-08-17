package com.wizzardo.tools.evaluation;

import java.util.Map;

/**
 * Created by wizzardo on 29/08/16.
 */
public class ClosureLookup extends Expression {

    protected final String functionName;
    protected final Map<String, UserFunction> functions;

    public ClosureLookup(String functionName, Map<String, UserFunction> functions) {
        this.functionName = functionName;
        this.functions = functions;
    }

    @Override
    public void setVariable(Variable v) {
    }

    @Override
    public Expression clone() {
        return this;
    }

    @Override
    public Object get(Map<String, Object> model) {
        if(model == null)
            return lookupInFunctions();

        Object localFunction = model.get(functionName);
        if (localFunction != null && localFunction instanceof Expression) {
            return localFunction;
        } else {
            return lookupInFunctions();
        }
    }

    private Object lookupInFunctions() {
        UserFunction function = functions.get(functionName).clone();
        function.setUserFunctions(functions);
        return function;
    }
}
