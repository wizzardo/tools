package com.wizzardo.tools.evaluation;

import java.util.ArrayList;
import java.util.List;

public class CallStack {
    private static final ThreadLocal<CallStack> threadLocal = ThreadLocal.withInitial(CallStack::new);
    protected List<Expression> stack = new ArrayList<>();

    public static CallStack current() {
        return threadLocal.get();
    }

    public void append(Expression expression) {
        stack.add(expression);
    }

    public void removeLast() {
        stack.remove(stack.size() - 1);
    }

    public RuntimeException throwException(Exception e) {
        if (e instanceof EvaluationException)
            return (RuntimeException) e;

        EvaluationException exception = new EvaluationException(e);

        ArrayList<StackTraceElement> elements = new ArrayList<>();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Expression expression = stack.get(i);
            String methodName = expression.toString();
            if (methodName == null)
                continue;

            StackTraceElement prev = elements.isEmpty() ? null : elements.get(elements.size() - 1);
            String file = expression.getFileName() == null ? "" : expression.getFileName();
            if (prev == null
                    || !file.equals(prev.getFileName())
                    || prev.getLineNumber() != expression.getLineNumber()
            ) {

                elements.add(new StackTraceElement(file, methodName, file, expression.getLineNumber()));
            }
        }

        exception.setStackTrace(elements.toArray(new StackTraceElement[elements.size()]));

        return exception;
    }

    public static class EvaluationException extends RuntimeException {
        public EvaluationException(Exception e) {
            super(e);
        }
    }
}
