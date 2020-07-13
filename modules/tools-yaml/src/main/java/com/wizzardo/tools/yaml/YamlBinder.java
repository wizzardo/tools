package com.wizzardo.tools.yaml;

interface YamlBinder {
    void add(Object value);

    void add(YamlItem value);

    Object getObject();

    YamlBinder getObjectBinder();

    YamlBinder getArrayBinder();

    void setTemporaryKey(String key);
}