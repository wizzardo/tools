package com.wizzardo.tools.evaluation;

import java.util.*;

public class EvaluationContext extends HashMap<String, Object> {
    protected final EvaluationContext parent;
    protected String file;
    protected int lineNumber = -1;
    protected int linePosition = 1;

    public EvaluationContext() {
        this.parent = null;
    }

    public EvaluationContext(EvaluationContext parent) {
        this.parent = parent;
    }

    public EvaluationContext(Map<String, Object> model) {
        super(model == null ? Collections.emptyMap() : model);
        this.parent = null;
    }

    @Override
    public int size() {
        return super.size() + (parent != null ? parent.size() : 0);
    }

    @Override
    public boolean isEmpty() {
        boolean empty = super.isEmpty();
        if (!empty)
            return false;
        return parent == null || parent.isEmpty();
    }

    @Override
    public Object get(Object key) {
        if (super.containsKey(key))
            return super.get(key);

        return parent != null ? parent.get(key) : null;
    }

    @Override
    public boolean containsKey(Object key) {
        if (super.containsKey(key))
            return true;

        return parent != null && parent.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        HashSet<String> keys = new HashSet<>(super.keySet());
        if (parent != null)
            keys.addAll(parent.keySet());
        return keys;
    }

    @Override
    public Object put(String key, Object value) {
        if (value == Expression.Definition.DEFINITION_MARK)
            return super.put(key, value);

        if (super.containsKey(key))
            return super.put(key, value);

        if (parent != null && parent.containsKey(key))
            return parent.put(key, value);

        return super.put(key, value);
    }

    public Object putLocal(String key, Object value) {
        return super.put(key, value);
    }

    @Override
    public Collection<Object> values() {
        return collectValues(new ArrayList<>(size()));
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return collectEntrySet(new HashSet<>(size(), 1));
    }

    @Override
    public EvaluationContext clone() {
        EvaluationContext context = new EvaluationContext(parent);
        for (Entry<String, Object> entry : super.entrySet()) {
            context.putLocal(entry.getKey(), entry.getValue());
        }
        return context;
    }

    protected Set<Entry<String, Object>> collectEntrySet(Set<Entry<String, Object>> into) {
        if (parent != null)
            parent.collectEntrySet(into);
        into.addAll(super.entrySet());
        return into;
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (parent != null)
            h += parent.hashCode() * 37;

        for (Entry<String, Object> entry : super.entrySet()) {
            if (entry.getValue() == this)
                continue;
            h += entry.hashCode();
        }
        return h;
    }

    protected Collection<Object> collectValues(Collection<Object> into) {
        if (parent != null)
            parent.collectValues(into);
        into.addAll(super.values());
        return into;
    }

    public EvaluationContext createLocalContext() {
        return new EvaluationContext(this);
    }

    public EvaluationContext getRoot() {
        return parent != null ? parent.getRoot() : this;
    }

    public String getFileName() {
        return getRoot().file;
    }

    public int getLineNumber() {
        return lineNumber == -1 ? (parent == null ? 1 : parent.getLineNumber()) : lineNumber;
    }

    public int getLinePosition() {
        return linePosition;
    }

    public boolean isPerfCountersEnabled() {
        return parent != null && getRoot().isPerfCountersEnabled();
    }

    public void setPerfCountersEnabled(boolean enabled) {
        if (parent != null)
            parent.setPerfCountersEnabled(enabled);
        else
            throw new IllegalStateException("Not implemented yet");
    }

    public void startPerfCounter() {
        if (parent != null)
            parent.startPerfCounter();
    }

    public void stopPerfCounter(String name) {
        if (parent != null)
            parent.stopPerfCounter(name);
    }
}
