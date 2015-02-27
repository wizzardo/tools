package com.wizzardo.tools.misc;

/**
 * @author: wizzardo
 * Date: 08.12.14
 */
public class UncheckedThrow {

    @SuppressWarnings("unchecked")
    private static <T extends Exception> void throwsUnchecked(Exception toThrow) throws T {
        throw (T) toThrow;
    }

    public static RuntimeException rethrow(final Exception ex) {
        UncheckedThrow.<RuntimeException>throwsUnchecked(ex);

        return new IllegalStateException("unreachable statement");
    }
}
