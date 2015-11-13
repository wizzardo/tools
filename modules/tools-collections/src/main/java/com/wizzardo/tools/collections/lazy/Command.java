package com.wizzardo.tools.collections.lazy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wizzardo on 08.11.15.
 */
abstract class Command<A, B> {
    protected Command<?, A> parent;
    protected Command<B, ?> child;

    Command(Command<?, A> parent) {
        this.parent = parent;
        parent.child = this;
    }

    Command() {
    }

    protected void process(A a) {
    }

    protected void start() {
        parent.start();
    }

    protected void end() {
        if (child != null)
            child.end();
    }

    public B get() {
        return null;
    }

    static class FilterCommand<A> extends Command<A, A> {
        private Lazy.Filter<A> filter;

        public FilterCommand(Command<?, A> parent, Lazy.Filter<A> filter) {
            super(parent);
            this.filter = filter;
        }

        @Override
        protected void process(A a) {
            if (filter.allow(a))
                child.process(a);
        }
    }

    static class CountCommand<A> extends Command<A, Integer> {
        private int count = 0;

        CountCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            count++;
        }

        @Override
        public Integer get() {
            return count;
        }
    }

    static class CollectListCommand<A> extends Command<A, List<A>> {
        private List<A> list = new ArrayList<A>();

        CollectListCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            list.add(a);
        }

        @Override
        public List<A> get() {
            return list;
        }
    }

    static class FirstCommand<A> extends Command<A, A> {
        private A first;

        FirstCommand(Command<?, A> parent) {
            super(parent);
        }

        @Override
        protected void process(A a) {
            if (first == null)
                first = a;
        }

        @Override
        public A get() {
            return first;
        }
    }

}
