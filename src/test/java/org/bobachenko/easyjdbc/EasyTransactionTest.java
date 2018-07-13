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

import org.bobachenko.easyjdbc.exception.EasySqlException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

class EasyTransactionTest extends EasyJdbcTest {


    @BeforeEach
    @Override
    protected void beforeEachTest() throws SQLException {
        super.beforeEachTest();
    }

    @Test
    void run_thenOk() {
        int[] id = {0};

        EasyTransaction.of(dataSource, IsolationLevel.Serializable).run(jdbc -> {

            Optional<Integer> oId =
                    jdbc.create("INSERT INTO PERSON (name, birthday, salary, lastLogin) " +
                            "VALUES ('', '2018-07-07', 1200, '2018-07-07 00:00:00');", Integer.class);

            oId.ifPresent(val -> id[0] = val);

            jdbc.update("INSERT INTO PERSON (name, birthday, salary, lastLogin) VALUES (?, ?, ?, ?);",
                    "", new Date(), 5555.0, new Date());

        }).commit();

        Optional<Person> person = jdbc.queryObject("SELECT * FROM PERSON WHERE id = ?",
                Person::map, id[0]);

        Assertions.assertTrue(person.isPresent());
    }

    @Test
    void run_thenNotOk() {
        int[] id = {0};

        Assertions.assertThrows(EasySqlException.class, () -> {
            EasyTransaction.of(dataSource).run(jdbc -> {
                Optional<Integer> oId =
                        jdbc.create("INSERT INTO PERSON (name, birthday, salary, lastLogin) VALUES ('', '', ?, ?);",
                                Integer.class,
                                "", new Date(), 5555.0, new Date());
                oId.ifPresent(val -> id[0] = val);

                Assertions.assertTrue(oId.isPresent());

                jdbc.update("INSERT INTO PERSON (nameEEEEE, birthday, salary, lastLogin) VALUES (?, ?, ?, ?);",
                        "", new Date(), 5555.0, new Date());

            }).commit();
        });

        Optional<Person> person = jdbc.queryObject("SELECT * FROM PERSON WHERE id = ?",
                Person::map, id[0]);

        Assertions.assertFalse(person.isPresent());
    }
}