package com.wizzardo.tools.evaluation;

/**
 * @author: wizzardo
 * Date: 1/23/14
 */
public class Variable implements Function.Setter {
    private String name;
    private Object value;

    public Variable(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Object get() {
        return value;
    }

    public void set(Object o) {
        value = o;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }

    @Override
    public void set(Object instance, Object value) {
        set(value);
    }
}
