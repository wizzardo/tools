package com.wizzardo.tools.collections.lazy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wizzardo on 08.11.15.
 */
abstract class Command<A, B> {
    Command<?, A> parent;
    Command<B, ?> child;

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

    public boolean isBlocking() {
        return false;
    }


    static class CountCommand<A> extends Command<A, Integer> {
        int count = 0;

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
        List<A> list = new ArrayList<A>();

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

        @Override
        public boolean isBlocking() {
            return true;
        }
    }

    static class NoopCommand<A> extends Command<A, A> {

        NoopCommand(Command<A, A> parent) {
            super(parent);
        }

        NoopCommand() {
        }


        @Override
        protected void process(A a) {
            child.process(a);
        }

        @Override
        public A get() {
            return null;
        }

        @Override
        protected void start() {
        }
    }

    static class FirstCommand<A> extends Command<A, A> {
        A first;

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
