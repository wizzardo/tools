package com.wizzardo.tools.yaml;

class YamlObjectBinder implements YamlBinder {
    private YamlObject json;
    private String tempKey;

    public YamlObjectBinder() {
        this.json = new YamlObject();
    }

    public void add(Object value) {
        add(new YamlItem(value));
    }

    public void add(YamlItem value) {
        json.put(tempKey, value);
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
    public void setTemporaryKey(String value) {
        tempKey = value;
    }
}
