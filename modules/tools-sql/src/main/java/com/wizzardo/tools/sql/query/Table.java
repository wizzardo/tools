package com.wizzardo.tools.sql.query;

import java.util.List;

public abstract class Table {
    protected final String name;
    protected final String alias;

    public Table(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public Table(String name) {
        this(name, null);
    }

    public String getName() {
        return alias == null ? name : alias;
    }

    public abstract List<Field> getFields();
}
