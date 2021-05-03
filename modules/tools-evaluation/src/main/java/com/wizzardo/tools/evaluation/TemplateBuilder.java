package com.wizzardo.tools.evaluation;


import com.wizzardo.tools.interfaces.Supplier;

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
    public void setVariable(Variable v) {
        for (Expression e : parts)
            e.setVariable(v);
    }

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
        if (hardcoded)
            return result;

        Supplier<String>[] suppliers = new Supplier[parts.size()];
        hardcoded = true;
        for (int i = 0; i < parts.size(); i++) {
            Expression e = parts.get(i);
            final Object o = e.get(model);
            hardcoded &= e.hardcoded;
            if (e.hardcoded) {
                final String s = String.valueOf(o);
                if (parts.size() == 1)
                    return result = s;

                suppliers[i] = new Supplier<String>() {
                    @Override
                    public String supply() {
                        return s;
                    }
                };
            } else if (o instanceof ClosureExpression) {
                final ClosureExpression expression = (ClosureExpression) o;
                suppliers[i] = new Supplier<String>() {
                    @Override
                    public String supply() {
                        return String.valueOf(expression.get());
                    }
                };
            } else {
                suppliers[i] = new Supplier<String>() {
                    @Override
                    public String supply() {
                        return String.valueOf(o);
                    }
                };
            }
        }
        GString gString = new GString(suppliers);
        if (hardcoded)
            result = gString;
        return gString;
    }

    public TemplateBuilder append(Expression e) {
        parts.add(e);
        return this;
    }

    public TemplateBuilder append(String s) {
        parts.add(new Holder(s, true));
        return this;
    }

    @Override
    public String toString() {
        return parts.toString();
    }

    public static class GString {
        final Supplier<String>[] parts;

        public GString(Supplier<String>[] parts) {
            this.parts = parts;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Supplier<String> part : parts) {
                sb.append(part.supply());
            }
            return sb.toString();
        }
    }
}
