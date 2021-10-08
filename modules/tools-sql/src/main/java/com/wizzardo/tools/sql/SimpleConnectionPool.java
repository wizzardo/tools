package com.wizzardo.tools.sql;

import com.wizzardo.tools.misc.Unchecked;
import com.wizzardo.tools.misc.pool.Holder;
import com.wizzardo.tools.misc.pool.Pool;
import com.wizzardo.tools.misc.pool.PoolBuilder;
import com.wizzardo.tools.misc.pool.SimpleHolder;

import javax.sql.*;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class SimpleConnectionPool implements DataSource {

    private ConnectionPoolDataSource dataSource;
    private int maxConnections;
    private Pool<PooledConnection> pool;

    static class PooledConnectionHolder extends SimpleHolder<PooledConnection> {
        final ConnectionEventListener listener;
        volatile boolean isIdle = true;
        volatile boolean isErrored = false;

        public PooledConnectionHolder(final Pool<PooledConnection> pool, PooledConnection value) {
            super(pool, value);
            listener = new ConnectionEventListener() {
                @Override
                public void connectionClosed(ConnectionEvent event) {
                    if (isErrored)
                        return;

                    if (!isIdle) {
                        isIdle = true;
                        pool.release(PooledConnectionHolder.this);
                    }
                }

                @Override
                public void connectionErrorOccurred(ConnectionEvent event) {
                    isErrored = true;
                    try {
                        value.close();
                    } catch (SQLException ignored) {
                    }
                    pool.dispose(PooledConnectionHolder.this);
                }
            };
            value.addConnectionEventListener(listener);
        }

        @Override
        public PooledConnection get() {
            if (isErrored)
                return null;

            isIdle = false;
            return super.get();
        }
    }

    public SimpleConnectionPool(ConnectionPoolDataSource dataSource, int maxConnections) {
        this.dataSource = dataSource;
        this.maxConnections = maxConnections;
        pool = new PoolBuilder<PooledConnection>()
                .limitSize(maxConnections)
                .initialSize(0)
                .queue(PoolBuilder.createSharedQueueSupplier())
                .supplier(() -> {
                    PooledConnection pooledConnection;
                    try {
                        pooledConnection = dataSource.getPooledConnection();
                    } catch (SQLException e) {
                        throw Unchecked.rethrow(e);
                    }
                    return pooledConnection;
                })
                .holder(PooledConnectionHolder::new)
                .build();
    }

    public Connection getConnection() throws SQLException {
        return getConnection(false);
    }

    public Connection getConnection(boolean checkValidity) throws SQLException {
        while (true) {
            Holder<PooledConnection> holder = pool.holder();
            PooledConnection pooledConnection = holder.get();
            if (pooledConnection == null)
                continue;

            Connection connection = pooledConnection.getConnection();

            if (checkValidity && !connection.isValid(1)) {
                pool.dispose(holder);
                continue;
            }

            return connection;
        }
    }

    @Override
    public Connection getConnection(String username, String password) {
        throw new IllegalStateException("Login credentials should be set in parent dataSource");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return iface.cast(this);
        } else {
            throw new SQLException("unwrap failed for:" + iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
}
