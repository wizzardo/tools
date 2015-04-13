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
        run(runnable, null);
    }

    public static <T> T call(Callable<T> callable) {
        return call(callable, null);
    }

    public static void run(UncheckedRunnable runnable, Runnable finaly) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw rethrow(e);
        } finally {
            if (finaly != null)
                finaly.run();
        }
    }

    public static <T> T call(Callable<T> callable, Runnable finaly) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw rethrow(e);
        } finally {
            if (finaly != null)
                finaly.run();
        }
    }

    public interface UncheckedRunnable {
        void run() throws Exception;
    }
}
