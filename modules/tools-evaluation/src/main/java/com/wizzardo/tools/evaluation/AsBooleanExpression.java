package com.wizzardo.tools.evaluation;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * @author: moxa
 * Date: 11/20/13
 */
public class AsBooleanExpression extends Expression {

    private Expression condition;
    private Boolean result;

    public AsBooleanExpression(Expression condition, EvaluationContext context) {
        super(context);
        this.condition = condition;
    }

    protected AsBooleanExpression(Expression condition, String file, int lineNumber, int linePosition) {
        super(file, lineNumber, linePosition);
        this.condition = condition;
    }

    @Override
    public void setVariable(Variable v) {
        condition.setVariable(v);
    }

    @Override
    public Expression clone() {
        return new AsBooleanExpression(condition.clone(), file, lineNumber, linePosition);
    }

    @Override
    public Object get(Map<String, Object> model) {
        if (result != null)
            return result;

        Object r = condition.get(model);
        Boolean result = toBoolean(r);

        if (condition.hardcoded)
            this.result = result;

        return result;
    }

    @Override
    public String toString() {
        return condition.toString();
    }

    public Expression getCondition() {
        return condition;
    }

    public static Boolean toBoolean(Object r) {
        if (r == null) {
            return false;
        } else if (r instanceof Boolean) {
            return (Boolean) r;
        } else if (r instanceof String || r instanceof TemplateBuilder.GString) {
            return r.toString().length() > 0;
        } else if (r instanceof Collection) {
            return !((Collection) r).isEmpty();
        } else if (r instanceof Map) {
            return !((Map) r).isEmpty();
        } else if (r instanceof Number) {
            return ((Number) r).doubleValue() != 0;
        } else if (r instanceof Iterator) {
            return ((Iterator) r).hasNext();
        } else if (r instanceof Enumeration) {
            return ((Enumeration) r).hasMoreElements();
        }
//        else if (r instanceof Matcher) {
//            Matcher m = (Matcher) r;
//            result = m.start() != -1; }
        else {
            return true;
        }
    }
}
