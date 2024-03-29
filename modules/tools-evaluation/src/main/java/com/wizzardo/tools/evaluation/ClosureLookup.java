package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.bytecode.DynamicProxy;

import java.util.Map;

/**
 * Created by wizzardo on 29/08/16.
 */
public class ClosureLookup extends Expression {

    protected final String functionName;
    protected final Map<String, UserFunction> functions;
    protected final Object[] args;
    protected final ClassExpression parent;

    public ClosureLookup(String functionName, Map<String, UserFunction> functions, int argsCount, ClassExpression parent, EvaluationContext context) {
        super(context);
        this.functionName = functionName;
        this.functions = functions;
        this.parent = parent;
        args = new Object[argsCount];
    }

    protected ClosureLookup(String functionName, Map<String, UserFunction> functions, int argsCount, ClassExpression parent, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.functionName = functionName;
        this.functions = functions;
        this.parent = parent;
        args = new Object[argsCount];
    }

    @Override
    public void setVariable(Variable v) {
    }

    @Override
    public Expression clone() {
        return this;
    }

    @Override
    protected Object doExecute(Map<String, Object> model) {
        return get(model, args);
    }

    public Object get(Map<String, Object> model, Object[] args) {
        if (model == null)
            return lookupInFunctions();

        Object localFunction = model.get(functionName);
        if (localFunction instanceof Expression) {
            return localFunction;
        }

        Object that = model.get("this");
        if (that instanceof ClassExpression.WithClassExpression) {
            ClassExpression classExpression = ((ClassExpression.WithClassExpression) that).getClassExpression();
            MethodDefinition method = classExpression.findMethod(functionName, args);
            if (method != null)
                return method.action.get(model);
        }
        if (that instanceof ClassExpression) {
            MethodDefinition method = ((ClassExpression) that).findMethod(functionName, args);
            if (method != null)
                return method.action.get(model);
        }
        if (that instanceof DynamicProxy && !functionName.equals("this")) {
            return null;
        }
        if (parent != null) {
            MethodDefinition method = parent.findMethod(functionName, args);
            if (method != null)
                return method.action.closure;
        }

        localFunction = lookupInFunctions();
        if (localFunction instanceof Expression) {
            return localFunction;
        }

        return that;
    }

    private Object lookupInFunctions() {
        UserFunction userFunction = functions.get(functionName);
        if (userFunction == null)
            return null;

        UserFunction function = userFunction.clone();
        function.setUserFunctions(functions);
        return function;
    }

    @Override
    public String toString() {
        return functionName;
    }
}
