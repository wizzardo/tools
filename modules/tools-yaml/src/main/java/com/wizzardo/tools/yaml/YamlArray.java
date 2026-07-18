package com.wizzardo.tools.yaml;

import com.wizzardo.tools.interfaces.Mapper;
import com.wizzardo.tools.misc.Appender;
import com.wizzardo.tools.misc.ExceptionDrivenStringBuilder;

import java.util.ArrayList;
import java.util.Collection;

import static com.wizzardo.tools.yaml.YamlTools.appendNewLineAndIndent;

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

    public void toYaml(Appender sb) {
        toYaml(sb, 0);
    }

    void toYaml(Appender sb, int indent) {
        if (isEmpty()) {
            sb.append("[]");
            return;
        }

        boolean first = true;
        for (YamlItem item : this) {
            if (!first) {
                appendNewLineAndIndent(sb, indent);
            } else
                first = false;

            sb.append("- ");

            if (item == null)
                sb.append("null");
            else {
                if (item.isYamlObject() || item.isYamlArray()) {
                    appendNewLineAndIndent(sb, indent);
                    item.toYaml(sb, indent + 1);
                } else
                    item.toYaml(sb, indent + 1);
            }
        }
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