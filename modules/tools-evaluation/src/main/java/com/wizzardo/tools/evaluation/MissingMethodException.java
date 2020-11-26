package com.wizzardo.tools.evaluation;

import java.util.Arrays;

/**
 * @author: moxa
 * Date: 10/28/13
 */
public class MissingMethodException extends RuntimeException {
    public MissingMethodException(Class clazz, String method, Object[] args) {
        super("No signature of method: " + clazz.getCanonicalName() + "." + method + " is applicable for argument types: " + getArgsClasses(args) + " values: " + Arrays.toString(args));
    }

    private static String getArgsClasses(Object[] args) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(args[i].getClass().getCanonicalName());
        }
        sb.append(")");
        return sb.toString();
    }
}
