package com.wizzardo.tools.misc;

import java.util.concurrent.Callable;

/**
 * @author: wizzardo
 * Date: 08.12.14
 */
public class Unchecked {

    @SuppressWarnings("unchecked")
    private static <T extends Exception> void throwsUnchecked(Exception toThrow) throws T {
        throw (T) toThrow;
    }

    public static RuntimeException rethrow(Exception ex) {
        Unchecked.<RuntimeException>throwsUnchecked(ex);

        return new IllegalStateException("unreachable statement");
    }

    public static void run(UncheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    public static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw rethrow(e);
        }
    }

    public interface UncheckedRunnable {
        void run() throws Exception;
    }
}
