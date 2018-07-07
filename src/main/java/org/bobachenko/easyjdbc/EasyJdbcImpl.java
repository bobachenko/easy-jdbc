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
import org.bobachenko.easyjdbc.mapper.RowMapper;

import javax.sql.DataSource;
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
final class EasyJdbcImpl implements EasyJdbc {

    //TODO Consider the option to use another logger later.
    private Logger logger = Logger.getLogger(EasyJdbc.class.getName());

    private DataSource dataSource;

    public EasyJdbcImpl(DataSource dataSource) {
        if (dataSource == null)
            throw new IllegalStateException("The dataSource parameter cannot be null.");

        this.dataSource = dataSource;
    }

    @Override
    public <T> Optional<T> queryScalar(String sql, Class<T> type, Object... params) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, params);
            rs = stmt.executeQuery();

            if (rs.next())
                return Optional.of(type.cast(rs.getObject(1)));
        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, rs);
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<T> queryObject(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, params);

            rs = stmt.executeQuery();

            if (rs.next())
                return Optional.of(mapper.map(rs, 0));

        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, rs);
        }

        return Optional.empty();
    }

    @Override
    public List<Map<String, Object>> queryAssoc(String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, params);

            rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> record = new TreeMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.putIfAbsent(metaData.getColumnName(i), rs.getObject(i));
                }

                result.add(record);
            }

        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, rs);
        }

        return result;
    }

    @Override
    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        List<T> result = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, params);

            rs = stmt.executeQuery();

            int rowNum = 0;
            while (rs.next())
                result.add(mapper.map(rs, rowNum++));

        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, rs);
        }

        return result;
    }

    @Override
    public <T> Optional<T> create(String sql, Class<T> typeOfNotCompositePrimaryKey, Object... params) {
        return create(sql, rs -> typeOfNotCompositePrimaryKey.cast(rs.getObject(1)), params);
    }

    @Override
    public <T> Optional<T> create(String sql, KeyMapper<T> compositeKeyMapper, Object... params) {
        Optional<T> key = Optional.empty();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, true, params);

            stmt.executeUpdate();

            // map key
            rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next())
                key = Optional.of(compositeKeyMapper.map(rs));

        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, rs);
        }

        return key;
    }

    @Override
    public void update(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement stmt = null;

        try {
            connection = dataSource.getConnection();
            stmt = prepareStatement(connection, sql, params);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        } finally {
            close(connection, stmt, null);
        }
    }

    /**
     * Prepare statement and fill parameters
     */
    private PreparedStatement prepareStatement(Connection connection, String sql, boolean returnKey, Object... params) throws SQLException {
        PreparedStatement statement;
        statement = returnKey ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) :
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.clearParameters();

        if (params != null) {
            int num = 0;
            for (Object param : params)
                addParameter(++num, statement, param);
        }
        return statement;
    }

    /**
     * Prepare statement and fill parameters. Statement doesn't return key
     */
    private PreparedStatement prepareStatement(Connection connection, String sql, Object... params) throws SQLException {
        return prepareStatement(connection, sql, false, params);
    }

    /**
     * Close all JDBC object
     * Because it's a good practice to always close ResultSet and Statement explicitly and not to rely on Connection.close.
     */
    private void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null)
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Close result set error", e);
            }

        if (statement != null)
            try {
                statement.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Close statement error", e);
            }

        if (connection != null)
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Close connection error", e);
            }
    }

    /**
     * Add parameter to statement
     */
    private void addParameter(int numberOfParam, PreparedStatement statement, Object paramValue) throws SQLException {

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
        }

        statement.setObject(numberOfParam, paramValue);
    }
}
