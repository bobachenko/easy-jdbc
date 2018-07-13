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

    //TODO Maybe it'l be better to use another logger later.
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

    @Override
    public <T> Optional<T> queryScalar(String sql, Class<T> type, Object... params) {
        return exec((con, st, rs) -> {
            st = prepareStatement(con, sql, params);
            rs = st.executeQuery();

            if (rs.next())
                return Optional.of(type.cast(rs.getObject(1)));

            return Optional.empty();
        });
    }

    @Override
    public <T> Optional<T> queryObject(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        return exec((con, st, rs) -> {
            st = prepareStatement(con, sql, params);
            rs = st.executeQuery();

            if (rs.next())
                return Optional.of(mapper.map(rs, 0));

            return Optional.empty();
        });
    }

    @Override
    public List<Map<String, Object>> queryAssoc(String sql, Object... params) {

        return exec((con, st, rs) -> {
            List<Map<String, Object>> result = new ArrayList<>();

            st = prepareStatement(con, sql, params);

            rs = st.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();

            while (rs.next()) {
                Map<String, Object> record = new TreeMap<>();

                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.putIfAbsent(metaData.getColumnName(i), rs.getObject(i));
                }

                result.add(record);
            }

            return result;
        });
    }

    @Override
    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        if (mapper == null)
            throw new IllegalArgumentException("RowMapper cannot be null.");

        return exec((con, st, rs) -> {
            List<T> result = new ArrayList<>();
            st = prepareStatement(con, sql, params);

            rs = st.executeQuery();

            int rowNum = 0;
            while (rs.next())
                result.add(mapper.map(rs, rowNum++));

            return result;
        });
    }

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

    @Override
    public <T> Optional<T> create(String sql, Class<T> typeOfNotCompositePrimaryKey, Object... params) {
        KeyMapper<T> compositeKeyMapper = null;
        if (typeOfNotCompositePrimaryKey != null)
            compositeKeyMapper = rs -> typeOfNotCompositePrimaryKey.cast(rs.getObject(1));
        return create(sql, compositeKeyMapper, params);
    }


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
                addParameter(++num, statement, param);
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
    private void addParameter(int numberOfParam, PreparedStatement statement,
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
        }

        // If it's another type, we have to rely on JDBC
        statement.setObject(numberOfParam, paramValue);
    }
}
