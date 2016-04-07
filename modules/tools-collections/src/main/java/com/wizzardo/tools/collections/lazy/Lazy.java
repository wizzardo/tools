package com.wizzardo.tools.collections.lazy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 08.11.15.
 */
public class Lazy<A, B> extends AbstractLazy<A, B> {

    public static <T> Lazy<T, T> of(final Iterable<T> iterable) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : iterable) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static <T> Lazy<T, T> of(final Iterator<T> iterator) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                while (!stop && iterator.hasNext()) {
                    processToChild(iterator.next());
                }
            }
        };
    }

    public static <T> Lazy<T, T> of(final T... array) {
        return new StartLazy<T>() {
            @Override
            protected void process() {
                for (T t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Integer, Integer> of(final int[] array) {
        return new StartLazy<Integer>() {
            @Override
            protected void process() {
                for (int t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Long, Long> of(final long[] array) {
        return new StartLazy<Long>() {
            @Override
            protected void process() {
                for (long t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Double, Double> of(final double[] array) {
        return new StartLazy<Double>() {
            @Override
            protected void process() {
                for (double t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Float, Float> of(final float[] array) {
        return new StartLazy<Float>() {
            @Override
            protected void process() {
                for (float t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Byte, Byte> of(final byte[] array) {
        return new StartLazy<Byte>() {
            @Override
            protected void process() {
                for (byte t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Boolean, Boolean> of(final boolean[] array) {
        return new StartLazy<Boolean>() {
            @Override
            protected void process() {
                for (boolean t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Short, Short> of(final short[] array) {
        return new StartLazy<Short>() {
            @Override
            protected void process() {
                for (short t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    public static Lazy<Character, Character> of(final char[] array) {
        return new StartLazy<Character>() {
            @Override
            protected void process() {
                for (char t : array) {
                    processToChild(t);
                    if (stop)
                        break;
                }
            }
        };
    }

    private static abstract class StartLazy<T> extends Lazy<T, T> {
        boolean stop = false;

        @Override
        protected void start() {
            if (child == null)
                return;

            process();

            child.onEnd();
        }

        protected abstract void process();

        @Override
        protected void stop() {
            stop = true;
        }
    }

    public B reduce(Reducer<B> reducer) {
        return reduce(null, reducer);
    }

    public B reduce(B def, Reducer<B> reducer) {
        LazyReduce<B> reduce = then(new LazyReduce<B>(def, reducer));
        reduce.start();
        return reduce.get();
    }

    public <T> Lazy<B, T> merge(Mapper<B, Lazy<T, T>> mapper) {
        return then(new LazyMapMerge<B ,T>(mapper));
    }

    public Lazy<B, B> filter(Filter<? super B> filter) {
        return then(new LazyFilter<B>(filter));
    }

    public Lazy<B, B> each(Consumer<? super B> consumer) {
        return then(new LazyEach<B>(consumer));
    }

    public <T> Lazy<B, T> map(Mapper<? super B, T> mapper) {
        return then(new LazyMap<B, T>(mapper));
    }

    public <K> Map<K, List<B>> toMap(Mapper<B, K> toKey) {
        return toMap(Lazy.<K, LazyGroup<K, B, B>>hashMapSupplier(), toKey, new LazyGroupToListMapper<K, B>());
    }
}
