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

import org.bobachenko.easyjdbc.exception.EasySqlException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Connection manager with support of transactions.
 * @author Maxim Bobachenko
 */
class TransactionalConnectionManager implements ConnectionManager {

    private DataSource dataSource;
    private IsolationLevel isolationLevel = IsolationLevel.Default;
    private Connection connection;

    TransactionalConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    TransactionalConnectionManager(DataSource dataSource, IsolationLevel isolationLevel) {
        this.dataSource = dataSource;
        this.isolationLevel = isolationLevel;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            if (isolationLevel != IsolationLevel.Default) {
                DatabaseMetaData metaData = connection.getMetaData();

                if (metaData.supportsTransactionIsolationLevel(isolationLevel.getJdbcLevel())) {
                    connection.setTransactionIsolation(isolationLevel.getJdbcLevel());
                } else {
                    throw new EasySqlException(String.format("Isolation level %s isn't supported!", isolationLevel.toString()), null);
                }
            }
        }
        return connection;
    }

    @Override
    public void closeConnection() throws SQLException {

    }

    void commit() throws SQLException {
        connection.commit();
        close();
    }

    void rollback() throws SQLException {
        connection.rollback();
        close();
    }

    //TODO make it good
    private void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
        connection = null;
    }
}
