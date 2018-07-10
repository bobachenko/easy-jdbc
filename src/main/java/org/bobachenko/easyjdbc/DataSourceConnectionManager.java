/*
Copyright (c) 2018 Maxim Bobachenko Contacts: <max@bobachenko.org>

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
package org.bobachenko.easyjdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection manager for data source.
 * It keeps connections as thread local for multithreading access.
 */
class DataSourceConnectionManager implements ConnectionManager {

    private final DataSource dataSource;
    private final ThreadLocal<Connection> connectionHoler = new ThreadLocal<>();

    DataSourceConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Get connection from datasource and store it to map.
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        connectionHoler.set(connection);
        return connection;
    }

    /**
     * Get connection from map and close it.
     */
    @Override
    public void closeConnection() throws SQLException {
        Connection con = connectionHoler.get();
        if (con != null)
            con.close();
        connectionHoler.remove();
    }
}
