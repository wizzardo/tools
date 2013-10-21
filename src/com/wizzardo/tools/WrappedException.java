package com.wizzardo.tools;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author: moxa
 * Date: 3/24/13
 */
public class WrappedException extends RuntimeException {
    private Throwable cause;

    public WrappedException(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return cause.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return cause.getLocalizedMessage();
    }

    @Override
    public Throwable getCause() {
        return cause.getCause();
    }

    @Override
    public String toString() {
        return cause.toString();
    }

    @Override
    public void printStackTrace() {
        cause.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        cause.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        cause.printStackTrace(s);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return cause.initCause(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return cause;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return cause.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        cause.setStackTrace(stackTrace);
    }
}
