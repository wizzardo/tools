package com.wizzardo.tools.sql.query;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.evaluation.*;
import com.wizzardo.tools.io.FileTools;
import com.wizzardo.tools.misc.Pair;
import com.wizzardo.tools.reflection.FieldInfo;
import com.wizzardo.tools.reflection.Fields;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

public class Generator {

    final File root;
    final String packag;

    public Generator(String generatedFilesRoot, String packag) {
        root = new File(generatedFilesRoot);
        this.packag = packag;
        if (!root.exists()) {
            root.mkdirs();
        }
    }

    void createTableFor(Class cl) {
        Fields<FieldInfo> fieldInfos = new Fields<>(cl);

        List<FieldDescription> fields = (List<FieldDescription>) Flow.of(fieldInfos.iterator())
                .map(fieldInfo -> createFieldDescription(getFieldType(fieldInfo.field.getType()), fieldInfo.field.getType(), fieldInfo.field.getName()))
                .toList()
                .get();

        createTableFor(cl.getSimpleName(), fields);
    }

    void createTableFor(String name, List<FieldDescription> fields) {
        StringBuilder sb = new StringBuilder();
        String classTableName = name + "Table";
        String offset = "    ";
        sb.append("package ").append(packag).append(";\n");
        sb.append("import com.wizzardo.tools.sql.query.*;\n\n");
        sb.append("import java.util.Arrays;\n");
        sb.append("import java.util.List;\n");
        sb.append("import java.util.Collections;\n");

        fields.forEach(d -> sb.append(d.importString()));

        sb.append("public class ").append(classTableName).append(" extends Table {\n\n");
        sb.append(offset).append("private ").append(classTableName).append("(String name, String alias) {\n").append(offset).append(offset).append("super(name, alias);\n").append(offset).append("}\n\n");
        sb.append(offset).append("public ").append(classTableName).append(" as(String alias) {\n").append(offset).append(offset).append("return new ").append(classTableName).append("(name, alias);\n").append(offset).append("}\n\n");

        sb.append(offset).append("public final static ").append(classTableName).append(" INSTANCE")
                .append(" = new ").append(classTableName).append("(\"").append(toSqlName(name)).append("\", null);\n\n");

        fields.forEach(d -> {
            sb.append(offset).append("public final Field.").append(d.toString()).append(" ").append(toSqlName(d.name).toUpperCase())
                    .append(" = new Field.").append(d.toString()).append("(this, \"").append(toSqlName(d.name)).append("\")")
                    .append(";\n");
        });

        sb.append("\n");

        sb.append(offset).append("public final List<Field> FIELDS").append(" ").append(" = Collections.unmodifiableList(Arrays.asList(");
        fields.forEach(it -> sb.append(toSqlName(it.name).toUpperCase()).append(", "));
        sb.setLength(sb.length() - 2);
        sb.append("));\n");

        sb.append(offset).append("public List<Field> getFields() {\n");
        sb.append(offset).append(offset).append("return FIELDS;\n");
        sb.append(offset).append("}\n");


        sb.append("}");

        setTextIfChanged(classTableName, sb.toString());
    }

    void createTableFor(File file) {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));

        List<String> imports = new ArrayList<>();
        String javaClass = FileTools.text(file);
        String pack = EvalTools.readPackage(javaClass);
        javaClass = EvalTools.readImports(javaClass, imports);

        File srcRoot = file.getParentFile().getParentFile();
        int i = 0;
        while ((i = pack.indexOf('.', i + 1)) != -1) {
            srcRoot = srcRoot.getParentFile();
        }

        ScriptEngine.Binding model = new ScriptEngine.Binding(srcRoot, pack, new ArrayList<>(imports));
        Expression prepare = EvalTools.prepare(javaClass, model, new HashMap<>(), imports);

        List<Expression> definitions = ((ClassExpression) prepare).getDefinitions();


        List<FieldDescription> fields = (List<FieldDescription>) Flow.of(definitions)
                .map(expression -> {
                    if (expression instanceof Expression.Definition) {
                        String type = ((Expression.Definition) expression).type;
                        Class clazz = EvalTools.findClass(type, imports, model);
                        if (clazz != null)
                            return createFieldDescription(getFieldType(clazz), clazz, ((Expression.Definition) expression).name);
                        Pair<String, ClassExpression> resolve = model.resolve(type);
                        if (resolve != null && (clazz = getFieldType(resolve.value)) != null) {
                            return createFieldDescription(clazz, resolve.value, ((Expression.Definition) expression).name);
                        }
                    } else if (expression instanceof Expression.DefineAndSet) {
                        String type = ((Expression.DefineAndSet) expression).type;
                        Class clazz = EvalTools.findClass(type, imports, model);
                        if (clazz != null)
                            return createFieldDescription(getFieldType(clazz), clazz, ((Expression.DefineAndSet) expression).name);

                        Pair<String, ClassExpression> resolve = model.resolve(type);
                        if (resolve != null && (clazz = getFieldType(resolve.value)) != null) {
                            return createFieldDescription(clazz, resolve.value, ((Expression.Definition) expression).name);
                        }
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .toList()
                .get();


        createTableFor(name, fields);
    }

    static FieldDescription createFieldDescription(Class<Field> fieldClass, Class fieldType, String name) {
        if (fieldType.isEnum())
            return new EnumFieldDescription(fieldClass, name, fieldType.getCanonicalName(), fieldType.getSimpleName());
        else
            return new FieldDescription(fieldClass, name);
    }


    static FieldDescription createFieldDescription(Class<Field> fieldClass, ClassExpression ce, String name) {
        if (ce.isEnum())
            return new EnumFieldDescription(fieldClass, name, ce.getPackageName() + "." + ce.getName(), ce.getName());
        else
            return new FieldDescription(fieldClass, name);
    }

    protected void setTextIfChanged(String classTableName, String content) {
        File file = new File(root, classTableName + ".java");
        if (!file.exists() || !FileTools.text(file).equals(content))
            FileTools.text(file, content);
    }

    protected static String toSqlName(String name) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        return name.replaceAll(regex, replacement).toLowerCase();
    }

    static class FieldDescription {
        final Class<Field> fieldClass;
        final String name;

        FieldDescription(Class<Field> fieldClass, String name) {
            this.fieldClass = fieldClass;
            this.name = name;
        }

        public String importString() {
            return "";
        }

        @Override
        public String toString() {
            return fieldClass.getSimpleName();
        }
    }

    static class EnumFieldDescription<T> extends FieldDescription {
        final String canonicalName;
        final String simpleName;

        EnumFieldDescription(Class<Field> fieldClass, String name, String canonicalName, String simpleName) {
            super(fieldClass, name);
            this.canonicalName = canonicalName;
            this.simpleName = simpleName;
        }

        public String importString() {
            return "import " + canonicalName + ";\n";
        }

        @Override
        public String toString() {
            return fieldClass.getSimpleName() + "<" + simpleName + ">";
        }
    }

    protected Class getFieldType(ClassExpression ce) {
        if (ce.isEnum())
            return Field.EnumField.class;

        throw new IllegalArgumentException("Cannot find proper field-class for class " + ce);
    }

    protected Class getFieldType(Class cl) {
        if (cl.isArray()) {
            Class componentType = cl.getComponentType();
            if (!componentType.isPrimitive())
                throw new IllegalArgumentException("Only primitive arrays are supported for now");
            if (componentType == byte.class) {
                return Field.ByteArrayField.class;
            }

            throw new IllegalArgumentException("Only byte arrays are supported for now");
        }
        if (cl.isPrimitive()) {
            if (cl == long.class)
                return Field.LongField.class;
            if (cl == int.class)
                return Field.IntField.class;
            if (cl == short.class)
                return Field.ShortField.class;
            if (cl == byte.class)
                return Field.ByteField.class;
            if (cl == float.class)
                return Field.FloatField.class;
            if (cl == double.class)
                return Field.DoubleField.class;
            if (cl == boolean.class)
                return Field.BooleanField.class;
        } else if (cl == String.class)
            return Field.StringField.class;
        else if (cl == Long.class)
            return Field.LongField.class;
        if (cl == Integer.class)
            return Field.IntField.class;
        if (cl == Short.class)
            return Field.ShortField.class;
        if (cl == Byte.class)
            return Field.ByteField.class;
        if (cl == Float.class)
            return Field.FloatField.class;
        if (cl == Double.class)
            return Field.DoubleField.class;
        if (cl == Boolean.class)
            return Field.BooleanField.class;
        if (cl.isEnum())
            return Field.EnumField.class;
        else if (cl == Date.class)
            return Field.DateField.class;
        else if (cl == Timestamp.class)
            return Field.TimestampField.class;

        throw new IllegalArgumentException("Cannot find proper field-class for class " + cl);
    }

    public void createTables(Class... classes) {
        StringBuilder sb = new StringBuilder();
        String classTableName = "Tables";
        String offset = "    ";
        sb.append("package ").append(packag).append(";\n");
        sb.append("import com.wizzardo.tools.sql.query.*;\n\n");
        sb.append("public class ").append(classTableName).append(" {\n\n");

        Arrays.asList(classes).forEach(cl -> {
            sb.append(offset).append("public final static ").append(cl.getSimpleName()).append("Table ").append(toSqlName(cl.getSimpleName()).toUpperCase()).append(" = ").append(cl.getSimpleName()).append("Table.INSTANCE").append(";\n");
        });
        sb.append("}");

        setTextIfChanged(classTableName, sb.toString());

        for (Class aClass : classes) {
            createTableFor(aClass);
        }
    }

    public void createTables(File... files) {
        StringBuilder sb = new StringBuilder();
        String classTableName = "Tables";
        String offset = "    ";
        sb.append("package ").append(packag).append(";\n");
        sb.append("import com.wizzardo.tools.sql.query.*;\n\n");
        sb.append("public class ").append(classTableName).append(" {\n\n");

        Arrays.asList(files).forEach(file -> {
            String name = file.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            sb.append(offset).append("public final static ").append(name).append("Table ").append(toSqlName(name).toUpperCase()).append(" = ").append(name).append("Table.INSTANCE").append(";\n");
        });
        sb.append("}");

        setTextIfChanged(classTableName, sb.toString());

        for (File aClass : files) {
            createTableFor(aClass);
        }
    }
}
