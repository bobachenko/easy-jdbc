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
import java.util.Optional;

/**
 * {@code ResultMapper} is an interface to map a result of sql operation to a java object.
 *
 * @author Maxim Bobachenko
 */
@FunctionalInterface
public interface ResultMapper<T> {
    /**
     * <p>Implement this method to map whole ResultSet.</p>
     *
     * You have to call {@code ResultSet.next()} method manually for get and return the data as you want.
     *
     * @param resultSet the ResultSet to map
     * @return the result object for the ResultSet
     * @throws SQLException
     */
    Optional<T> map(ResultSet resultSet) throws SQLException;
}
