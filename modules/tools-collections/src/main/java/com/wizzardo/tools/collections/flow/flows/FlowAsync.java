package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.FlowProcessor;
import com.wizzardo.tools.collections.flow.Mapper;
import com.wizzardo.tools.misc.Unchecked;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowAsync<A, B> extends FlowProcessor<A, B> implements Runnable {

    protected final static int NATIVE_THREADS_COUNT = Runtime.getRuntime().availableProcessors();

    protected final ExecutorService service;
    protected final BlockingQueue<B> output;
    protected final BlockingQueue<A> input;
    protected final Mapper<A, Flow<B>> mapper;
    protected final AtomicInteger counter = new AtomicInteger();
    protected final int queueLimit;
    protected boolean waiting = false;

    public FlowAsync(Mapper<A, Flow<B>> mapper) {
        this(NATIVE_THREADS_COUNT, mapper);
    }

    public FlowAsync(int queueLimit, Mapper<A, Flow<B>> mapper) {
        this(Executors.newFixedThreadPool(NATIVE_THREADS_COUNT), queueLimit, mapper);
    }

    public FlowAsync(ExecutorService service, int queueLimit, final Mapper<A, Flow<B>> mapper) {
        this.service = service;
        this.mapper = mapper;
        this.queueLimit = queueLimit;
        input = new LinkedBlockingQueue<A>(queueLimit);
        output = new LinkedBlockingQueue<B>();
    }

    @Override
    public void process(final A a) {
        while (!canAdd()) {
            processOutput();
            waitForInput();
        }

        add(a);
        service.submit(this);


//        try {
//            input.put(a);
//        } catch (InterruptedException e) {
//            throw Unchecked.rethrow(e);
//        }
//        service.submit(task);
//        if (!output.isEmpty())
//            processOutput();
    }

    @Override
    protected void onEnd() {
        processOutput();
        while (!isEnded()) {
            waitForOutput();
            processOutput();
        }
        super.onEnd();
    }

    protected void processOutput() {
        BlockingQueue<B> queue = this.output;
        FlowProcessor<B, ?> child = FlowAsync.this.child;
        while (!queue.isEmpty()) {
            B b = queue.poll();
            if (child != null)
                child.process(b);
        }
//
//        if (ended)
//            if (monitor.isEnded())
//                super.onEnd();
//            else {
//                waitForOutput();
//                processOutput();
//            }

    }

    public boolean isEnded() {
        return counter.get() == 0;
    }

    protected void add(A a) {
        counter.incrementAndGet();
        input.add(a);
    }

    public boolean canAdd() {
        return input.size() < queueLimit;
    }

    protected void waitForInput() {
        if (input.size() < queueLimit)
            return;

        if (!output.isEmpty())
            return;

        synchronized (this) {
            do {
                if (input.size() < queueLimit)
                    return;

                if (!output.isEmpty())
                    return;

                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            } while (true);
        }
    }

    protected void waitForOutput() {
        if (output.isEmpty())
            synchronized (this) {
                waiting = true;
                while (output.isEmpty() && counter.get() > 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                waiting = false;
            }
    }

    protected A take() {
        try {
            return input.take();
        } catch (InterruptedException e) {
            throw Unchecked.rethrow(e);
        } finally {
            notifyIfWaiting();
        }
    }

    protected void endTask() {
        counter.decrementAndGet();
        notifyIfWaiting();
    }

    protected void notifyIfWaiting() {
        if (waiting)
            synchronized (this) {
                if (waiting)
                    this.notifyAll();
            }
    }

    public void doInAsyncTask() {
        try {
            Flow<B> flow = mapper.map(take());
            flow.then(new FlowProcessor<B, B>() {
                @Override
                public void process(B b) {
                    try {
                        output.put(b);
                        notifyIfWaiting();
                    } catch (InterruptedException e) {
                        throw Unchecked.rethrow(e);
                    }
                }

                @Override
                protected void onEnd() {
                    endTask();
                }
            }).execute();
        } catch (Exception ex) {
            endTask();
            throw Unchecked.rethrow(ex);
        }
    }

    @Override
    public void run() {
        doInAsyncTask();
    }
}
