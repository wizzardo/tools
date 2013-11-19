package com.wizzardo.tools.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: moxa
 * Date: 11/19/13
 */
public class TemplateBuilder extends Expression {
    private List<Expression> parts = new ArrayList<Expression>();

    @Override
    public Expression clone() {
        TemplateBuilder tb = new TemplateBuilder();
        for (Expression e : parts) {
            tb.parts.add(e.clone());
        }
        return tb;
    }

    @Override
    public Object get(Map<String, Object> model) {
        if (parts.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        for (Expression e : parts)
            sb.append(e.get(model));

        return sb.toString();
    }

    public TemplateBuilder append(Expression e) {
        parts.add(e);
        return this;
    }

    public TemplateBuilder append(String s) {
        parts.add(new Holder(s, true));
        return this;
    }
}
