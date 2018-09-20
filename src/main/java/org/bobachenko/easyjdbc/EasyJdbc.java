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

import org.bobachenko.easyjdbc.mapper.KeyMapper;
import org.bobachenko.easyjdbc.mapper.ResultMapper;
import org.bobachenko.easyjdbc.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface to use JDBC easily.
 *
 * @author Maxim Bobachenko
 */
public interface EasyJdbc {

    /**
     * Executes a common jdbc operation.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query
     * @param params parameters for the query with correspondent types, if it's needed
     * @return Optional with user's object inside. It depends on implementation of the mapper.
     */
    <T> Optional<T> queryResult(String sql, ResultMapper<T> mapper, Object... params);

    /**
     * Executes a query and returns scalar value with a given type.
     * @param sql a query with one field. If field isn't one this method returns first field.
     * @param typeOfReturnValue Type to cast.
     *                          ClassCastException is possible if types of filed and this parameter are not appropriate.
     * @param params parameters for the query in it's needed
     * @return value if it exists
     */
    <T> Optional<T> queryScalar(String sql, Class<T> typeOfReturnValue, Object... params);

    /**
     * Executes a query and creates an object that has a data of the single row of the result.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query
     * @param params  parameters for the query with correspondent types, if it's needed
     * @return Optional with user's object inside. It depends on implementation of the mapper.
     */
    <T> Optional<T> queryObject(String sql, RowMapper<T> mapper, Object... params);

    /**
     * Executes a query and creates a list of maps that have a data of all rows of the result.
     * Every map in the list contains string keys with the names like columns before keyword FROM in the query.
     * @param sql a query to execute
     * @param params parameters for the query with correspondent types, if it's needed
     */
    List<Map<String, Object>> queryAssoc(String sql, Object... params);

    /**
     * Executes a query and creates a list of object that have a data of all rows of the result.
     * @param sql a query to execute
     * @param mapper class or lambda to map a result of query. It's called for every rows in ResultSet
     * @param params parameters for the query with correspondent types, if it's needed
     */
    <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params);

    /**
     * Creates a new row by "INSERT" statement and returns value of a primary key
     * @param sql a query with the INSERT keyword to execute
     * @param typeOfNotCompositePrimaryKey type of primary key
     * @param params parameters for the query for insert
     * @return value of primary key
     */
    <T> Optional<T> create(String sql, Class<T> typeOfNotCompositePrimaryKey, Object... params);

    /**
     * Creates a new row by "INSERT" statement and returns an object that contains the data of the composite primary key
     * @param sql a query with the INSERT keyword to execute
     * @param compositeKeyMapper class or lambda to map the value of the composite key
     * @param params parameters for the query for insert
     * @return object that contains a data of the composite primary key
     */
    <T> Optional<T> create(String sql, KeyMapper<T> compositeKeyMapper, Object... params);

    /**
     * Executes a query to modify the data by the "UPDATE" or "DELETE" keywords.
     * @param sql a query to execute
     * @param params parameters for the query
     * @return the number of rows affected
     */
    int update(String sql, Object... params);

    /**
     * Constructs an instance of EasyJdbc
     * @return implementation of EasyJdbc
     */
    static EasyJdbcImpl of(DataSource dataSource) {
        return new EasyJdbcImpl(new DataSourceConnectionManager(dataSource));
    }

    /**
     * Constructs an instance of EasyJdbc
     * @return implementation of EasyJdbc
     */
    static EasyJdbcImpl of(Connection connection) {
        return new EasyJdbcImpl( new ExternalConnectionManager(connection));
    }
}
