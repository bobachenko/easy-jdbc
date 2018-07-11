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
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Class for transaction support
 */
class EasyTransactionImpl implements EasyTransaction {

    private TransactionalConnectionManager connectionManager;

    EasyTransactionImpl(TransactionalConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public EasyTransaction run(Consumer<EasyJdbc> transactionConsumer) {

        try {
            transactionConsumer.accept(new EasyJdbcImpl(connectionManager));
        } catch (Exception e) {
            try {
                connectionManager.rollback();
            } catch (SQLException sqle) {/* sorry sorry guys... */}
            throw e;
        }

        return this;
    }

    @Override
    public void commit() {
        try {
            connectionManager.commit();
        } catch (SQLException e) {
            throw new EasySqlException(e.getMessage(), e);
        }
    }
}
