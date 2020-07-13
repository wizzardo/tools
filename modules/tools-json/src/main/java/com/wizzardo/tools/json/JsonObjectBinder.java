package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
class JsonObjectBinder implements JsonBinder {
    private JsonObject json;
    private String tempKey;

    public JsonObjectBinder() {
        reset();
    }

    public void add(Object value) {
        add(new JsonItem(value));
    }

    public void add(JsonItem value) {
        json.put(tempKey, value);
    }

    @Override
    public Object getObject() {
        return json;
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
    public void setTemporaryKey(String value) {
        tempKey = value;
    }

    @Override
    public void setTemporaryKey(Pair<String, JsonFieldInfo> pair) {
        tempKey = pair.key;
    }

    @Override
    public JsonFieldSetter getFieldSetter() {
        return null;
    }

    @Override
    public CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> getFieldsTree() {
        return Binder.fieldsNames.getRoot();
    }

    @Override
    public void reset() {
        this.json = new JsonObject();
    }
}
