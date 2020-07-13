package com.wizzardo.tools.yaml;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;

import java.util.ArrayList;
import java.util.Collection;

public class YamlArray extends ArrayList<YamlItem> {

    public String toString() {
        return ExceptionDrivenStringBuilder.withBuilder(new Mapper<ExceptionDrivenStringBuilder, String>() {
            @Override
            public String map(ExceptionDrivenStringBuilder builder) {
                Appender sb = Appender.create(builder);
                toYaml(sb);
                return sb.toString();
            }
        });
    }

    void toYaml(Appender sb) {
        sb.append('[');
        boolean comma = false;
        for (YamlItem item : this) {
            if (comma)
                sb.append(',');
            else
                comma = true;

            if (item == null)
                sb.append("null");
            else
                item.toYaml(sb);
        }
        sb.append(']');
    }

    public YamlArray append(Object ob) {
        if (ob instanceof YamlItem) {
            add((YamlItem) ob);
        } else
            add(new YamlItem(ob));
        return this;
    }

    public YamlArray appendAll(Collection l) {
        if (l == null)
            append(new YamlItem(null));
        else
            for (Object ob : l) {
                append(ob);
            }
        return this;
    }
}