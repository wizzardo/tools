package com.wizzardo.tools.sql;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimplePooledConnection implements PooledConnection {

    protected Connection physicalConnection;
    protected ProxyConnection proxyConnection;
    protected List<ConnectionEventListener> connectionListeners = new ArrayList<>();

    public SimplePooledConnection(Connection physicalConnection) {
        this.physicalConnection = physicalConnection;
        proxyConnection = new ProxyConnection();
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!proxyConnection.isClosed())
            proxyConnection.close();

        if (physicalConnection == null || physicalConnection.isClosed())
            throw new SQLException("Connection is closed");

        proxyConnection.reset();
        return proxyConnection;
    }


    @Override
    public void addStatementEventListener(StatementEventListener listener) {
    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        connectionListeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionListeners.remove(listener);
    }

    public List<ConnectionEventListener> getListeners() {
        return connectionListeners;
    }

    public void close() throws SQLException {
        connectionListeners.clear();
        proxyConnection.close();

        if (physicalConnection != null) {
            try {
                physicalConnection.close();
            } finally {
                physicalConnection = null;
            }
        }
    }

    protected class ProxyConnection extends ConnectionAdapter {
        volatile boolean isClosed;
        List<AutoCloseable> closeables = new ArrayList<>();

        public ProxyConnection() {
            super(SimplePooledConnection.this.physicalConnection);
        }

        public void reset() {
            isClosed = false;
            closeables.clear();
        }

        @Override
        public void close() throws SQLException {
            ConnectionEvent event = new ConnectionEvent(SimplePooledConnection.this);

            for (int i = connectionListeners.size() - 1; i >= 0; i--) {
                connectionListeners.get(i).connectionClosed(event);
            }

            if (!physicalConnection.getAutoCommit()) {
                physicalConnection.rollback();
            }
            try {
                for (AutoCloseable closeable : closeables) {
                    closeable.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            physicalConnection.setAutoCommit(true);
            isClosed = true;
        }

        @Override
        public boolean isClosed() throws SQLException {
            if (!isClosed)
                isClosed = super.isClosed();

            return isClosed;
        }

        @Override
        protected void onSQLException(SQLException e) {
            if ("database connection closed".equals(e.getMessage())) {
                ConnectionEvent event = new ConnectionEvent(SimplePooledConnection.this, e);

                for (int i = connectionListeners.size() - 1; i >= 0; i--) {
                    connectionListeners.get(i).connectionErrorOccurred(event);
                }
            }
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql);
            closeables.add(statement);
            return statement;
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            CallableStatement statement = super.prepareCall(sql);
            closeables.add(statement);
            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency);
            closeables.add(statement);
            return statement;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            CallableStatement statement = super.prepareCall(sql, resultSetType, resultSetConcurrency);
            closeables.add(statement);
            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            closeables.add(statement);
            return statement;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            CallableStatement statement = super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
            closeables.add(statement);
            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, autoGeneratedKeys);
            closeables.add(statement);
            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, columnIndexes);
            closeables.add(statement);
            return statement;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            PreparedStatement statement = super.prepareStatement(sql, columnNames);
            closeables.add(statement);
            return statement;
        }
    }
}
