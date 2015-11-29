package com.wizzardo.tools.collections.lazy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by wizzardo on 08.11.15.
 */
class Command<A, B> {
    protected Command<?, A> parent;
    protected Command<B, ?> child;

    protected void process(A a) {
    }

    protected <T extends Command<B, C>, C> T then(T command) {
        command.parent = this;
        this.child = command;
        return command;
    }

    protected void start() {
        parent.start();
    }

    protected void end() {
        child.end();
    }

    protected void stop() {
        parent.stop();
    }

    protected B get() {
        return null;
    }

    static class FinishCommand<A, B> extends Command<A, B> {

        @Override
        protected void end() {
            if (child != null)
                child.end();
        }
    }

    static class LazyFilter<T> extends Lazy<T, T> {
        private Filter<T> filter;

        public LazyFilter(Filter<T> filter) {
            this.filter = filter;
        }

        @Override
        protected void process(T t) {
            if (filter.allow(t))
                child.process(t);
        }
    }

    static class LazyEach<T> extends Lazy<T, T> {
        private Consumer<T> consumer;

        public LazyEach(Consumer<T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(t);
            child.process(t);
        }
    }

    static class LazyMap<A, B> extends Lazy<A, B> {
        private Mapper<A, B> mapper;

        public LazyMap(Mapper<A, B> mapper) {
            this.mapper = mapper;
        }

        @Override
        protected void process(A a) {
            child.process(mapper.map(a));
        }
    }

    static class LazyReduce<T> extends Lazy<T, T> {
        private final Reducer<T> reducer;
        private T prev;

        public LazyReduce(Reducer<T> reducer) {
            this.reducer = reducer;
        }

        @Override
        protected void process(T t) {
            if (prev == null)
                prev = t;
            else
                prev = reducer.reduce(prev, t);
        }

        @Override
        protected void end() {
            child.process(prev);
            super.end();
        }
    }

    static class LazyCount<A> extends FinishCommand<A, Integer> {
        private int count = 0;

        @Override
        protected void process(A a) {
            count++;
        }

        @Override
        public Integer get() {
            return count;
        }

        public int getCount() {
            return count;
        }
    }

    static class LazyCollectList<A> extends FinishCommand<A, List<A>> {
        private List<A> list;

        LazyCollectList(int initialSize) {
            list = new ArrayList<A>(initialSize);
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

    static class LazyFirst<A> extends FinishCommand<A, A> {
        private A first;

        public LazyFirst(A def) {
            first = def;
        }

        @Override
        protected void process(A a) {
            first = a;
            parent.stop();
        }

        @Override
        public A get() {
            return first;
        }
    }

    static class LazyLast<A> extends FinishCommand<A, A> {
        private A last;

        public LazyLast(A def) {
            last = def;
        }

        @Override
        protected void process(A a) {
            last = a;
        }

        @Override
        public A get() {
            return last;
        }
    }

    static class LazyMin<A> extends FinishCommand<A, A> {
        private A min;

        public LazyMin(A def) {
            min = def;
        }

        @Override
        protected void process(A a) {
            if (min == null || ((Comparable<A>) min).compareTo(a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class LazyMax<A> extends FinishCommand<A, A> {
        private A max;

        public LazyMax(A def) {
            max = def;
        }

        @Override
        protected void process(A a) {
            if (max == null || ((Comparable<A>) max).compareTo(a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

    static class LazyMinWithComparator<A> extends FinishCommand<A, A> {
        private A min;
        private Comparator<A> comparator;

        LazyMinWithComparator(A def, Comparator<A> comparator) {
            this.comparator = comparator;
            min = def;
        }

        @Override
        protected void process(A a) {
            if (min == null || comparator.compare(min, a) > 0)
                min = a;
        }

        @Override
        public A get() {
            return min;
        }
    }

    static class LazyMaxWithComparator<A> extends FinishCommand<A, A> {
        private A max;
        private Comparator<A> comparator;

        LazyMaxWithComparator(A def, Comparator<A> comparator) {
            this.comparator = comparator;
            max = def;
        }

        @Override
        protected void process(A a) {
            if (max == null || comparator.compare(max, a) < 0)
                max = a;
        }

        @Override
        public A get() {
            return max;
        }
    }

}
