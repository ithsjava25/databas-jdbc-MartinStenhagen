package com.example;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class SimpleDriverManagerDataSource implements DataSource {

    private final String url;
    private final String user;
    private final String pass;

    public SimpleDriverManagerDataSource(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return java.sql.DriverManager.getConnection(url, user, pass);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return java.sql.DriverManager.getConnection(url, username, password);
    }

    // resten av metoderna kan lämnas tomma eftersom de inte används:
    @Override public <T> T unwrap(Class<T> iface) { return null; }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    @Override public java.io.PrintWriter getLogWriter() { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out) {}
    @Override public void setLoginTimeout(int seconds) {}
    @Override public int getLoginTimeout() { return 0; }
    @Override public java.util.logging.Logger getParentLogger() { return null; }
}

