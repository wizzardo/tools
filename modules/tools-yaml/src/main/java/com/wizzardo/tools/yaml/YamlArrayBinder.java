package com.wizzardo.tools.yaml;

class YamlArrayBinder implements YamlBinder {
    private YamlArray json;

    public YamlArrayBinder() {
        this.json = new YamlArray();
    }

    public void add(Object value) {
        add(new YamlItem(value));
    }

    public void add(YamlItem value) {
        json.add(value);
    }

    @Override
    public Object getObject() {
        return json;
    }

    @Override
    public YamlBinder getObjectBinder() {
        return new YamlObjectBinder();
    }

    @Override
    public YamlBinder getArrayBinder() {
        return new YamlArrayBinder();
    }

    @Override
    public void setTemporaryKey(String key) {
        throw new UnsupportedOperationException("YamlArray can not have any keys");
    }

}
