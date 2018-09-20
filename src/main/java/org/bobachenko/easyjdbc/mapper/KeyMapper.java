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

package org.bobachenko.easyjdbc.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@code KeyMapper} is an interface used by {@code EasyJdbc} for mapping
 * a value of a composite primary key to a result object.
 *
 * @author Maxim Bobachenko
 */
@FunctionalInterface
public interface KeyMapper<T>  {
    /**
     * Implement this method to map a value of a primary key.
     *
     * @param resultSet the ResultSet to map. It has data of the primary key.
     * @return the result object for the primary key
     * @throws SQLException if it's happens e.g. during getting column values.
     * Don't catch this exception, because it'll be caught by {@code EasyJdbc}
     */
    T map(ResultSet resultSet) throws SQLException;
}
