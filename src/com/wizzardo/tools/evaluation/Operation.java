/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.Range;
import com.wizzardo.tools.WrappedException;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * @author Moxa
 */
class Operation extends Expression {

    private Expression leftPart;
    private Expression rightPart;
    private Operator operator;
    private int start, end;
    private volatile boolean checkedForSimplify = false;

    public Operation(Expression leftPart, Expression rightPart, Operator operator) {
        this.leftPart = wrapBooleanOperationLeft(leftPart, operator);
        this.rightPart = wrapBooleanOperationRight(rightPart, operator);
        this.operator = operator;
    }

    public Operation(Expression leftPart, Operator operator, int start, int end) {
        this.leftPart = wrapBooleanOperationLeft(leftPart, operator);
        this.operator = operator;
        this.start = start;
        this.end = end;
    }

    private Expression wrapBooleanOperationLeft(Expression exp, Operator operator) {
        switch (operator) {
            case AND:
            case AND2:
            case OR:
            case OR2:
            case TERNARY:
                if (exp != null)
                    return new AsBooleanExpression(exp);
        }
        return exp;
    }

    private Expression wrapBooleanOperationRight(Expression exp, Operator operator) {
        switch (operator) {
            case AND:
            case AND2:
            case OR:
            case OR2:
            case NOT:
                if (exp != null)
                    return new AsBooleanExpression(exp);
        }
        return exp;
    }

    @Override
    public void setVariable(Variable v) {
        if (leftPart != null)
            leftPart.setVariable(v);
        if (rightPart != null)
            rightPart.setVariable(v);
    }

    @Override
    public Operation clone() {
        return new Operation(leftPart == null ? null : leftPart.clone(), rightPart == null ? null : rightPart.clone(), operator);
    }

    @Override
    public String toString() {
        return (leftPart == null ? "" : leftPart + " ") + getOperator().text + (rightPart == null ? "" : " " + rightPart);
    }

    public Expression getLeftPart() {
        return leftPart;
    }

    public void setLeftPart(Expression leftPart) {
        this.leftPart = wrapBooleanOperationLeft(leftPart, operator);
    }

    public Expression getRightPart() {
        return rightPart;
    }

    public void setRightPart(Expression rightPart) {
        this.rightPart = wrapBooleanOperationRight(rightPart, operator);
    }

    public Operator getOperator() {
        return operator;
    }

    public Expression leftPart() {
        return leftPart;
    }

    public void leftPart(Expression leftPart) {
        setLeftPart(leftPart);
    }

    public Expression rightPart() {
        return rightPart;
    }

    public void rightPart(Expression rightPart) {
        setRightPart(rightPart);
    }

    public Operator operator() {
        return operator;
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

    private static EnumSet<Operator> leftPartNotIn = EnumSet.of(
            Operator.EQUAL,
            Operator.PLUS2,
            Operator.MINUS2,
            Operator.PLUS_EQUAL,
            Operator.MINUS_EQUAL,
            Operator.DIVIDE_EQUAL,
            Operator.MULTIPLY_EQUAL
    );

    private static EnumSet<Operator> rightPartNotIn = EnumSet.of(
            Operator.TERNARY,
            Operator.ELVIS,
            Operator.AND2,
            Operator.OR2,
            Operator.EQUAL,
            Operator.PLUS2,
            Operator.PLUS_EQUAL,
            Operator.MINUS_EQUAL,
            Operator.DIVIDE_EQUAL,
            Operator.MULTIPLY_EQUAL
    );

    public Object get(Map<String, Object> model) {
        if (hardcoded)
            return result;
        //System.out.println("execute: " + this);
        Object ob1 = null;
        Object ob2 = null;
        if (leftPart != null && !leftPartNotIn.contains(operator)) {
            ob1 = leftPart.get(model);
        }

        if (rightPart != null && !rightPartNotIn.contains(operator)) {
            ob2 = rightPart.get(model);
        }
        Object result = null;
        //System.out.println(model);
        //System.out.println(ob1 + "\t" + operator + "\t" + ob2);
        switch (operator) {
            case PLUS: {
                result = plus(ob1, ob2, operator);
                break;
            }
            case MINUS: {
                result = minus(ob1, ob2);
                break;
            }
            case MULTIPLY: {
                result = multiply(ob1, ob2);
                break;
            }
            case DIVIDE: {
                result = divide(ob1, ob2);
                break;
            }
            case PLUS_EQUAL:
            case MINUS_EQUAL:
            case MULTIPLY_EQUAL:
            case DIVIDE_EQUAL:
            case EQUAL:
            case PLUS2:
            case MINUS2: {
                result = set(leftPart, rightPart, model, operator);
                break;
            }
            case NOT: {
                result = !(Boolean) ob2;
                break;
            }
            case GREATE: {
                result = gt(ob1, ob2);
                break;
            }
            case LOWER: {
                result = lt(ob1, ob2);
                break;
            }
            case GREATE_EQUAL: {
                result = gte(ob1, ob2);
                break;
            }
            case LOWER_EQUAL: {
                result = lte(ob1, ob2);
                break;
            }
            case EQUAL2: {
                result = e(ob1, ob2);
                break;
            }
            case NOT_EQUAL: {
                result = ne(ob1, ob2);
                break;
            }
            case TERNARY: {
                //System.out.println("left: " + leftPart);
                //System.out.println("right: " + rightPart);
                if ((Boolean) ob1) {
                    result = ((Operation) rightPart).leftPart.get(model);
                } else {
                    result = ((Operation) rightPart).rightPart.get(model);
                }
                break;
            }
            case OR2: {
                if ((Boolean) ob1) {
                    result = true;
                } else {
                    result = rightPart().get(model);
                }
                break;
            }
            case OR: {
                if ((Boolean) ob1) {
//                    rightPart().get(model); already done
                    result = true;
                } else {
                    result = ob2;
                }
                break;
            }
            case AND2: {
                if (!(Boolean) ob1) {
                    result = false;
                } else {
                    result = rightPart().get(model);
                }
                break;
            }
            case AND: {
                if (!(Boolean) ob1) {
//                    rightPart().get(model); already done
                    result = false;
                } else {
                    result = ob2;
                }
                break;
            }
            case APPEND: {
                result = append(ob1, ob2);
                break;
            }
            case GET: {
                result = get(ob1, ob2);
                break;
            }
            case RANGE: {
                result = createRange(ob1, ob2);
                break;
            }
            case ELVIS: {
                if (AsBooleanExpression.toBoolean(ob1)) {
                    result = ob1;
                } else
                    result = rightPart.get(model);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented:" + this.operator);
        }
        hardcoded = (leftPart == null || leftPart.hardcoded) && (rightPart == null || rightPart.hardcoded);
        if (hardcoded) {
            this.result = result;
            return result;
        }
        simplify();

        return result;
    }

    private void simplify() {
        if (!checkedForSimplify) {
            synchronized (this) {
                if (!checkedForSimplify) {
                    if (leftPart != null && leftPart.hardcoded && leftPart.result instanceof Number) {
                        if (checkForSimplify(operator, rightPart))
                            switchHardcodedParts(this, (Operation) rightPart);

                    } else if (rightPart != null && rightPart.hardcoded && rightPart.result instanceof Number) {
                        if (checkForSimplify(operator, leftPart))
                            switchHardcodedParts(this, (Operation) leftPart);
                    }
                    checkedForSimplify = true;
                }
            }
        }
    }

    private void switchHardcodedParts(Operation from, Operation to) {
        if (from.rightPart != null && from.rightPart.hardcoded) {
            if (!to.leftPart.hardcoded) {
                Expression temp = to.leftPart;
                to.leftPart = from.rightPart;
                from.rightPart = temp;
                if (from.operator != to.operator && (from.operator == Operator.MINUS || from.operator == Operator.DIVIDE)) {
                    switchParts(to);
                    switchOperators(from, to);
                }
            } else {
                Expression temp = to.rightPart;
                to.rightPart = from.rightPart;
                from.rightPart = temp;
                switchOperators(from, to);
            }
        }

//        else if (from.leftPart != null && from.leftPart.hardcoded) {
//            if (!to.leftPart.hardcoded) {
//                Expression temp = to.leftPart;
//                to.leftPart = from.leftPart;
//                from.leftPart = temp;
//            } else {
//                Expression temp = to.rightPart;
//                to.rightPart = from.leftPart;
//                from.leftPart = temp;
//            }
//        }

    }

    private void switchParts(Operation op) {
        Expression temp = op.leftPart;
        op.leftPart = op.rightPart;
        op.rightPart = temp;
    }

    private void switchOperators(Operation from, Operation to) {
        if (from.operator != to.operator) {
            Operator temp = from.operator;
            from.operator = to.operator;
            to.operator = temp;
        }
    }

    private boolean checkForSimplify(Operator operator, Expression anotherPart) {
        if (operator != Operator.PLUS && operator != Operator.MINUS
                && operator != Operator.MULTIPLY && operator != Operator.DIVIDE)
            return false;

        if (anotherPart == null || !(anotherPart instanceof Operation))
            return false;

        Operation op = (Operation) anotherPart;
        if (op.operator != Operator.PLUS && op.operator != Operator.MINUS
                && op.operator != Operator.MULTIPLY && op.operator != Operator.DIVIDE)
            return false;

        if ((operator == Operator.PLUS || operator == Operator.MINUS) && (op.operator == Operator.MULTIPLY || op.operator == Operator.DIVIDE))
            return false;
        if ((operator == Operator.MULTIPLY || operator == Operator.DIVIDE) && (op.operator == Operator.PLUS || op.operator == Operator.MINUS))
            return false;

        if ((op.leftPart == null || !(op.leftPart.hardcoded && op.leftPart.result instanceof Number))
                && (op.rightPart == null || !(op.rightPart.hardcoded && op.rightPart.result instanceof Number)))
            return false;

        return true;
    }

    private static Range createRange(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Number && ob2 instanceof Number) {
            return new Range(((Number) ob1).intValue(), ((Number) ob2).intValue());
        }
        return null;
    }

    private static Object get(Object ob1, Object ob2) {
        if (ob1 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Map) {
            return ((Map) ob1).get(ob2);
        }
        if (ob1 instanceof List && ob2 instanceof Number) {
            List l = (List) ob1;
            int i = ((Number) ob2).intValue();
            if (i >= l.size())
                return null;
            else
                return (l).get(i);
        }
        if (ob1.getClass().getName().startsWith("[") && ob2 instanceof Number) {
            return Array.get(ob1, ((Number) ob2).intValue());
        }
        return null;
    }

    private static Object append(Object ob1, Object ob2) {
        if (ob1 == null) {
            throw new NullPointerException("can not append to null");
        }
        if (ob1 instanceof Collection) {
            ((Collection) ob1).add(ob2);
            return ob1;
        }
        if (ob1 instanceof StringBuilder) {
            ((StringBuilder) ob1).append(ob2);
            return ob1;
        }
        return null;
    }

    private static Object set(Expression leftPart, Expression rightPart, Map<String, Object> model) {
        return set(leftPart, rightPart, model, null);
    }

    private static Object set(Expression leftPart, Expression rightPart, Map<String, Object> model, Operator operator) {
        //left part not yet executed
        Object ob1 = null;
        Object ob2 = null;
        if (leftPart != null) {
            if (rightPart != null) {
                ob2 = rightPart.get(model);
            }

            Function function = null;
            if (leftPart instanceof Function)
                function = (Function) leftPart;

            if (function != null) {
                ob1 = function.getThatObject().get(model);
            }
            if (function != null && function.getFieldName() != null && ob1 instanceof Map) {
                if (operator != null) {
                    String key = function.getFieldName();
                    Map m = (Map) ob1;
                    return mapSetAndReturn(key, m, m.get(key), ob2, operator);
                }

                ((Map) ob1).put(function.getFieldName(), ob2);
                return ob2;
            }
            if (function != null && function.prepareField(ob1) != null) {
                if (operator != null) {
                    Field key = function.getField();
                    try {
                        return fieldSetAndReturn(ob1, key, key.get(ob1), ob2, operator);
                    } catch (IllegalAccessException e) {
                        throw new WrappedException(e);
                    }
                }

                try {
                    function.getField().set(ob1, ob2);
                } catch (IllegalAccessException e) {
                    throw new WrappedException(e);
                }
                return ob2;
            }

            Operation operation = null;
            if (leftPart instanceof Operation)
                operation = (Operation) leftPart;

            if (operation != null
                    && operation.operator() == Operator.GET) {
                ob1 = operation.leftPart().get(model);
                if (ob1 instanceof Map) {
                    if (operator != null) {
                        Object key = operation.rightPart().get(model);
                        Map m = (Map) ob1;
                        return mapSetAndReturn(key, m, m.get(key), ob2, operator);
                    }

                    ((Map) ob1).put(operation.rightPart().get(model), ob2);
                    return ob2;
                }
                int index = ((Number) operation.rightPart().get(model)).intValue();
                if (ob1 instanceof List) {
                    List l = (List) ob1;
                    while (index >= l.size()) {
                        l.add(null);
                    }
                    l.set(index, ob2);
                    return ob2;
                }
                if (ob1.getClass().getName().startsWith("[")) {
                    Array.set(ob1, index, ob2);
                    return ob2;
                }
            }
        }

        if (operator != null) {

            Function function = null;
            if (rightPart != null && rightPart instanceof Function)
                function = (Function) rightPart;

            if (function != null) {
                Object thatObject = function.getThatObject().get(model);
                if (function.getFieldName() != null && thatObject instanceof Map) {
                    String key = function.getFieldName();
                    Map m = (Map) thatObject;
                    return mapSetAndReturn(key, m, ob1, m.get(key), operator);
                }

                if (function.prepareField(thatObject) != null) {
                    Field key = function.getField();
                    try {
                        return fieldSetAndReturn(thatObject, key, null, key.get(thatObject), operator);
                    } catch (IllegalAccessException e) {
                        throw new WrappedException(e);
                    }
                }
            } else if (leftPart != null) {
                ob1 = leftPart.get(model);
            } else if (rightPart != null) {
                ob2 = rightPart.get(model);
            }

            Variable v = getVariable(leftPart, rightPart);
            if (v != null)
                return variableSetAndReturn(v, ob1, ob2, operator);

            return mapSetAndReturn(leftPart != null ? leftPart.raw() : rightPart.raw(), model, ob1, ob2, operator);
        }
        return null;
    }

    private static Variable getVariable(Expression leftPart, Expression rightPart) {
        Variable v = null;
        if (leftPart != null) {
            if (leftPart instanceof Holder)
                return ((Holder) leftPart).variable;
        } else if (rightPart != null && rightPart instanceof Holder)
            return ((Holder) rightPart).variable;
        return null;
    }

    private static Object variableSetAndReturn(Variable v, Object left, Object right, Operator operator) {
        switch (operator) {
            case PLUS2: {
                //post-increment
                if (left != null) {
                    Object r = increment(left);
                    v.set(r);
                    return left;
                }
                //pre-increment
                if (right != null) {
                    Object ob = increment(right);
                    v.set(ob);
                    return ob;
                }
            }
            case MINUS2: {
                //post-decrement
                if (left != null) {
                    Object r = decrement(left);
                    v.set(r);
                    return left;
                }
                //pre-decrement
                if (right != null) {
                    Object ob = decrement(right);
                    v.set(ob);
                    return ob;
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(left, right, operator);
                v.set(r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(left, right);
                v.set(r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(left, right);
                v.set(r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(left, right);
                v.set(r);
                return r;
            }
            case EQUAL: {
                v.set(right);
                return right;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented:" + operator);
    }

    private static Object mapSetAndReturn(Object key, Map model, Object left, Object right, Operator operator) {
        switch (operator) {
            case PLUS2: {
                //post-increment
                if (left != null) {
                    Object r = increment(left);
                    model.put(key, r);
                    return left;
                }
                //pre-increment
                if (right != null) {
                    Object ob = increment(right);
                    model.put(key, ob);
                    return ob;
                }
            }
            case MINUS2: {
                //post-decrement
                if (left != null) {
                    Object r = decrement(left);
                    model.put(key, r);
                    return left;
                }
                //pre-decrement
                if (right != null) {
                    Object ob = decrement(right);
                    model.put(key, ob);
                    return ob;
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(left, right, operator);
                model.put(key, r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(left, right);
                model.put(key, r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(left, right);
                model.put(key, r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(left, right);
                model.put(key, r);
                return r;
            }
            case EQUAL: {
                model.put(key, right);
                return right;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented:" + operator);
    }

    private static Object fieldSetAndReturn(Object thatObject, Field field, Object left, Object right, Operator operator) throws IllegalAccessException {
        switch (operator) {
            case PLUS2: {
                //pre-increment
                if (right != null) {
                    Object ob = increment(right);
                    field.set(thatObject, ob);
                    return ob;
                }
                //post-increment
                if (left != null) {
                    Object r = increment(left);
                    field.set(thatObject, r);
                    return left;
                }
            }
            case MINUS2: {
                //pre-decrement
                if (right != null) {
                    Object ob = decrement(right);
                    field.set(thatObject, ob);
                    return ob;
                }
                //post-decrement
                if (left != null) {
                    Object r = decrement(left);
                    field.set(thatObject, r);
                    return left;
                }
            }
            case PLUS_EQUAL: {
                Object r = plus(left, right, operator);
                field.set(thatObject, r);
                return r;
            }
            case MINUS_EQUAL: {
                Object r = minus(left, right);
                field.set(thatObject, r);
                return r;
            }
            case MULTIPLY_EQUAL: {
                Object r = multiply(left, right);
                field.set(thatObject, r);
                return r;
            }
            case DIVIDE_EQUAL: {
                Object r = divide(left, right);
                field.set(thatObject, r);
                return r;
            }
            case EQUAL: {
                field.set(thatObject, right);
                return right;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented:" + operator);
    }

    private static Object plus(Object ob1, Object ob2, Operator o) {
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
            if (ob1 instanceof Collection) {
                if (ob2 instanceof Collection) {
                    if (o == Operator.PLUS) {
                        Collection c = createNewCollection((Collection) ob1);
                        c.addAll((Collection) ob2);
                        return c;
                    } else
                        ((Collection) ob1).addAll((Collection) ob2);
                    return ob1;
                } else {
                    if (o == Operator.PLUS) {
                        Collection c = createNewCollection((Collection) ob1);
                        c.add(ob2);
                        return c;
                    } else
                        ((Collection) ob1).add(ob2);
                    return ob1;
                }
            }
            return String.valueOf(ob1) + String.valueOf(ob2);
        }
    }

    private static Collection createNewCollection(Collection c) {
        try {
            Collection l = c.getClass().newInstance();
            l.addAll(c);
            return l;
        } catch (InstantiationException e) {
            throw new WrappedException(e);
        } catch (IllegalAccessException e) {
            throw new WrappedException(e);
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

    private static boolean e(Object ob1, Object ob2) {
        if (ob1 == null || ob2 == null)
            return false;
        if (ob1 == ob2)
            return true;
        return ob1.equals(ob2);
    }

    private static Object ne(Object ob1, Object ob2) {
        return !e(ob1, ob2);
    }

    static Object multiply(Object ob1, Object ob2) {
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
        if (EvalTools.defaultEvaluatingStrategy == EvalTools.EvaluatingStrategy.DOUBLE) {
            return ((Number) ob1).doubleValue() / ((Number) ob2).doubleValue();
        }
        if (EvalTools.defaultEvaluatingStrategy == EvalTools.EvaluatingStrategy.FLOAT) {
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