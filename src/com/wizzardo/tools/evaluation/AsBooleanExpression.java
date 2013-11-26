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

    public AsBooleanExpression(Expression condition) {
        this.condition = condition;
    }

    @Override
    public Expression clone() {
        return new AsBooleanExpression(condition.clone());
    }

    @Override
    public Object get(Map<String, Object> model) {
        if (result != null)
            return result;

        Object r = condition.get(model);
        Boolean result;
        if (r == null) {
            result = false;
        } else if (r instanceof Boolean) {
            result = (Boolean) r;
        } else if (r instanceof String) {
            result = ((String) r).length() > 0;
        } else if (r instanceof Collection) {
            result = !((Collection) r).isEmpty();
        } else if (r instanceof Map) {
            result = !((Map) r).isEmpty();
        } else if (r instanceof Number) {
            result = ((Number) r).doubleValue() != 0;
        } else if (r instanceof Iterator) {
            result = ((Iterator) r).hasNext();
        } else if (r instanceof Enumeration) {
            result = ((Enumeration) r).hasMoreElements();
        }
//        else if (r instanceof Matcher) {
//            Matcher m = (Matcher) r;
//            result = m.start() != -1; }
        else {
            result = true;
        }

        if (condition.hardcoded)
            this.result = result;

        return result;
    }
}
