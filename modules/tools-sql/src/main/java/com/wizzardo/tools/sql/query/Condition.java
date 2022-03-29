package com.wizzardo.tools.sql.query;

public abstract class Condition {

    protected abstract void toSql(QueryBuilder builder);

    public void fillData(QueryBuilder builder) {
    }

    public enum Operator {
        EQ("="),
        AND("and"),
        OR("or"),
        IN("in"),
        NOT_IN("not in"),
        ;

        final String representation;

        Operator(String representation) {
            this.representation = representation;
        }
    }

    public Condition and(Condition condition) {
        return new MultiCondition(this, Operator.AND, condition);
    }

    public Condition or(Condition condition) {
        return new MultiCondition(this, Operator.OR, condition);
    }

    public static class FieldCondition extends Condition {
        public final Field field;
        public final Operator operator;
        public final Object value;
        public final Field.ToSqlMapper mapper;

        public FieldCondition(Field field, Operator operator, Object value, Field.ToSqlMapper mapper) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.mapper = mapper;
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" ");
            field.toSql(sb);
            sb.append(operator.representation);
            sb.append("?");
        }

        @Override
        public void fillData(QueryBuilder builder) {
            mapper.map(value, builder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FieldCondition that = (FieldCondition) o;

            if (!field.equals(that.field)) return false;
            return operator == that.operator;
        }

        @Override
        public int hashCode() {
            int result = field.hashCode();
            result = 31 * result + operator.hashCode();
            return result;
        }
    }

    public static class IsNullCondition extends Condition {
        public final Field field;

        public IsNullCondition(Field field) {
            this.field = field;
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" ");
            field.toSql(sb);
            sb.append(" is null");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsNullCondition that = (IsNullCondition) o;

            return field.equals(that.field);
        }

        @Override
        public int hashCode() {
            return field.hashCode();
        }
    }

    public static class IsNotNullCondition extends Condition {
        public final Field field;

        public IsNotNullCondition(Field field) {
            this.field = field;
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" ");
            field.toSql(sb);
            sb.append(" is not null");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsNotNullCondition that = (IsNotNullCondition) o;

            return field.equals(that.field);
        }

        @Override
        public int hashCode() {
            return field.hashCode();
        }
    }

    public static class InCondition extends Condition {
        public final Field field;
        public final Operator operator;
        public final QueryBuilder.FetchableStep subquery;

        public InCondition(Field field, Operator operator, QueryBuilder.FetchableStep subquery) {
            this.field = field;
            this.operator = operator;
            this.subquery = subquery;
            if (operator != Operator.IN && operator != Operator.NOT_IN)
                throw new IllegalArgumentException("InCondition cannot handle operator "+operator);
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" ");
            field.toSql(sb);
            sb.append(" ");
            sb.append(operator.representation);
            sb.append(" (");
            subquery.toSql(sb);
            sb.append(")");
        }

        @Override
        public void fillData(QueryBuilder builder) {
            subquery.fillData(builder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InCondition)) return false;

            InCondition that = (InCondition) o;

            if (!field.equals(that.field)) return false;
            if (operator != that.operator) return false;
            return subquery.equals(that.subquery);
        }

        @Override
        public int hashCode() {
            int result = field.hashCode();
            result = 31 * result + operator.hashCode();
            result = 31 * result + subquery.hashCode();
            return result;
        }
    }

    public static class JoinCondition extends Condition {
        public final Field a;
        public final Field b;

        JoinCondition(Field a, Field b) {
            this.a = a;
            this.b = b;
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" ");
            a.toSql(sb);
            sb.append("=");
            b.toSql(sb);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            JoinCondition that = (JoinCondition) o;

            if (!a.equals(that.a)) return false;
            return b.equals(that.b);
        }

        @Override
        public int hashCode() {
            int result = a.hashCode();
            result = 31 * result + b.hashCode();
            return result;
        }
    }

    public static class MultiCondition extends Condition {
        private final Condition a;
        private final Condition b;
        private final Operator operator;

        MultiCondition(Condition a, Operator operator, Condition b) {
            this.a = a;
            this.b = b;
            this.operator = operator;
        }

        @Override
        protected void toSql(QueryBuilder sb) {
            sb.append(" (");
            a.toSql(sb);
            sb.append(" ) ");
            sb.append(operator.representation);
            sb.append(" (");
            b.toSql(sb);
            sb.append(" )");
        }

        @Override
        public void fillData(QueryBuilder builder) {
            a.fillData(builder);
            b.fillData(builder);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MultiCondition that = (MultiCondition) o;

            if (!a.equals(that.a)) return false;
            if (!b.equals(that.b)) return false;
            return operator == that.operator;
        }

        @Override
        public int hashCode() {
            int result = a.hashCode();
            result = 31 * result + b.hashCode();
            result = 31 * result + operator.hashCode();
            return result;
        }
    }
}
