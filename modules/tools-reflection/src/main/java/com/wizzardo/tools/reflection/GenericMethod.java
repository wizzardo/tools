package com.wizzardo.tools.reflection;

/**
 * Created by wizzardo on 14/03/17.
 */
public class GenericMethod {
    public final String name;
    public final Generic returnType;
    protected final Generic[] args;

    public GenericMethod(String name, Generic returnType, Generic[] args) {
        this.name = name;
        this.returnType = returnType;
        this.args = args;
    }

    public int getArgumentsCount() {
        return args.length;
    }

    public Generic getArgument(int i) {
        return args[i];
    }
}
