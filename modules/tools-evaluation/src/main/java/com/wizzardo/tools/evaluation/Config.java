package com.wizzardo.tools.evaluation;

import com.wizzardo.tools.collections.CollectionTools;

import java.util.HashMap;

/**
 * Created by wizzardo on 22.12.15.
 */
public class Config extends HashMap<String, Object> implements CollectionTools.Closure<Object, ClosureExpression> {
    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (value == null) {
            put((String) key, value = new Config());
        }
        return value;
    }

    @Override
    public Object execute(ClosureExpression it) {
        it.get(this);
        return this;
    }
}
