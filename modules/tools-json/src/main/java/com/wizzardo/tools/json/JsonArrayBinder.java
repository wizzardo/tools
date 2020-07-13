package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JsonArrayBinder implements JsonBinder {
    private JsonArray json;

    public JsonArrayBinder() {
        reset();
    }

    public void add(Object value) {
        add(new JsonItem(value));
    }

    public void add(JsonItem value) {
        json.add(value);
    }

    @Override
    public Object getObject() {
        return json;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return null;
    }

    @Override
    public CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> getFieldsTree() {
        return null;
    }

    @Override
    public void reset() {
        this.json = new JsonArray();
    }

    @Override
    public JsonBinder getObjectBinder() {
        return new JsonObjectBinder();
    }

    @Override
    public JsonBinder getArrayBinder() {
        return new JsonArrayBinder();
    }

    @Override
    public void setTemporaryKey(String key) {
        throw new UnsupportedOperationException("JsonArray can not have any keys");
    }

    @Override
    public void setTemporaryKey(Pair<String, JsonFieldInfo> pair) {
        throw new UnsupportedOperationException("JsonArray can not have any keys");
    }
}
