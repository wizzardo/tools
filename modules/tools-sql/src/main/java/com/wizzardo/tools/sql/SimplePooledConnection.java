package com.wizzardo.tools.sql;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimplePooledConnection implements PooledConnection {

    protected Connection physicalConnection;
    protected volatile Connection proxyConnection;

    //    protected List<StatementEventListener> statementListeners = new ArrayList<>();
    protected List<ConnectionEventListener> connectionListeners = new ArrayList<>();

    public SimplePooledConnection(Connection physicalConnection) {
        this.physicalConnection = physicalConnection;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (proxyConnection != null)
            proxyConnection.close();

        if (physicalConnection == null || physicalConnection.isClosed())
            throw new SQLException("Connection is closed");

        proxyConnection = new ConnectionAdapter(physicalConnection) {
            boolean isClosed;

            @Override
            public void close() throws SQLException {
                ConnectionEvent event = new ConnectionEvent(SimplePooledConnection.this);

                for (int i = connectionListeners.size() - 1; i >= 0; i--) {
                    connectionListeners.get(i).connectionClosed(event);
                }

                if (!physicalConnection.getAutoCommit()) {
                    physicalConnection.rollback();
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
        };

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
        if (proxyConnection != null) {
            connectionListeners.clear();
            proxyConnection.close();
        }

        if (physicalConnection != null) {
            try {
                physicalConnection.close();
            } finally {
                physicalConnection = null;
            }
        }
    }
}
