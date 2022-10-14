package com.wizzardo.tools.sql.query;

import com.wizzardo.tools.json.JsonTools;
import com.wizzardo.tools.sql.DBTools;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

public class Field {

    protected final String sql;
    protected final Table table;
    protected final String name;

    public Field(Table table, String name) {
        this(table, name, table.getName() + "." + name);
    }

    public Field(String sql) {
        this(null, sql, sql);
    }

    private Field(Table table, String name, String sql) {
        this.sql = sql;
        this.table = table;
        this.name = name;
    }

    public Field as(String alias) {
        return new AliasField(this, alias);
    }

    public String getName() {
        return name;
    }

    public void toSql(QueryBuilder sb) {
        sb.append(sql);
    }

    protected ToSqlMapper toSqlMapper(Class c) {
        return FieldMappers.toSqlMapper(c, this);
    }

    public static Field of(QueryBuilder.FetchableStep step, String name) {
        return new FetchableField(step, name);
    }

    public static Field of(QueryBuilder.FetchableStep step) {
        return new FetchableField(step);
    }

    interface ToSqlMapper {
        void map(Object o, QueryBuilder builder);

        default String getCast(Connection c) {
            return null;
        }
    }

    public Condition.JoinCondition eq(Field another) {
        return new Condition.JoinCondition(this, another);
    }

    public Condition.IsNullCondition isNull() {
        return new Condition.IsNullCondition(this);
    }

    public Condition.IsNotNullCondition isNotNull() {
        return new Condition.IsNotNullCondition(this);
    }

    public static class LongField extends Field {
        public LongField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(long l) {
            return eq(Long.valueOf(l));
        }

        public Condition.FieldCondition eq(Long l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField((Long) o));
        }

        public Condition.InCondition in(QueryBuilder.FetchableStep subQuery) {
            return new Condition.InCondition(this, Condition.Operator.IN, subQuery);
        }

        public Condition.InCondition notIn(QueryBuilder.FetchableStep subQuery) {
            return new Condition.InCondition(this, Condition.Operator.NOT_IN, subQuery);
        }
    }

    public static class IntField extends Field {
        public IntField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(int i) {
            return eq(Integer.valueOf(i));
        }

        public Condition.FieldCondition eq(Integer i) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, i, (o, builder) -> builder.setField((Integer) o));
        }

        public Condition.InCondition in(QueryBuilder.FetchableStep subQuery) {
            return new Condition.InCondition(this, Condition.Operator.IN, subQuery);
        }

        public Condition.InCondition notIn(QueryBuilder.FetchableStep subQuery) {
            return new Condition.InCondition(this, Condition.Operator.NOT_IN, subQuery);
        }
    }

    public static class ShortField extends Field {
        public ShortField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(short s) {
            return eq(Short.valueOf(s));
        }

        public Condition.FieldCondition eq(Short s) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, s, (o, builder) -> builder.setField((Short) o));
        }
    }

    public static class ByteField extends Field {
        public ByteField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(byte b) {
            return eq(Byte.valueOf(b));
        }

        public Condition.FieldCondition eq(Byte b) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, b, (o, builder) -> builder.setField((Byte) o));
        }
    }

    public static class DoubleField extends Field {
        public DoubleField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(double d) {
            return eq(Double.valueOf(d));
        }

        public Condition.FieldCondition eq(Double d) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, d, (o, builder) -> builder.setField((Double) o));
        }
    }

    public static class FloatField extends Field {
        public FloatField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(float f) {
            return eq(Float.valueOf(f));
        }

        public Condition.FieldCondition eq(Float f) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, f, (o, builder) -> builder.setField((Float) o));
        }
    }

    public static class BooleanField extends Field {
        public BooleanField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(boolean b) {
            return eq(Boolean.valueOf(b));
        }

        public Condition.FieldCondition eq(Boolean b) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, b, (o, builder) -> builder.setField((Float) o));
        }
    }

    public static class StringField extends Field {
        public StringField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(String l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField((String) o));
        }
    }

    public static class JsonField extends Field {
       static final ToSqlMapper onlyFieldMapper = new ToSqlMapper() {
            @Override
            public void map(Object o, QueryBuilder builder) {
                builder.setField(JsonTools.serialize(o));
            }

            @Override
            public String getCast(Connection connection) {
                if (DBTools.isPostgreSQLDB(connection))
                    return "::json";
                return null;
            }
        };

        public JsonField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(Object l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, onlyFieldMapper);
        }
    }

    public static class EnumField<E extends Enum<E>> extends Field {
        public EnumField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(E l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField(o == null ? null : ((E) o).name()));
        }
    }

    public static class ByteArrayField extends Field {
        public ByteArrayField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(byte[] l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField((byte[]) o));
        }
    }


    public static class DateField extends Field {
        public DateField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(Date l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField((Date) o));
        }
    }

    public static class TimestampField extends Field {
        public TimestampField(Table table, String name) {
            super(table, name);
        }

        public Condition.FieldCondition eq(Timestamp l) {
            return new Condition.FieldCondition(this, Condition.Operator.EQ, l, (o, builder) -> builder.setField((Timestamp) o));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (!sql.equals(field.sql)) return false;
        if (table != null ? !table.equals(field.table) : field.table != null) return false;
        return name.equals(field.name);
    }

    @Override
    public int hashCode() {
        int result = sql.hashCode();
        result = 31 * result + (table != null ? table.hashCode() : 0);
        result = 31 * result + name.hashCode();
        return result;
    }

    private static class FetchableField extends Field {

        private final QueryBuilder.FetchableStep step;

        public FetchableField(QueryBuilder.FetchableStep step, String name) {
            super(name);
            this.step = step;
        }

        public FetchableField(QueryBuilder.FetchableStep step) {
            this(step, "");
        }

        @Override
        public void toSql(QueryBuilder sb) {
            sb.append("(");
            step.toSql(sb);
            sb.append(")");

            if (!name.isEmpty())
                sb.append(" as ").append(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            FetchableField that = (FetchableField) o;

            return step.equals(that.step);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + step.hashCode();
            return result;
        }
    }

    public static Field invoke(String function, Field... args) {
        return new FunctionField(function, args);
    }

    private static class FunctionField extends Field {
        private final String function;
        private final Field[] args;

        public FunctionField(String function, Field... args) {
            super(function);
            this.function = function;
            this.args = args;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            sb.append(function).append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0)
                    sb.append(", ");

                args[i].toSql(sb);
            }
            sb.append(")");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            FunctionField that = (FunctionField) o;

            if (!function.equals(that.function)) return false;
            return Arrays.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + function.hashCode();
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }
    }

    private static class AliasField extends Field {
        private final String alias;
        private final Field f;

        public AliasField(Field f, String alias) {
            super(alias);
            this.alias = alias;
            this.f = f;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            f.toSql(sb);
            sb.append(" as ").append(alias);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            AliasField that = (AliasField) o;

            if (!alias.equals(that.alias)) return false;
            return f.equals(that.f);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + alias.hashCode();
            result = 31 * result + f.hashCode();
            return result;
        }
    }
}
