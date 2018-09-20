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

import javax.sql.DataSource;
import java.util.function.Consumer;

/**
 * Transactions support
 * Use one object for one transaction, therefore call method of() for each transaction
 * @author Maxim Bobachenko
 */
public interface EasyTransaction {

    /**
     * Run the current transaction
     * @param transactionConsumer a consumer that has a EasyJdbc object to use it for execute your queries
     * @return
     */
    EasyTransaction run(Consumer<EasyJdbc> transactionConsumer);

    /**
     * Commit the current transaction
     */
    void commit();

    /**
     * Factory method to create instance of transaction
     * @param dataSource instance of DataSource class
     */
    static EasyTransaction of(DataSource dataSource) {
        return new EasyTransactionImpl(new TransactionalConnectionManager(dataSource));
    }

    /**
     * Factory method to create instance of transaction
     * @param dataSource instance of DataSource class
     * @param isolationLevel isolation level
     */
    static EasyTransaction of(DataSource dataSource, IsolationLevel isolationLevel) {
        return new EasyTransactionImpl(new TransactionalConnectionManager(dataSource, isolationLevel));
    }
}
