package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;

import java.util.Iterator;

/**
 * Created by wizzardo on 16.04.16.
 */
public abstract class FlowStart<T> extends Flow<T, T> {
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

    public static <T> Flow<T, T> of(final Iterable<T> iterable) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Flow<T, ?> child = this.child;
                for (T t : iterable) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static <T> Flow<T, T> of(final Iterator<T> iterator) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Iterator<T> i = iterator;
                Flow<T, ?> child = this.child;
                while (!stop && i.hasNext()) {
                    child.process(i.next());
                }
            }
        };
    }

    public static <T> Flow<T, T> of(final T... array) {
        return new FlowStart<T>() {
            @Override
            protected void process() {
                Flow<T, ?> child = this.child;
                for (T t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Integer, Integer> of(final int[] array) {
        return new FlowStart<Integer>() {
            @Override
            protected void process() {
                Flow<Integer, ?> child = this.child;
                for (int t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Long, Long> of(final long[] array) {
        return new FlowStart<Long>() {
            @Override
            protected void process() {
                Flow<Long, ?> child = this.child;
                for (long t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Double, Double> of(final double[] array) {
        return new FlowStart<Double>() {
            @Override
            protected void process() {
                Flow<Double, ?> child = this.child;
                for (double t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Float, Float> of(final float[] array) {
        return new FlowStart<Float>() {
            @Override
            protected void process() {
                Flow<Float, ?> child = this.child;
                for (float t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Byte, Byte> of(final byte[] array) {
        return new FlowStart<Byte>() {
            @Override
            protected void process() {
                Flow<Byte, ?> child = this.child;
                for (byte t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Boolean, Boolean> of(final boolean[] array) {
        return new FlowStart<Boolean>() {
            @Override
            protected void process() {
                Flow<Boolean, ?> child = this.child;
                for (boolean t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Short, Short> of(final short[] array) {
        return new FlowStart<Short>() {
            @Override
            protected void process() {
                Flow<Short, ?> child = this.child;
                for (short t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }

    public static Flow<Character, Character> of(final char[] array) {
        return new FlowStart<Character>() {
            @Override
            protected void process() {
                Flow<Character, ?> child = this.child;
                for (char t : array) {
                    if (stop)
                        break;
                    child.process(t);
                }
            }
        };
    }
}
