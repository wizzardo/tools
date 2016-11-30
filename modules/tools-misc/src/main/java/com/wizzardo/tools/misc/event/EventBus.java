package com.wizzardo.tools.misc.event;

import com.wizzardo.tools.misc.Consumer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus<E> {

    protected ConcurrentMap<E, List<Listener<? super E, Object>>> listeners;
    protected Consumer<Exception> exceptionConsumer = new Consumer<Exception>() {
        @Override
        public void consume(Exception e) {
            e.printStackTrace();
        }
    };

    public EventBus(ConcurrentMap<E, List<Listener<? super E, Object>>> listeners) {
        this.listeners = listeners;
    }

    public EventBus() {
        this(new ConcurrentHashMap<E, List<Listener<? super E, Object>>>());
    }

    public <D> EventBus<E> on(E event, Listener<? super E, D> listener) {
        List<Listener<? super E, Object>> l = getListeners(event, true);
        l.add((Listener<? super E, Object>) listener);
        return this;
    }

    public <D> EventBus<E> off(E event, Listener<? super E, D> listener) {
        List<Listener<? super E, Object>> l = getListeners(event, false);
        if (l != null)
            l.remove(listener);
        return this;
    }

    public <D> EventBus<E> off(E event) {
        listeners.remove(event);
        return this;
    }

    public void trigger(E event, Object data) {
        List<Listener<? super E, Object>> list = getListeners(event, false);
        if (list == null)
            return;

        for (Listener<? super E, Object> listener : list) {
            try {
                listener.on(event, data);
            } catch (Exception e) {
                try {
                    onError(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    protected void onError(Exception e) {
        exceptionConsumer.consume(e);
    }

    protected void onError(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

    protected List<Listener<? super E, Object>> getListeners(E event, boolean create) {
        List<Listener<? super E, Object>> l = this.listeners.get(event);
        if (l == null && create) {
            List<Listener<? super E, Object>> old = this.listeners.putIfAbsent(event, l = createList());
            if (old != null)
                return old;
        }
        return l;
    }

    protected List<Listener<? super E, Object>> createList() {
        return new CopyOnWriteArrayList<Listener<? super E, Object>>();
    }
}
