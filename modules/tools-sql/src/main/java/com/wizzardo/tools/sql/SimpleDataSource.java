package com.wizzardo.tools.sql;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class SimpleDataSource implements DataSource {
    protected transient PrintWriter logger;
    protected Driver driver;
    protected String url;
    protected Properties properties;


    public SimpleDataSource(Driver driver, String url) {
        this(driver, url, new Properties());
    }

    public SimpleDataSource(Driver driver, String url, Properties properties) {
        this.driver = driver;
        this.url = url;
        this.properties = properties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null, null);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties properties = this.properties;
        if (username != null || password != null) {
            properties = new Properties(properties);
            if (username != null)
                properties.put("user", username);
            if (password != null)
                properties.put("pass", password);
        }
        return driver.connect(url, properties);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return logger;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.logger = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("getParentLogger");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws ClassCastException {
        return iface.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }
}
