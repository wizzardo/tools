package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Supplier;

import java.util.Iterator;

/**
 * Created by wizzardo on 16.04.16.
 */
public abstract class FlowStart<T> extends Flow<T> {
    boolean stop = false;

    @Override
    protected void start() {
        if (child == null)
            return;

        process();

        onEnd();
    }

    protected abstract void process();

    @Override
    protected void stop() {
        stop = true;
    }

    public static <T> Flow<T> of(final Iterable<T> iterable) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                FlowProcessor<T, ?> child = this.child;
                for (T t : iterable) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static <T> Flow<T> of(final Iterator<T> iterator) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Iterator<T> i = iterator;
                FlowProcessor<T, ?> child = this.child;
                while (!stop && i.hasNext()) {
                    child.process(i.next());
                }
            }
        };
    }

    public static <T> Flow<T> of(final T... array) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                FlowProcessor<T, ?> child = this.child;
                for (T t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static <T> Flow<T> of(final Supplier<T> supplier) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                child.process(supplier.supply());
            }
        };
    }

    public static <T> Flow<T> of(final Supplier<T>... suppliers) {
        return new FlowStart<T>() {
            @Override
            protected void process() {

                FlowProcessor<T, ?> child = this.child;
                for (Supplier<T> supplier : suppliers) {
                    if (stop)
                        break;
                    child.process(supplier.supply());
                }
            }
        };
    }

    public static Flow<Integer> of(final int[] array) {
        return new FlowStart<Integer>() {
            @Override
            protected void process() {
                FlowProcessor<Integer, ?> child = this.child;
                for (int t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Long> of(final long[] array) {
        return new FlowStart<Long>() {
            @Override
            protected void process() {
                FlowProcessor<Long, ?> child = this.child;
                for (long t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Double> of(final double[] array) {
        return new FlowStart<Double>() {
            @Override
            protected void process() {
                FlowProcessor<Double, ?> child = this.child;
                for (double t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Float> of(final float[] array) {
        return new FlowStart<Float>() {
            @Override
            protected void process() {
                FlowProcessor<Float, ?> child = this.child;
                for (float t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Byte> of(final byte[] array) {
        return new FlowStart<Byte>() {
            @Override
            protected void process() {
                FlowProcessor<Byte, ?> child = this.child;
                for (byte t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Boolean> of(final boolean[] array) {
        return new FlowStart<Boolean>() {
            @Override
            protected void process() {
                FlowProcessor<Boolean, ?> child = this.child;
                for (boolean t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Short> of(final short[] array) {
        return new FlowStart<Short>() {
            @Override
            protected void process() {
                FlowProcessor<Short, ?> child = this.child;
                for (short t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Character> of(final char[] array) {
        return new FlowStart<Character>() {
            @Override
            protected void process() {
                FlowProcessor<Character, ?> child = this.child;
                for (char t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }
}
