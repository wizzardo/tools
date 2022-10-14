package com.wizzardo.tools.sql.query;

import com.wizzardo.tools.cache.Cache;
import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.sql.DBTools;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QueryBuilder {

    private static class SqlConnectionKey {
        final String sql;
        final Connection connection;
        final int hash;

        private SqlConnectionKey(String sql, Connection connection) {
            this.sql = sql;
            this.connection = connection;
            hash = 31 * sql.hashCode() + connection.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SqlConnectionKey)) return false;

            SqlConnectionKey that = (SqlConnectionKey) o;

            if (hash != that.hash) return false;
            if (!sql.equals(that.sql)) return false;
            return connection == that.connection;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static Cache<SqlConnectionKey, PreparedStatement> preparedStatements = new Cache<>(60);

    protected StringBuilder sb = new StringBuilder(128);
    protected PreparedStatement statement;
    protected int fieldCounter = 1;

    public QueryBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public QueryBuilder setLength(int length) {
        sb.setLength(length);
        return this;
    }

    public int length() {
        return sb.length();
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public static WrapConnectionStep withConnection(Connection connection) {
        return new WrapConnectionStep(connection);
    }

    public void setField(long value) {
        try {
            statement.setLong(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(int value) {
        try {
            statement.setInt(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(short value) {
        try {
            statement.setShort(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(byte value) {
        try {
            statement.setByte(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(double value) {
        try {
            statement.setDouble(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(float value) {
        try {
            statement.setFloat(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(boolean value) {
        try {
            statement.setBoolean(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(String value) {
        try {
            if (value == null)
                setNullField(Types.VARCHAR);
            else
                statement.setString(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(java.util.Date value) {
        try {
            if (value == null)
                setNullField(Types.DATE);
            else if (value instanceof java.sql.Date)
                statement.setDate(fieldCounter++, (java.sql.Date) value);
            else
                statement.setDate(fieldCounter++, new java.sql.Date(value.getTime()));
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(Timestamp value) {
        try {
            if (value == null)
                setNullField(Types.DATE);
            else
                statement.setTimestamp(fieldCounter++, value);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setNullField(int type) {
        try {
            statement.setNull(fieldCounter++, type);
        } catch (SQLException e) {
            throw Unchecked.rethrow(e);
        }
    }

    public void setField(Long value) {
        if (value == null)
            setNullField(Types.BIGINT);
        else
            setField(value.longValue());
    }

    public void setField(Integer value) {
        if (value == null)
            setNullField(Types.INTEGER);
        else
            setField(value.intValue());
    }

    public void setField(Short value) {
        if (value == null)
            setNullField(Types.SMALLINT);
        else
            setField(value.shortValue());
    }

    public void setField(Byte value) {
        if (value == null)
            setNullField(Types.TINYINT);
        else
            setField(value.byteValue());
    }

    public void setField(Double value) {
        if (value == null)
            setNullField(Types.FLOAT);
        else
            setField(value.doubleValue());
    }

    public void setField(Float value) {
        if (value == null)
            setNullField(Types.REAL);
        else
            setField(value.floatValue());
    }

    public void setField(Boolean value) {
        if (value == null)
            setNullField(Types.BIT);
        else
            setField(value.booleanValue());
    }

    public void setField(byte[] value) {
        if (value == null)
            setNullField(Types.BINARY);
        else
            try {
                statement.setBytes(fieldCounter++, value);
            } catch (SQLException e) {
                throw Unchecked.rethrow(e);
            }
    }

    public interface Mapper2<R, T1, T2> {
        R execute(T1 it, T2 it2) throws SQLException;
    }

    public interface BuildStep {
        void toSql(QueryBuilder sb);

        void fillData(QueryBuilder sb);

        List<Field> getFields();

        Connection getConnection();

        default PreparedStatement prepare() throws SQLException {
            return prepare(Connection::prepareStatement);
        }

        default PreparedStatement prepare(Mapper2<PreparedStatement, Connection, String> preparer) throws SQLException {
            QueryBuilder builder = new QueryBuilder();
            toSql(builder);
            String sql = builder.toString();
            System.out.println(sql);

            PreparedStatement statement;
            Connection connection = getConnection();

            SqlConnectionKey k = new SqlConnectionKey(sql, connection);
            statement = preparedStatements.get(k);
            if (statement == null || statement.isClosed()) {
                statement = preparer.execute(connection, sql);
                preparedStatements.put(k, statement);
            }
            builder.statement = statement;
            fillData(builder);
            return statement;
        }
    }

    public static abstract class AbstractChainStep implements BuildStep {
        protected final AbstractChainStep previous;

        protected AbstractChainStep(AbstractChainStep previous) {
            this.previous = previous;
        }

        public String toSql() {
            QueryBuilder sb = new QueryBuilder();
            toSql(sb);
            return sb.toString();
        }

        public void toSql(QueryBuilder sb) {
            if (previous != null)
                previous.toSql(sb);
        }

        public void fillData(QueryBuilder sb) {
            if (previous != null)
                previous.fillData(sb);
        }

        public Connection getConnection() {
            return previous != null ? previous.getConnection() : null;
        }

        public List<Field> getFields() {
            return previous != null ? previous.getFields() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AbstractChainStep that = (AbstractChainStep) o;

            return previous != null ? previous.equals(that.previous) : that.previous == null;
        }

        @Override
        public int hashCode() {
            return previous != null ? previous.hashCode() : 0;
        }
    }

    public static class WrapConnectionStep extends AbstractChainStep {
        private final Connection connection;

        protected WrapConnectionStep(Connection connection) {
            super(null);
            this.connection = connection;
        }

        @Override
        public Connection getConnection() {
            return connection;
        }

        public SelectStep select() {
            return new SelectStep(this);
        }

        public SelectStep select(List<Field> fields) {
            return new SelectStep(this, fields);
        }

        public SelectStep select(Field... fields) {
            return new SelectStep(this, Arrays.asList(fields));
        }

        public InsertIntoStep insertInto(Table table) {
            return new InsertIntoStep(this, table);
        }

        public DeleteFromStep deleteFrom(Table table) {
            return new DeleteFromStep(this, table);
        }

        public UpdateStep update(Table table) {
            return new UpdateStep(this, table);
        }
    }

    public static class SelectStep extends AbstractChainStep implements FetchableStep {
        private List<Field> fields;

        public SelectStep(AbstractChainStep previous) {
            super(previous);
        }

        public SelectStep(AbstractChainStep previous, List<Field> fields) {
            super(previous);
            this.fields = fields;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (fields == null || fields.isEmpty()) {
                sb.append("select *");
            } else {
                sb.append("select ");
                for (int i = 0; i < fields.size(); i++) {
                    if (i > 0)
                        sb.append(", ");

                    fields.get(i).toSql(sb);
                }
            }
        }

        @Override
        public List<Field> getFields() {
            return fields;
        }

        public FromStep from(Table table) {
            return new FromStep(this, table);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            SelectStep that = (SelectStep) o;

            return fields != null ? fields.equals(that.fields) : that.fields == null;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (fields != null ? fields.hashCode() : 0);
            return result;
        }
    }

    public interface FetchableStep extends BuildStep, ExecutableStep {

        default ResultSet executeQuery() throws SQLException {
            return prepare().executeQuery();
        }

        default <R> List<R> fetchInto(Class<R> cl) throws SQLException {
            return fetchInto(cl, new ArrayList<>());
        }

        default <R, C extends Collection<R>> C fetchInto(Class<R> cl, C collector) throws SQLException {
            List<Field> fields = getFields();
            FieldMappers.RecordMapper<R> mapper;
            if (fields == null)
                mapper = FieldMappers.fromSqlMapper(cl);
            else
                mapper = FieldMappers.fromSqlMapper(cl, fields);

            ResultSet rs = executeQuery();
//            boolean next = rs.next();
//            System.out.println("fetchInto: " + next);
            while (rs.next()) {
                collector.add(mapper.map(rs));
//                next = rs.next();
//                System.out.println("fetchInto: " + next);
            }
            return collector;
        }

        default <R> Stream<R> fetchIntoStream(Class<R> cl) throws SQLException {
            List<Field> fields = getFields();
            FieldMappers.RecordMapper<R> mapper;
            if (fields == null)
                mapper = FieldMappers.fromSqlMapper(cl);
            else
                mapper = FieldMappers.fromSqlMapper(cl, fields);

            ResultSet rs = executeQuery();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<R>() {
                @Override
                public boolean hasNext() {
                    try {
                        return rs.next();
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                }

                @Override
                public R next() {
                    return mapper.map(rs);
                }
            }, 0), false);
        }

        default <R> R fetchOneInto(Class<R> cl) throws SQLException {
            List<Field> fields = getFields();
            FieldMappers.RecordMapper<R> mapper;
            if (fields == null)
                mapper = FieldMappers.fromSqlMapper(cl);
            else
                mapper = FieldMappers.fromSqlMapper(cl, fields);

            ResultSet rs = executeQuery();
//            boolean next = rs.next();
//            System.out.println("fetchOneInto: " + next);
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        }
    }

    public interface ExecutableStep extends BuildStep {
        default int executeUpdate() throws SQLException {
            return prepare().executeUpdate();
        }

        default int executeInsert() throws SQLException {
            PreparedStatement statement = prepare((connection, sql) -> connection.prepareStatement(sql));
            return statement.executeUpdate();
        }

        default long executeInsert(Field indexField) throws SQLException {
            PreparedStatement statement = prepare((connection, sql) -> connection.prepareStatement(sql, new String[]{indexField.name}));
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else
                throw new IllegalStateException();
        }
    }

    public static class FromStep extends AbstractChainStep implements FetchableStep {
        private final Table table;

        public FromStep(AbstractChainStep previous, Table table) {
            super(previous);
            this.table = table;
        }

        public JoinStep join(Table table) {
            return new JoinStep(this, table);
        }

        public WhereStep where(Condition condition) {
            return new WhereStep(this, condition);
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" from ").append(table.name);
            if (table.alias != null)
                sb.append(" as ").append(table.alias);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            FromStep fromStep = (FromStep) o;

            return table.equals(fromStep.table);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }

        public LimitStep limit(int limit) {
            return new LimitStep(this, limit);
        }

        public OffsetStep offset(int offset) {
            return new OffsetStep(this, offset);
        }

        public OffsetLimitStep offsetLimit(int offset, int limit) {
            return new OffsetLimitStep(this, offset, limit);
        }

        public OrderByStep orderBy(Field field) {
            return new OrderByStep(this, field);
        }

        public OrderByStep orderBy(Field field, OrderByStep.Order order) {
            return new OrderByStep(this, field, order);
        }
    }

    public static class JoinStep extends AbstractChainStep {
        private final Table table;

        public JoinStep(AbstractChainStep previous, Table table) {
            super(previous);
            this.table = table;
        }

        public JoinOnStep on(Condition.JoinCondition condition) {
            return new JoinOnStep(this, condition);
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" join ");
            if (table.alias == null)
                sb.append(table.name);
            else
                sb.append(table.alias);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            JoinStep fromStep = (JoinStep) o;

            return table.equals(fromStep.table);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }

    public static class JoinOnStep extends AbstractChainStep implements FetchableStep {
        private final Condition.JoinCondition condition;

        public JoinOnStep(AbstractChainStep previous, Condition.JoinCondition condition) {
            super(previous);
            this.condition = condition;
        }

        public JoinStep join(Table table) {
            return new JoinStep(this, table);
        }

        public WhereStep where(Condition condition) {
            return new WhereStep(this, condition);
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" on");
            condition.toSql(sb);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            JoinOnStep that = (JoinOnStep) o;

            return condition.equals(that.condition);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + condition.hashCode();
            return result;
        }
    }

    public static class WhereStep extends AbstractChainStep implements FetchableStep {
        private final Condition condition;

        public WhereStep(AbstractChainStep previous, Condition condition) {
            super(previous);
            this.condition = condition;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" where");
            condition.toSql(sb);
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);
            condition.fillData(sb);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            WhereStep whereStep = (WhereStep) o;

            return condition.equals(whereStep.condition);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + condition.hashCode();
            return result;
        }

        public LimitStep limit(int limit) {
            return new LimitStep(this, limit);
        }

        public OffsetStep offset(int offset) {
            return new OffsetStep(this, offset);
        }

        public OffsetLimitStep offsetLimit(int offset, int limit) {
            return new OffsetLimitStep(this, offset, limit);
        }

        public OrderByStep orderBy(Field field) {
            return new OrderByStep(this, field);
        }

        public OrderByStep orderBy(Field field, OrderByStep.Order order) {
            return new OrderByStep(this, field, order);
        }
    }

    public static class OrderByStep extends AbstractChainStep implements FetchableStep {
        private final Order order;
        private final Field by;

        public enum Order {DESC, ASC}

        public OrderByStep(AbstractChainStep previous, Field by, Order order) {
            super(previous);
            this.by = by;
            this.order = order;
        }

        public OrderByStep(AbstractChainStep previous, Field field) {
            this(previous, field, Order.ASC);
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" order by ");
            by.toSql(sb);
            sb.append(" ").append(order.name());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            OrderByStep orderByStep = (OrderByStep) o;

            return by.equals(orderByStep.by) && order == orderByStep.order;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + by.hashCode();
            result = 31 * result + order.hashCode();
            return result;
        }

        public LimitStep limit(int limit) {
            return new LimitStep(this, limit);
        }

        public OffsetLimitStep offsetLimit(int offset, int limit) {
            return new OffsetLimitStep(this, offset, limit);
        }
    }

    public static class LimitStep extends AbstractChainStep implements FetchableStep {
        private final int limit;

        public LimitStep(AbstractChainStep previous, int limit) {
            super(previous);
            this.limit = limit;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (DBTools.isOracleDB(getConnection()))
                sb.append(" fetch next ? rows only");
            else
                sb.append(" limit ?");
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);
            sb.setField(limit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            LimitStep step = (LimitStep) o;

            return limit == step.limit;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + limit;
            return result;
        }
    }

    public static class OffsetStep extends AbstractChainStep implements FetchableStep {
        private final int offset;

        public OffsetStep(AbstractChainStep previous, int offset) {
            super(previous);
            this.offset = offset;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (DBTools.isOracleDB(getConnection()))
                sb.append(" offset ? rows");
            else
                sb.append(" offset ?");
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);
            sb.setField(offset);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            OffsetStep step = (OffsetStep) o;

            return offset == step.offset;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + offset;
            return result;
        }
    }

    public static class OffsetLimitStep extends AbstractChainStep implements FetchableStep {
        private final int offset;
        private final int limit;

        public OffsetLimitStep(AbstractChainStep previous, int offset, int limit) {
            super(previous);
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (DBTools.isOracleDB(getConnection()))
                sb.append(" offset ? rows fetch next ? rows only");
            else
                sb.append(" limit ? offset ?");
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);

            if (DBTools.isOracleDB(getConnection())) {
                sb.setField(offset);
                sb.setField(limit);
            } else {
                sb.setField(limit);
                sb.setField(offset);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            OffsetLimitStep step = (OffsetLimitStep) o;

            return offset == step.offset && limit == step.limit;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + offset;
            result = 31 * result + limit;
            return result;
        }
    }


    public static class UpdateStep extends AbstractChainStep {
        private final Table table;

        public UpdateStep(AbstractChainStep previous, Table table) {
            super(previous);
            this.table = table;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append("update ").append(table.name);
            if (table.alias != null)
                sb.append(" as ").append(table.alias);
        }

        public SetValueStep set(Condition.FieldCondition fieldCondition) {
            return new SetValueStep(this, fieldCondition);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            UpdateStep that = (UpdateStep) o;

            return table.equals(that.table);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }


    public static abstract class SetStep extends AbstractChainStep {

        protected SetStep(AbstractChainStep previous) {
            super(previous);
        }

        public SetValueStep set(Condition.FieldCondition fieldCondition) {
            return new SetValueStep(this, fieldCondition);
        }

        public SetFieldStep set(Condition.JoinCondition fieldCondition) {
            return new SetFieldStep(this, fieldCondition);
        }

        public WhereStep where(Condition condition) {
            return new WhereStep(this, condition);
        }
    }

    public static class SetValueStep extends SetStep {
        private final Condition.FieldCondition condition;

        public SetValueStep(AbstractChainStep previous, Condition.FieldCondition condition) {
            super(previous);
            this.condition = condition;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (previous instanceof SetValueStep)
                sb.append(", ");
            else
                sb.append(" set ");

            sb.append(condition.field.name).append("=?");
            String cast = condition.mapper.getCast(getConnection());
            if (cast != null)
                sb.append(cast);
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);
            condition.fillData(sb);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            SetValueStep that = (SetValueStep) o;

            return condition.equals(that.condition);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + condition.hashCode();
            return result;
        }
    }

    public static class SetFieldStep extends SetStep {
        private final Condition.JoinCondition condition;

        public SetFieldStep(AbstractChainStep previous, Condition.JoinCondition condition) {
            super(previous);
            this.condition = condition;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            if (previous instanceof SetValueStep)
                sb.append(", ");
            else
                sb.append(" set ");

            sb.append(condition.a.name).append("=").append(condition.b.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            SetFieldStep that = (SetFieldStep) o;

            return condition.equals(that.condition);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + condition.hashCode();
            return result;
        }
    }

    public static class DeleteFromStep extends AbstractChainStep {
        private final Table table;

        public DeleteFromStep(AbstractChainStep previous, Table table) {
            super(previous);
            this.table = table;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append("delete from ").append(table.name);
            if (table.alias != null)
                sb.append(" as ").append(table.alias);
        }

        public WhereStep where(Condition condition) {
            return new WhereStep(this, condition);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            DeleteFromStep that = (DeleteFromStep) o;

            return table.equals(that.table);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }


    public static class InsertIntoStep extends AbstractChainStep {
        private final Table table;

        public InsertIntoStep(AbstractChainStep previous, Table table) {
            super(previous);
            this.table = table;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append("insert into ").append(table.name);
            if (table.alias != null)
                sb.append(" as ").append(table.alias);
        }

        public InsertFieldsStep fields(List<Field> fields) {
            return new InsertFieldsStep(this, fields);
        }

        public InsertValuesStep values(Object o) {
            return values(o, false);
        }

        public InsertValuesStep values(Object o, boolean withIdField) {
            List<Field> fields = table.getFields();
            if (!withIdField)
                fields = fields.stream()
                        .filter(field -> !field.getName().equals("id"))
                        .collect(Collectors.toList());

            return new InsertFieldsStep(this, fields).values(o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            InsertIntoStep that = (InsertIntoStep) o;

            return table.equals(that.table);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + table.hashCode();
            return result;
        }
    }

    public static class InsertFieldsStep extends AbstractChainStep {
        private final List<Field> fields;

        public InsertFieldsStep(AbstractChainStep previous, List<Field> fields) {
            super(previous);
            this.fields = fields;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" (");
            for (Field field : fields) {
                sb.append(field.getName()).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(")");
        }

        public InsertValuesStep values(Object o) {
            List<Field.ToSqlMapper> mappers = fields.stream().map(it -> it.toSqlMapper(o.getClass())).collect(Collectors.toList());
            return new InsertValuesStep(this, o, mappers);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            InsertFieldsStep that = (InsertFieldsStep) o;

            return fields.equals(that.fields);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + fields.hashCode();
            return result;
        }
    }

    public static class InsertValuesStep extends AbstractChainStep implements ExecutableStep {
        private final List<Field.ToSqlMapper> fields;
        private final Object o;

        public InsertValuesStep(AbstractChainStep previous, Object o, List<Field.ToSqlMapper> fields) {
            super(previous);
            this.fields = fields;
            this.o = o;
        }

        @Override
        public void toSql(QueryBuilder sb) {
            super.toSql(sb);
            sb.append(" values (");
            Connection connection = getConnection();
            for (int i = 0; i < fields.size(); i++) {
                String cast = fields.get(i).getCast(connection);
                if (cast == null)
                    sb.append("?, ");
                else
                    sb.append("? ").append(cast).append(", ");
            }
            sb.setLength(sb.length() - 2);
            sb.append(")");
        }

        @Override
        public void fillData(QueryBuilder sb) {
            super.fillData(sb);
            for (Field.ToSqlMapper field : fields) {
                field.map(o, sb);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!super.equals(o)) return false;

            InsertValuesStep that = (InsertValuesStep) o;

            return fields.size() == that.fields.size();
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + fields.size();
            return result;
        }
    }

    public static class TIMESTAMP {

        private TIMESTAMP() {
        }

        public static Timestamp now() {
            return new Timestamp(System.currentTimeMillis());
        }
    }
}
