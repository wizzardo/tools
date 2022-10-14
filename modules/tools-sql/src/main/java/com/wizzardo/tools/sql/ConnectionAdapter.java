package com.wizzardo.tools.sql;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionAdapter implements Connection {

    protected final Connection connection;

    public ConnectionAdapter(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Statement createStatement() throws SQLException {
        throwIfClosed();
        try {
            return connection.createStatement();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    protected void throwIfClosed() throws SQLException {
        if (isClosed())
            throw new SQLException("Connection is closed");
    }

    protected void onSQLException(SQLException e) {
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareCall(sql);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        throwIfClosed();
        try {
            return connection.nativeSQL(sql);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        throwIfClosed();
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        throwIfClosed();
        try {
            return connection.getAutoCommit();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void commit() throws SQLException {
        throwIfClosed();

        try {
            connection.commit();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void rollback() throws SQLException {
        throwIfClosed();
        try {
            connection.rollback();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        throwIfClosed();
        try {
            return connection.getMetaData();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        throwIfClosed();
        try {
            connection.setReadOnly(readOnly);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        throwIfClosed();
        try {
            return connection.isReadOnly();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        throwIfClosed();
        try {
            connection.setCatalog(catalog);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public String getCatalog() throws SQLException {
        throwIfClosed();
        try {
            return connection.getCatalog();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        throwIfClosed();
        try {
            connection.setTransactionIsolation(level);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        throwIfClosed();
        try {
            return connection.getTransactionIsolation();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throwIfClosed();
        try {
            return connection.getWarnings();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void clearWarnings() throws SQLException {
        throwIfClosed();
        try {
            connection.clearWarnings();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        throwIfClosed();
        try {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throwIfClosed();
        try {
            return connection.getTypeMap();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        throwIfClosed();
        try {
            connection.setTypeMap(map);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        throwIfClosed();
        try {
            connection.setHoldability(holdability);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public int getHoldability() throws SQLException {
        throwIfClosed();
        try {
            return connection.getHoldability();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throwIfClosed();
        try {
            return connection.setSavepoint();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        throwIfClosed();
        try {
            return connection.setSavepoint(name);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        throwIfClosed();
        try {
            connection.rollback(savepoint);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        throwIfClosed();
        try {
            connection.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throwIfClosed();
        try {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        throwIfClosed();
        try {
            return connection.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Clob createClob() throws SQLException {
        throwIfClosed();
        try {
            return connection.createClob();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Blob createBlob() throws SQLException {
        throwIfClosed();
        try {
            return connection.createBlob();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public NClob createNClob() throws SQLException {
        throwIfClosed();
        try {
            return connection.createNClob();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throwIfClosed();
        try {
            return connection.createSQLXML();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        throwIfClosed();
        try {
            return connection.isValid(timeout);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        connection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        connection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        throwIfClosed();
        try {
            return connection.getClientInfo(name);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throwIfClosed();
        try {
            return connection.getClientInfo();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        throwIfClosed();
        try {
            return connection.createArrayOf(typeName, elements);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        throwIfClosed();
        try {
            return connection.createStruct(typeName, attributes);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        throwIfClosed();
        try {
            connection.setSchema(schema);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public String getSchema() throws SQLException {
        throwIfClosed();
        try {
            return connection.getSchema();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        throwIfClosed();
        try {
            connection.abort(executor);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        throwIfClosed();
        try {
            connection.setNetworkTimeout(executor, milliseconds);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throwIfClosed();
        try {
            return connection.getNetworkTimeout();
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throwIfClosed();
        try {
            return connection.unwrap(iface);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throwIfClosed();
        try {
            return connection.isWrapperFor(iface);
        } catch (SQLException e) {
            onSQLException(e);
            throw e;
        }
    }
}
