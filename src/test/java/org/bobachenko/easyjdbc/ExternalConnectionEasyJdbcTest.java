/*
Copyright (c) 2018 Maxim Bobachenko  Contacts: <max@bobachenko.org>

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

class ExternalConnectionEasyJdbcTest extends Test {

    private static Connection externalConnection;

    @BeforeEach
    @Override
    protected void beforeEachTest() throws SQLException {
        super.beforeEachTest();
        externalConnection = dataSource.getConnection();
        jdbc = EasyJdbc.of(externalConnection);
    }

    @AfterEach
    void closeConnection() throws SQLException {
        if(externalConnection!=null)
            externalConnection.close();
    }

    @org.junit.jupiter.api.Test
    void whenCreateInstanceWithClosedConnection_thenIllegalStateExteption() throws SQLException {
        externalConnection.close();
        Assertions.assertThrows(IllegalStateException.class, () -> {
            EasyJdbc.of(externalConnection);
        });
    }

    @org.junit.jupiter.api.Test
    void queryScalar_thenReturnInt() {
        Optional<Integer> id = jdbc.queryScalar("SELECT id FROM PERSON WHERE name = ?",
                Integer.class, "Person 1");
        Assertions.assertTrue(id.isPresent() && id.get() == 1);
    }

    @org.junit.jupiter.api.Test
    void queryScalar_thenReturnLong() {
        Optional<Long> count = jdbc.queryScalar("SELECT COUNT(id) FROM PERSON WHERE name = ?",
                Long.class, "Person 1");
        Assertions.assertTrue(count.isPresent() && count.get() == 1);
    }

    @org.junit.jupiter.api.Test
    void queryScalar_thenReturnString() {
        Optional<String> name = jdbc.queryScalar("SELECT name FROM PERSON WHERE id = ?",
                String.class, 1);
        Assertions.assertTrue(name.isPresent() && name.get().equals("Person 1"));
    }

    @org.junit.jupiter.api.Test
    void queryScalar_thenReturnTimestamp() {
        Optional<Date> date = jdbc.queryScalar("SELECT birthday FROM PERSON WHERE id = ?",
                Date.class, 1);
        Assertions.assertTrue(date.isPresent());
    }

    @org.junit.jupiter.api.Test
    void queryObject() {
        Optional<Person> person = jdbc.queryObject("SELECT * FROM PERSON WHERE id = ?",
                Person::map, 1);
        Assertions.assertTrue(person.isPresent() && person.get().name.equals("Person 1"));
    }

    @org.junit.jupiter.api.Test
    void queryAssoc() {
        List<Map<String, Object>> assocList = jdbc.queryAssoc("SELECT * FROM PERSON WHERE id < ?", 4);
        Assertions.assertEquals(assocList.size(), 3);
    }

    @org.junit.jupiter.api.Test
    void queryList() {
        List<Person> list = jdbc.queryList("SELECT * FROM PERSON WHERE id < ?", Person::map, 4);
        Assertions.assertEquals(list.size(), 3);
    }

    @org.junit.jupiter.api.Test
    void create_thenReturnSimpleKey() {
        Optional<Integer> key = jdbc.create("INSERT INTO PERSON (name, birthday, salary, lastLogin) VALUES (?, ?, ?, ?);",
                Integer.class, "New persion", new Date(), 5555.0, new Date());
        Assertions.assertTrue(key.isPresent() && key.get() == 11);

    }

    @org.junit.jupiter.api.Test
    void create_thenReturnCompositeKey() {
        Optional<AbstractMap.SimpleEntry<Integer, Integer>> key =
                jdbc.create("INSERT INTO TWO_GENERATED_KYES_TABLE (name) VALUES (?);",
                        rs -> new AbstractMap.SimpleEntry<>(rs.getInt("id1"), rs.getInt("id2")),
                        "some value");
        Assertions.assertTrue(key.isPresent() && key.get().getKey() == 1 && key.get().getValue() == 1);
    }

    @org.junit.jupiter.api.Test
    void update() {
        Optional<Integer> key = jdbc.create("INSERT INTO PERSON (name, birthday, salary, lastLogin) VALUES (?, ?, ?, ?);",
                Integer.class, "new name", new Date(), 5555.0, null);

        key.ifPresent(k->jdbc.update("UPDATE PERSON SET name = ?, picture = ? WHERE id = ?", "", new byte[] {0x00}, k));

        Optional<Person> person = jdbc.queryObject("SELECT * FROM PERSON WHERE id = ?",
                Person::map, key.orElse(0));

        Assertions.assertTrue(person.isPresent() && person.get().name.equals(""));
    }
}