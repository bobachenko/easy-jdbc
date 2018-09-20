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
 * An interface used by {@code EasyJdbc} for mapping row(s) of a
 * {@code ResultSet} to an object that represents a row of data.
 *
 * @author Maxim Bobachenko
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * <p>Implement this method to map each (or single) row of a ResultSet.</p>
     *
     * Don't call {@code ResultSet.next()} method manually. It's called by {@code EasyJdbc}.
     *
     * @param rsultSet the ResultSet to map
     * @param rowNuber the number of the row
     * @return the result object for the row
     * @throws SQLException if it's happens e.g. during getting column values.     *
     * Don't catch this exception, because it'll be caught by {@code EasyJdbc}
     */
    T map(ResultSet rsultSet, int rowNuber) throws SQLException;
}
