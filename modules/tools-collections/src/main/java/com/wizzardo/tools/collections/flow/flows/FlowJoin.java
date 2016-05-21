package com.wizzardo.tools.collections.flow.flows;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowJoin<A> extends FlowProcessOnEnd<A, String> {
    private final StringBuilder sb;
    private final String separator;

    public FlowJoin(StringBuilder sb, String separator) {
        this.sb = sb;
        this.separator = separator;
    }

    @Override
    public void process(A a) {
        StringBuilder sb = this.sb;
        if (sb.length() > 0)
            sb.append(separator);

        sb.append(a);
    }

    @Override
    public String get() {
        start();
        return sb.toString();
    }
}
