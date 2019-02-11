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
import org.bobachenko.easyjdbc.mapper.KeyMapper;
import org.bobachenko.easyjdbc.mapper.ResultMapper;
import org.bobachenko.easyjdbc.mapper.RowMapper;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class to use JDBC easily.
 *
 * @author Maxim Bobachenko
 */
public final class EasyJdbcImpl implements EasyJdbc {

    //TODO Maybe it would be better to use another logger.
    private Logger logger = Logger.getLogger(EasyJdbc.class.getName());

    private final ConnectionManager connectionManager;

    EasyJdbcImpl(ConnectionManager connectionManager) {
        if (connectionManager == null)
            throw new IllegalStateException("The dataSource parameter cannot be null.");

        this.connectionManager = connectionManager;
    }

    private <T> T exec(Operation<T> operation) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            return operation.run(connectionManager.getConnection(), stmt, rs);
        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(stmt, rs);
        }
    }

    /**
     * Executes a common jdbc operation.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query
     * @param params parameters for the query with correspondent types, if it's needed
     * @return Optional with user's object inside. It depends on implementation of the mapper.
     */
    @Override
    public <T> Optional<T> queryResult(String sql, ResultMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        return exec((con, st, rs) -> {
            st = prepareStatement(con, sql, params);
            rs = st.executeQuery();
            return mapper.map(rs);
        });
    }

    /**
     * Executes a query and returns scalar value with a given type.
     * @param sql a query with one field. If field isn't one this method returns first field.
     * @param typeOfReturnValue Type to cast.
     *                          ClassCastException is possible if types of filed and this parameter are not appropriate.
     * @param params parameters for the query in it's needed
     * @return value if it exists
     */
    @Override
    public <T> Optional<T> queryScalar(String sql, Class<T> typeOfReturnValue, Object... params) {
        return queryResult(sql, rs -> {
            if(rs.next())
                return Optional.of(typeOfReturnValue.cast(rs.getObject(1)));
            return Optional.empty();
        }, params);
    }

    /**
     * Executes a query and creates an object that has a data of the single row of the result.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query
     * @param params  parameters for the query with correspondent types, if it's needed
     * @return Optional with user's object inside. It depends on implementation of the mapper.
     */
    @Override
    public <T> Optional<T> queryObject(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        return queryResult(sql, rs -> {
            if(rs.next())
                return Optional.of(mapper.map(rs, 0));
            return Optional.empty();
        }, params);
    }

    /**
     * Executes a query and creates a list of maps that have a data of all rows of the result.
     * Every map in the list contains string keys with the names like columns before keyword FROM in the query.
     * @param sql a query to execute
     * @param params parameters for the query with correspondent types, if it's needed
     */
    @Override
    public List<Map<String, Object>> queryAssoc(String sql, Object... params) {

        return queryResult(sql, rs -> {
            List<Map<String, Object>> result = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> record = new TreeMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.putIfAbsent(metaData.getColumnName(i), rs.getObject(i));
                }

                result.add(record);
            }

            return Optional.of(result);
        }, params).get();
    }

    /**
     * Executes a query and creates a list of object that have a data of all rows of the result.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query. It's called for every rows in ResultSet
     * @param params parameters for the query with correspondent types, if it's needed
     */
    @Override
    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        return queryResult(sql, rs -> {
            List<T> result = new ArrayList<>();

            int rowNum = 0;
            while (rs.next())
                result.add(mapper.map(rs, rowNum++));
            return Optional.of(result);

        }, params).get();

    }

    /**
     * Creates a new row by "INSERT" statement and returns value of a primary key
     * @param sql a query with the INSERT keyword to execute
     * @param typeOfNotCompositePrimaryKey type of primary key
     * @param params parameters for the query for insert
     * @return value of primary key
     */
    @Override
    public <T> Optional<T> create(String sql, Class<T> typeOfNotCompositePrimaryKey, Object... params) {
        KeyMapper<T> compositeKeyMapper = null;
        if (typeOfNotCompositePrimaryKey != null)
            compositeKeyMapper = rs -> typeOfNotCompositePrimaryKey.cast(rs.getObject(1));
        return create(sql, compositeKeyMapper, params);
    }

    /**
     * Creates a new row by "INSERT" statement and returns an object that contains the data of the composite primary key
     * @param sql a query with the INSERT keyword to execute
     * @param compositeKeyMapper class or lambda to map the value of the composite key
     * @param params parameters for the query for insert
     * @return object that contains a data of the composite primary key
     */
    @Override
    public <T> Optional<T> create(String sql, KeyMapper<T> compositeKeyMapper, Object... params) {
        return exec((con, st, rs) -> {
            if (con.isReadOnly())
                throw new IllegalStateException("Connection cannot be in read only state when" +
                        " create operation is being called!");

            st = prepareStatement(con, sql, true, params);
            st.executeUpdate();

            // map key
            if (compositeKeyMapper != null) {
                rs = st.getGeneratedKeys();
                if (rs != null && rs.next())
                    return Optional.of(compositeKeyMapper.map(rs));
            }

            return Optional.empty();
        });
    }

    /**
     * Executes a query to modify the data by the "UPDATE" or "DELETE" keywords.
     * @param sql a query to execute
     * @param params parameters for the query
     * @return the number of rows affected
     */
    @Override
    public int update(String sql, Object... params) {
        return exec((con, st, rs) -> {
            if (con.isReadOnly())
                throw new IllegalStateException("Connection cannot be in read only state when " +
                        "create operation is being called!");

            st = prepareStatement(con, sql, params);
            return st.executeUpdate();
        });
    }

    /**
     * Prepare statement and fill parameters
     */
    private PreparedStatement prepareStatement(Connection connection, String sql,
                                               boolean returnKey, Object... params) throws SQLException {
        PreparedStatement statement;
        statement = returnKey ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) :
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.clearParameters();

        if (params != null) {
            int num = 0;
            for (Object param : params)
                addParameter(connection, ++num, statement, param);
        }
        return statement;
    }

    /**
     * Prepare statement and fill parameters. Statement doesn't return generated keys
     */
    private PreparedStatement prepareStatement(Connection connection, String sql,
                                               Object... params) throws SQLException {
        return prepareStatement(connection, sql, false, params);
    }

    /**
     * Close all JDBC object
     * Because it's a good practice to always close ResultSet
     * and Statement explicitly and not to rely on Connection.close.
     */
    private void close(Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Close result set error", e);
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Close statement error", e);
            }
        }

        try {
            connectionManager.closeConnection();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Close connection error", e);
        }
    }

    /**
     * Add parameter to statement
     */
    private void addParameter(Connection con, int numberOfParam, PreparedStatement statement,
                              Object paramValue) throws SQLException {

        // cast java types to JDBC types
        if (paramValue instanceof Boolean) {
            statement.setBoolean(numberOfParam, (boolean) paramValue);
            return;
        } else if (paramValue instanceof Byte) {
            statement.setByte(numberOfParam, (byte) paramValue);
            return;
        } else if (paramValue instanceof Double) {
            statement.setDouble(numberOfParam, (double) paramValue);
            return;
        } else if (paramValue instanceof Float) {
            statement.setFloat(numberOfParam, (float) paramValue);
            return;
        } else if (paramValue instanceof Integer) {
            statement.setInt(numberOfParam, (int) paramValue);
            return;
        } else if (paramValue instanceof Long) {
            statement.setLong(numberOfParam, (long) paramValue);
            return;
        } else if (paramValue instanceof Short) {
            statement.setShort(numberOfParam, (short) paramValue);
            return;
        } else if (paramValue instanceof String) {
            statement.setString(numberOfParam, paramValue.toString());
            return;
        } else if (paramValue instanceof Character) {
            statement.setString(numberOfParam, String.valueOf(paramValue));
            return;
        } else if (paramValue instanceof Date) {
            statement.setTimestamp(numberOfParam, new Timestamp(((Date) paramValue).getTime()));
            return;
        } else if (paramValue instanceof Calendar) {
            statement.setTimestamp(numberOfParam, new Timestamp(((Calendar) paramValue).getTimeInMillis()));
            return;
        } else if (paramValue instanceof Instant) {
            statement.setTimestamp(numberOfParam, new Timestamp(((Instant) paramValue).toEpochMilli()));
            return;
        } else if (paramValue instanceof BigDecimal) {
            statement.setBigDecimal(numberOfParam, (BigDecimal) paramValue);
            return;
        } else if (paramValue instanceof Array) {
            Array a = (Array) paramValue;
            java.sql.Array sqlArray = con.createArrayOf(a.getDbDatatype(), a.getValues());
            statement.setArray(numberOfParam, sqlArray);
            return;
        }

        // If it's another type, we have to rely on JDBC
        statement.setObject(numberOfParam, paramValue);
    }
}