/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import java.util.Map;

/**
 * @author Moxa
 */
class Operation {

    private Expression leftPart;
    private Expression rightPart;
    private Operator operator;
    private int start, end;

    public Operation(Expression leftPart, Expression rightPart, Operator operator) {
        this.leftPart = leftPart;
        this.rightPart = rightPart;
        this.operator = operator;
    }

    public Operation(Expression leftPart, Operator operator, int start, int end) {
        this.leftPart = leftPart;
        this.operator = operator;
        this.start = start;
        this.end = end;
    }

    @Override
    public Operation clone() {
        if (rightPart == null) {
            return new Operation(leftPart.clone(), null, operator);
        } else {
            return new Operation(leftPart.clone(), rightPart.clone(), operator);
        }
    }

    @Override
    public String toString() {
        return "(" + leftPart + ")\t" + getOperator().text + "\t(" + rightPart + ")";
    }

    public Expression getLeftPart() {
        return leftPart;
    }

    public void setLeftPart(Expression leftPart) {
        this.leftPart = leftPart;
    }

    public Expression getRightPart() {
        return rightPart;
    }

    public void setRightPart(Expression rightPart) {
        this.rightPart = rightPart;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Expression leftPart() {
        return leftPart;
    }

    public void leftPart(Expression leftPart) {
        this.leftPart = leftPart;
    }

    public Expression rightPart() {
        return rightPart;
    }

    public void rightPart(Expression rightPart) {
        this.rightPart = rightPart;
    }

    public Operator operator() {
        return operator;
    }

    public void operator(Operator operator) {
        this.operator = operator;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int end() {
        return end;
    }

    public int start() {
        return start;
    }

    public void end(int end) {
        this.end = end;
    }

    public void start(int start) {
        this.start = start;
    }

    public boolean isFull() {
        switch (operator.requirement) {
            case ANY:
                return leftPart != null || rightPart != null;
            case RIGHR:
                return rightPart != null;
            case LEFT:
                return leftPart != null;
            case BOTH:
                return leftPart != null && rightPart != null;
        }
        return false;
    }

    public Object evaluate(Map<String, Object> model) throws Exception {
        //System.out.println("execute: " + this);
        Object ob1 = null;
        Object ob2 = null;
        if (leftPart != null) {
            ob1 = leftPart.get(model);
        }
        if (rightPart != null
                && operator != Operator.TERNARY
                && operator != Operator.AND2
                && operator != Operator.OR2) {
            ob2 = rightPart.get(model);
        }
        //System.out.println(model);
        //System.out.println(ob1 + "\t" + operator + "\t" + ob2);
        switch (operator) {
            case PLUS: {
                return plus(ob1, ob2);
            }
            case MINUS: {
                return minus(ob1, ob2);
            }
            case MULTIPLY: {
                return multiply(ob1, ob2);
            }
            case DIVIDE: {
                return divide(ob1, ob2);
            }
            case PLUS2: {
                //pre-increment
                if (rightPart != null && rightPart.exp().length() > 0) {
                    Object ob = increment(rightPart.get(model));
                    model.put(rightPart.exp(), ob);
                    return ob;
                }
                //post-increment
                if (leftPart != null && leftPart.exp().length() > 0) {
                    Object ob = leftPart.get(model);
                    //System.out.println("try do post-increment");
                    //System.out.println(leftPart);
                    //System.out.println(leftPart.exp);
                    //System.out.println(leftPart.operation);
                    //System.out.println("and?");
                    model.put(leftPart.exp(), increment(ob));
                    return ob;
                }
            }
            case MINUS2: {
                //pre-decrement
                if (rightPart != null && rightPart.exp().length() > 0) {
                    Object ob = decrement(rightPart.get(model));
                    model.put(rightPart.exp(), ob);
                    return ob;
                }
                //post-decrement
                if (leftPart != null && leftPart.exp().length() > 0) {
                    Object ob = leftPart.get(model);
                    model.put(leftPart.exp(), decrement(ob));
                    return ob;
                }
            }
            case NOT: {
                return !(Boolean) rightPart.get(model);
            }
            case GREATE: {
                return gt(ob1, ob2);
            }
            case LOWER: {
                return lt(ob1, ob2);
            }
            case GREATE_EQUAL: {
                return gte(ob1, ob2);
            }
            case LOWER_EQUAL: {
                return lte(ob1, ob2);
            }
            case EQUAL2: {
                return e(ob1, ob2);
            }
            case NOT_EQUAL: {
                return ne(ob1, ob2);
            }
            case TERNARY: {
                //System.out.println("left: " + leftPart);
                //System.out.println("right: " + rightPart);
                if ((Boolean) leftPart.get(model)) {
                    return rightPart.operation.leftPart.get(model);
                } else {
                    return rightPart.operation.rightPart.get(model);
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(ob1, ob2);
                model.put(leftPart.exp(), r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(ob1, ob2);
                model.put(leftPart.exp(), r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(ob1, ob2);
                model.put(leftPart.exp(), r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(ob1, ob2);
                model.put(leftPart.exp(), r);
                return r;
            }
            case EQUAL: {
                model.put(leftPart.exp(), ob2);
                return ob2;
            }
            case OR2: {
                if ((Boolean) ob1) {
                    return true;
                } else {
                    return rightPart().get(model);
                }
            }
            case OR: {
                if ((Boolean) ob1) {
                    rightPart().get(model);
                    return true;
                } else {
                    return rightPart().get(model);
                }
            }
            case AND2: {
                if (!(Boolean) ob1) {
                    return false;
                } else {
                    return rightPart().get(model);
                }
            }
            case AND: {
                if (!(Boolean) ob1) {
                    rightPart().get(model);
                    return false;
                } else {
                    return rightPart().get(model);
                }
            }
        }

        throw new UnsupportedOperationException("Not yet implemented:" + this.operator);
    }

    private static Object plus(Object ob1, Object ob2) {
        if (ob1 instanceof Number && ob2 instanceof Number) {
            if (ob1 instanceof Double || ob2 instanceof Double) {
                return ((Number) ob1).doubleValue() + ((Number) ob2).doubleValue();
            }
            if (ob1 instanceof Float || ob2 instanceof Float) {
                return ((Number) ob1).floatValue() + ((Number) ob2).floatValue();
            }
            if (ob1 instanceof Long || ob2 instanceof Long) {
                return ((Number) ob1).longValue() + ((Number) ob2).longValue();
            }
            if (ob1 instanceof Integer || ob2 instanceof Integer) {
                return ((Number) ob1).intValue() + ((Number) ob2).intValue();
            }
            if (ob1 instanceof Short || ob2 instanceof Short) {
                return ((Number) ob1).shortValue() + ((Number) ob2).shortValue();
            }
            if (ob1 instanceof Byte || ob2 instanceof Byte) {
                return ((Number) ob1).byteValue() + ((Number) ob2).byteValue();
            }
            return ((Number) ob1).doubleValue() + ((Number) ob2).doubleValue();
        } else {
            return String.valueOf(ob1) + String.valueOf(ob2);
        }
    }

    private static Object minus(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return (ob1 != null ? ((Number) ob1).doubleValue() : 0) - ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return (ob1 != null ? ((Number) ob1).floatValue() : 0) - ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return (ob1 != null ? ((Number) ob1).longValue() : 0) - ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return (ob1 != null ? ((Number) ob1).intValue() : 0) - ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return (ob1 != null ? ((Number) ob1).shortValue() : 0) - ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return (ob1 != null ? ((Number) ob1).byteValue() : 0) - ((Number) ob2).byteValue();
        }
        return (ob1 != null ? ((Number) ob1).doubleValue() : 0) - ((Number) ob2).doubleValue();
    }

    private static Object gt(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() > ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() > ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() > ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() > ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() > ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() > ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() > ((Number) ob2).doubleValue();
    }

    private static Object lt(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() < ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() < ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() < ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() < ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() < ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() < ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() < ((Number) ob2).doubleValue();
    }

    private static Object gte(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() >= ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() >= ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() >= ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() >= ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() >= ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() >= ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() >= ((Number) ob2).doubleValue();
    }

    private static Object lte(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() <= ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() <= ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() <= ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() <= ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() <= ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() <= ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() <= ((Number) ob2).doubleValue();
    }

    private static Object e(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            return ob1 == ob2;
        }
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() == ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() == ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() == ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() == ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() == ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() == ((Number) ob2).byteValue();
        }
        return ob1 == ob2;
    }

    private static Object ne(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            return ob1 != ob2;
        }
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() != ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() != ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() != ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() != ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() != ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() != ((Number) ob2).byteValue();
        }
        return ob1 != ob2;
    }

    private static Object multiply(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() * ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() * ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() * ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() * ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() * ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() * ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() * ((Number) ob2).doubleValue();
    }

    private static Object divide(Object ob1, Object ob2) {
        if (ob1 instanceof Double || ob2 instanceof Double) {
            return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
        }
        if (ob1 instanceof Float || ob2 instanceof Float) {
            return ((Number) ob1).floatValue() / ((Number) ob2).floatValue();
        }
        if (EvalUtils.defaultEvaluatingStrategy == EvalUtils.EvaluatingStrategy.DOUBLE) {
            return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
        }
        if (EvalUtils.defaultEvaluatingStrategy == EvalUtils.EvaluatingStrategy.FLOAT) {
            return ((Number) ob1).floatValue() / ((Number) ob2).floatValue();
        }
        if (ob1 instanceof Long || ob2 instanceof Long) {
            return ((Number) ob1).longValue() / ((Number) ob2).longValue();
        }
        if (ob1 instanceof Integer || ob2 instanceof Integer) {
            return ((Number) ob1).intValue() / ((Number) ob2).intValue();
        }
        if (ob1 instanceof Short || ob2 instanceof Short) {
            return ((Number) ob1).shortValue() / ((Number) ob2).shortValue();
        }
        if (ob1 instanceof Byte || ob2 instanceof Byte) {
            return ((Number) ob1).byteValue() / ((Number) ob2).byteValue();
        }
        return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
    }

    private static Object increment(Object ob1) {
        if (ob1 instanceof Double) {
            return ((Number) ob1).doubleValue() + 1;
        }
        if (ob1 instanceof Float) {
            return ((Number) ob1).floatValue() + 1;
        }
        if (ob1 instanceof Long) {
            return ((Number) ob1).longValue() + 1;
        }
        if (ob1 instanceof Short) {
            return ((Number) ob1).shortValue() + 1;
        }
        if (ob1 instanceof Integer) {
            return ((Number) ob1).intValue() + 1;
        }
        if (ob1 instanceof Byte) {
            return ((Number) ob1).byteValue() + 1;
        }
        return ((Number) ob1).doubleValue() + 1;
    }

    private static Object decrement(Object ob1) {
        if (ob1 instanceof Double) {
            return ((Number) ob1).doubleValue() - 1;
        }
        if (ob1 instanceof Float) {
            return ((Number) ob1).floatValue() - 1;
        }
        if (ob1 instanceof Long) {
            return ((Number) ob1).longValue() - 1;
        }
        if (ob1 instanceof Integer) {
            return ((Number) ob1).intValue() - 1;
        }
        if (ob1 instanceof Short) {
            return ((Number) ob1).shortValue() - 1;
        }
        if (ob1 instanceof Byte) {
            return ((Number) ob1).byteValue() - 1;
        }
        return ((Number) ob1).doubleValue() - 1;
    }
}