/*
 * Copyright 2021 pi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.h2tools.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 *
 * @author pi
 */
public class ResultSetIterableIterators {

    static class ResultSetIterable implements Iterable<ResultSet> {

        final ResultSet resultSet;

        ResultSetIterable(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public Iterator<ResultSet> iterator() {
            return new ResultSetIteratorImpl(resultSet);
        }

    }

    static class ResultSetIteratorImpl implements Iterator<ResultSet> {

        final ResultSet resultSet;

        ResultSetIteratorImpl(ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @Override
        public boolean hasNext() {
            try {
                final boolean hasNext = resultSet.next();
                return hasNext;
            } catch (SQLException sqlex) {
                throw new ResultSetRunTimeException(sqlex);
            }
        }

        @Override
        public ResultSet next() {
            return resultSet;
        }

    }

    static class ResultSetRunTimeException extends RuntimeException {

        private final SQLException sqlex;

        ResultSetRunTimeException(SQLException sqlex) {
            super(sqlex);
            this.sqlex = sqlex;
        }

        SQLException sqlException() {
            return this.sqlex;
        }

    }

}
