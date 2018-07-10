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
    <T> Optional<T> queryScalar(String sql, Class<T> typeOfReturnValue, Object... params);
    <T> Optional<T> queryObject(String sql, RowMapper<T> mapper, Object... params);
    List<Map<String, Object>> queryAssoc(String sql, Object... params);
    <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params);
    <T> Optional<T> create(String sql, Class<T> typeOfNotCompositePrimaryKey, Object... params);
    <T> Optional<T> create(String sql, KeyMapper<T> compositeKeyMapper, Object... params);
    int update(String sql, Object... params);

    static EasyJdbcImpl of(DataSource dataSource) {
        return new EasyJdbcImpl(new DataSourceConnectionManager(dataSource));
    }
    static EasyJdbcImpl of(Connection connection) {
        return new EasyJdbcImpl( new ExternalConnectionManager(connection));
    }
}
