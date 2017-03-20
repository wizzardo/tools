package com.wizzardo.tools.reflection;

import java.lang.reflect.Method;
import java.util.List;

import static com.wizzardo.tools.reflection.Misc.join;

/**
 * Created by wizzardo on 14/03/17.
 */
public class GenericMethod {
    public final Method method;
    public final Generic returnType;
    public final List<Generic> args;

    public GenericMethod(Method method, Generic returnType, List<Generic> args) {
        this.method = method;
        this.returnType = returnType;
        this.args = args;
    }

    @Override
    public String toString() {
        return join(new StringBuilder(32)
                        .append(returnType)
                        .append(' ')
                        .append(method.getName())
                        .append('(')
                , args, ",")
                .append(')')
                .toString();
    }
}
