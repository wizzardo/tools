package org.bordl.utils.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: moxa
 * Date: 8/11/13
 */
public class ClosureExpression extends Expression {

    private List<Expression> expressions = new ArrayList<Expression>();

    @Override
    public Expression clone() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Object get(Map<String, Object> model) {
//        HashMap<String, Object> local = new HashMap<String, Object>(model);
        Object ob = null;
        for (Expression expression : expressions) {
            ob = expression.get(model);
        }
        return ob;
    }

    public void add(Expression expression) {
        expressions.add(expression);
    }
}
