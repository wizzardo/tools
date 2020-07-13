package com.wizzardo.tools.json;

import com.wizzardo.tools.misc.CharTree;
import com.wizzardo.tools.misc.Pair;

/**
 * @author: wizzardo
 * Date: 2/6/14
 */
interface JsonBinder {
    void add(Object value);

    void add(JsonItem value);

    Object getObject();

    JsonBinder getObjectBinder();

    JsonBinder getArrayBinder();

    void setTemporaryKey(String key);

    void setTemporaryKey(Pair<String, JsonFieldInfo> pair);

    JsonFieldSetter getFieldSetter();

    CharTree.CharTreeNode<Pair<String, JsonFieldInfo>> getFieldsTree();

    void reset();
}
