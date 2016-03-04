package com.wizzardo.tools.collections.lazy;

import java.util.Collection;
import java.util.Comparator;

/**
 * Created by wizzardo on 08.11.15.
 */
class Command<A, B> {
    protected Command<?, A> parent;
    protected Command<B, ?> child;

    protected void process(A a) {
    }

    protected void processToChild(B b) {
        child.process(b);
    }

    protected <T extends Command<B, C>, C> T then(T command) {
        command.parent = this;
        this.child = command;
        return command;
    }

    protected void start() {
        parent.start();
    }

    protected void onEnd() {
        child.onEnd();
    }

    protected void stop() {
        parent.stop();
    }

    protected B get() {
        return null;
    }

    protected Command getLast(Command command) {
        Command last = command;
        while (last.child != null) {
            last = last.child;
        }
        return last;
    }

    static class FinishCommand<A, B> extends Command<A, B> {

        @Override
        protected void onEnd() {
            if (child != null)
                child.onEnd();
        }
    }

    static class LazyFilter<T> extends Lazy<T, T> {
        private Filter<? super T> filter;

        public LazyFilter(Filter<? super T> filter) {
            this.filter = filter;
        }

        @Override
        protected void process(T t) {
            if (filter.allow(t))
                child.process(t);
        }
    }

    static class LazyCollectWithAccumulator<C, T> extends FinishCommand<T, C> {
        private BiConsumer<? super C, ? super T> accumulator;
        private C collector;

        public LazyCollectWithAccumulator(C collector, BiConsumer<? super C, ? super T> accumulator) {
            this.accumulator = accumulator;
            this.collector = collector;
        }

        @Override
        protected void process(T t) {
            accumulator.consume(collector, t);
        }

        @Override
        protected C get() {
            return collector;
        }
    }

    static class LazyEach<T> extends Lazy<T, T> {
        private Consumer<? super T> consumer;

        public LazyEach(Consumer<? super T> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void process(T t) {
            consumer.consume(t);
            child.process(t);
        }
    }

    static class LazyIterate<A, B> extends Lazy<A, B> {
        private Iterater<? super A, B> iterater;
        private Consumer<B> consumer;

        public LazyIterate(Iterater<? super A, B> iterater) {
            this.iterater = iterater;
            consumer = new Consumer<B>() {
                @Override
                public void consume(B b) {
                    child.process(b);
                }
            };
        }

        @Override
        protected void process(A a) {
            iterater.iterate(a, consumer);
        }
    }

    static class LazyMerge<B extends Lazy<T, T>, T> extends Lazy<B, T> {
        Lazy<T, T> proxy = new Lazy<T, T>() {
            @Override
            protected void process(T t) {
                LazyMerge.this.child.process(t);
            }

            @Override
            protected void onEnd() {
            }
        };

        @Override
        protected void process(B b) {
            b.child = proxy;
        }
    }

    static class LazyMap<A, B> extends Lazy<A, B> {
        private Mapper<? super A, B> mapper;

        public LazyMap(Mapper<? super A, B> mapper) {
            this.mapper = mapper;
        }

        @Override
        protected void process(A a) {
            child.process(mapper.map(a));
        }
    }

    static class LazyReduce<T> extends FinishCommand<T, T> {
        private final Reducer<T> reducer;
        private T prev;

        public LazyReduce(T def, Reducer<T> reducer) {
            this.reducer = reducer;
            prev = def;
        }

        @Override
        protected void process(T t) {
            if (prev == null)
                prev = t;
            else
                prev = reducer.reduce(prev, t);
        }

        @Override
        protected T get() {
            return prev;
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

    static class LazyCollect<A> extends FinishCommand<A, Collection<A>> {
        private Collection<A> collection;

        LazyCollect(Collection<A> collection) {
            this.collection = collection;
        }

        @Override
        protected void process(A a) {
            collection.add(a);
        }

        @Override
        public Collection<A> get() {
            return collection;
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
            onEnd();
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
        private Comparator<? super A> comparator;

        LazyMinWithComparator(A def, Comparator<? super A> comparator) {
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
        private Comparator<? super A> comparator;

        LazyMaxWithComparator(A def, Comparator<? super A> comparator) {
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
