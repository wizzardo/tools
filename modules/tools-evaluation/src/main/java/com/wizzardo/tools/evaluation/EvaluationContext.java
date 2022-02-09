package com.wizzardo.tools.evaluation;

import java.util.*;

public class EvaluationContext extends HashMap<String, Object> {
    protected final EvaluationContext parent;

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
    public Collection<Object> values() {
        return collectValues(new ArrayList<>(size()));
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return collectEntrySet(new HashSet<>(size(), 1));
    }

    protected Set<Entry<String, Object>> collectEntrySet(Set<Entry<String, Object>> into) {
        if (parent != null)
            parent.collectEntrySet(into);
        into.addAll(super.entrySet());
        return into;
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
}
