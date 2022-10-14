package com.wizzardo.tools.sql;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class SimpleConnectionPoolDataSource implements ConnectionPoolDataSource, DataSource {

    protected final DataSource dataSource;

    public SimpleConnectionPoolDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws ClassCastException {
        return iface.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return getPooledConnection(null, null);
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return new SimplePooledConnection(getConnection(user, password));
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
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
