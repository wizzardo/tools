package com.wizzardo.tools.reflection;

import java.util.List;

/**
 * Created by wizzardo on 14/03/17.
 */
public class GenericMethod {
    public final String name;
    public final Generic returnType;
    public final List<Generic> args;

    public GenericMethod(String name, Generic returnType, List<Generic> args) {
        this.name = name;
        this.returnType = returnType;
        this.args = args;
    }
}
