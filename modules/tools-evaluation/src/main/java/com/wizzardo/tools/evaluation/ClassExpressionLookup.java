package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.misc.Unchecked;
import java.util.Map;

public class ClassExpressionLookup extends Expression {

    protected final String className;
    protected final String key;
    protected ClassExpression ce;

    public ClassExpressionLookup(String className, EvaluationContext context) {
        super(context);
        this.className = className;
        this.key = "class " + className;
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
        if (ce == null) {
            ce = (ClassExpression) model.get(key);
        }
        if (ce == null) {
            Unchecked.rethrow(new ClassNotFoundException("Can not find class '" + className + "'"));
        }
        return ce;
    }

    @Override
    public String toString() {
        return className;
    }
}
