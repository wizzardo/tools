/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import java.util.HashMap;
import java.util.Map;

enum Operator {

    PLUS("+", Operator.Requirement.BOTH, 1, false),
    PLUS2("++", Operator.Requirement.ANY, 3, false),
    MINUS("-", Operator.Requirement.BOTH, 1, false),
    MINUS2("--", Operator.Requirement.ANY, 3, false),
    MULTIPLY("*", Operator.Requirement.BOTH, 2, false),
    DIVIDE("/", Operator.Requirement.BOTH, 2, false),
    EQUAL("=", Operator.Requirement.BOTH, -2, false),
    APPEND("<<", Operator.Requirement.BOTH, -2, false),
    EQUAL2("==", Operator.Requirement.BOTH, 0, true),
    RANGE("..", Operator.Requirement.BOTH, 0, true),
    AND("&", Operator.Requirement.BOTH, -1, true),
    AND2("&&", Operator.Requirement.BOTH, -1, true),
    OR("|", Operator.Requirement.BOTH, -1, true),
    OR2("||", Operator.Requirement.BOTH, -1, true),
    NOT("!", Operator.Requirement.RIGHR, 1, true),
    NOT_EQUAL("!=", Operator.Requirement.BOTH, 1, true),
    PLUS_EQUAL("+=", Operator.Requirement.BOTH, -2, false),
    MINUS_EQUAL("-=", Operator.Requirement.BOTH, -2, false),
    MULTIPLY_EQUAL("*=", Operator.Requirement.BOTH, -2, false),
    DIVIDE_EQUAL("/=", Operator.Requirement.BOTH, -2, false),
    LOWER_EQUAL("<=", Operator.Requirement.BOTH, 1, true),
    GREATE_EQUAL(">=", Operator.Requirement.BOTH, 1, true),
    LOWER("<", Operator.Requirement.BOTH, 1, true),
    GREATE(">", Operator.Requirement.BOTH, 1, true),
    TERNARY("?", Operator.Requirement.BOTH, 4, true),
    COLON(":", Operator.Requirement.BOTH, 4, true),
    GET("%get%", Operator.Requirement.BOTH, 0, false);
    private static final Map<String, Operator> operators = new HashMap<String, Operator>();
    final public String text;
    final public Operator.Requirement requirement;
    final public int priority;
    final public boolean logical;

    static {
        for (Operator op : Operator.values()) {
            operators.put(op.text, op);
        }
    }

    private Operator(String text, Operator.Requirement requirement, int priority, boolean logical) {
        this.text = text;
        this.requirement = requirement;
        this.priority = priority;
        this.logical = logical;
    }

    public static Operator get(String text) {
        //System.out.println("try to find operator by text: \"" + text + "\"");
        return operators.get(text);
    }

    public static enum Requirement {

        LEFT, RIGHR, BOTH, ANY;
    }

    @Override
    public String toString() {
        return text;
    }
}