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

import org.bobachenko.easyjdbc.datasource.EasyDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;

/**
 * Base class for h2 tests
 */
public class EasyJdbcTest {
    /*
                simple model
     */
    static class Person {
        int id;
        String name;
        Date birthday;
        double salary;
        Date lastLogin;
        byte[] picture;

        Person(int id, String name, Date birthday, double salary, Date lastLogin, byte[] picture) {
            this.id = id;
            this.name = name;
            this.birthday = birthday;
            this.salary = salary;
            this.lastLogin = lastLogin;
            this.picture = picture;
        }

        static Person map(ResultSet rs, int num) throws SQLException {
            return new Person(rs.getInt("id"), rs.getString("name"),
                    rs.getTimestamp("birthday"), rs.getDouble("salary"),
                    rs.getTimestamp("lastLogin"), rs.getBytes("picture"));
        }
    }

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";

    private static final String CREATE_PERSON_QUERY = "CREATE TABLE PERSON(id INT PRIMARY KEY auto_increment, "
            + "name VARCHAR(255), birthday DATE, salary DOUBLE, lastLogin TIMESTAMP, picture BLOB);";
    private static final String DELETE_PERSON_QUERY = "DELETE FROM PERSON;";
    private static final String INSERT_PERSON_QUERY = "INSERT INTO PERSON "
            + "(id, name, birthday, salary, lastLogin) VALUES (?, ?, ?, ?, ?);";
    private static final String CREATE_RELATIVES_QUERY = "CREATE TABLE TWO_GENERATED_KYES_TABLE "
            + "(id1 INT auto_increment, id2 INT auto_increment, name VARCHAR(255)); "
            + "ALTER TABLE TWO_GENERATED_KYES_TABLE ADD PRIMARY KEY (id1, id2);";

    private static final String DROP_TABLE_PERSON = "DROP TABLE PERSON;";
    private static final String DROP_TABLE_TWO_GENERATED_KYES_TABLE = "DROP TABLE TWO_GENERATED_KYES_TABLE;";

    static DataSource dataSource;
    static EasyJdbc jdbc;

    @BeforeAll
    protected static void init() throws SQLException {
        dataSource = EasyDataSource.of(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

        Connection connection = dataSource.getConnection();

        Statement personStmt = connection.createStatement();
        personStmt.execute(CREATE_PERSON_QUERY);

        Statement compositeStmt = connection.createStatement();
        compositeStmt.execute(CREATE_RELATIVES_QUERY);

        personStmt.close();
        compositeStmt.close();
        connection.close();
    }

    @AfterAll
    protected static void afterAll() throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement personStmt = connection.createStatement();
        personStmt.execute(DROP_TABLE_PERSON);

        Statement compositeStmt = connection.createStatement();
        compositeStmt.execute(DROP_TABLE_TWO_GENERATED_KYES_TABLE);

        personStmt.close();
        compositeStmt.close();
        connection.close();
    }

    protected void beforeEachTest() throws SQLException {
        Connection connection = dataSource.getConnection();

        PreparedStatement clearStmt = connection.prepareStatement(DELETE_PERSON_QUERY);
        clearStmt.execute();
        clearStmt.close();

        PreparedStatement statement = connection.prepareStatement(INSERT_PERSON_QUERY);

        for (int i = 1; i <= 10; i++) {
            statement.setInt(1, i);
            statement.setString(2, String.format("Person %d", i));
            statement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            statement.setDouble(4, 999.0 * i);
            statement.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.execute();
        }

        statement.close();
        connection.close();

        jdbc = EasyJdbc.of(dataSource);
    }
}
