/*
Copyright (c) 2018 Maxim Bobachenko  Contacts: <max@bobachenko.org>

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.bobachenko.easyjdbc.datasource;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Easy {@code DataSource} implementation is for test purpose only.
 *
 * Don't use it for real application except if your application is very simple.
 * It doesn't have connection pool and other necessary features.
 *
 * @author Maxim Bobachenko
 */
public final class EasyDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;

    /**
     * Factory method to create instance of EasyDataSource
     * @param driverClass the fully qualified name of the jdbc driver class.
     * @param url a database url of the form <code> jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param username the user's name
     * @param password the user's password
     * @return instance of EasyDataSource
     */
    public static DataSource of(String driverClass, String url, String username, String password) {
        return new EasyDataSource(driverClass, url, username, password);
    }

    private EasyDataSource(String driverClass, String url, String username, String password) {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.url = url;
        this.username = username;
        this.password = password;
    }


    /**
     * Tries to establish a connection.
     *
     * @return Connection
     * @throws SQLException if a database access error occurs
     * @see javax.sql.DataSource#getConnection()
     */
    @Override
    public Connection getConnection() throws SQLException {
        if(username!=null && !username.isEmpty() && password!=null && !password.isEmpty())
            return DriverManager.getConnection(url, username, password);
        return DriverManager.getConnection(url);
    }


    /**
     * Tries to establish a connection.
     *
     * @param username the user's name
     * @param password the user's password
     * @return a connection to the data source
     * @throws SQLException if a database access error happens.
     * @see javax.sql.DataSource#getConnection(String, String)
     */
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * This method isn't implemented and always throws UnsupportedOperationException
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * This method isn't implemented and always throws UnsupportedOperationException
     */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the log writer for this object.
     *
     * @return the log writer or null if logging is disabled
     * @exception java.sql.SQLException if a database access error happens.
     * @see CommonDataSource#getLogWriter()
     */
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /**
     * Sets the log writer for this object.
     *
     * @param out the new log writer or null if you'd like to disable logging
     * @exception SQLException if a database access error happens.
     * @see CommonDataSource#setLogWriter
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    /**
     * Sets the time in seconds that this object waits while connection is establishing.
     * If value is zero it's indicating that timeout is the default system timeout.
     *
     * @param seconds the data source login time limit
     * @exception SQLException if a database access error happens.
     * @see CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    /**
     * Gets the time in seconds that this object waits while connection is establishing.
     * If value is zero it's indicating that timeout is the default system timeout.
     *
     * @return time limit
     * @exception SQLException if a database access error happens.
     * @see CommonDataSource#setLoginTimeout(int)
     */
    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    /**
     * This method isn't implemented and always throws SQLFeatureNotSupportedException
     */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
}
