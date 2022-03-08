/*
 * Copyright 2020 berni3.
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
package org.huberb.h2tools.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class ConsumerThrowingSQLExceptionTest {

    /**
     * Test of accept method, of class ConsumerThrowingSQLException.
     */
    @Test
    public void given_consumer_then_accept_of_consumer_is_invoked() throws SQLException {
        Object t = null;
        ConsumerThrowingSQLExceptionImpl instance = new ConsumerThrowingSQLExceptionImpl();
        instance.accept(t);
        assertEquals(1, instance.acceptCalledCount);
    }

    /**
     * Test of andThen method, of class ConsumerThrowingSQLException.
     */
    @Test
    public void given_consumer_after_consumer_then_after_is_called_last() throws SQLException {
        final List<String> l = new ArrayList<>();
        final ConsumerThrowingSQLException<Object> after = (v) -> {
            l.add("after:" + Boolean.TRUE);
        };
        final ConsumerThrowingSQLException<Object> instance = (v) -> {
            l.add("instance:" + Boolean.TRUE);
        };

        final ConsumerThrowingSQLException result = instance.andThen(after);
        final Object t = null;
        result.accept(t);

        assertEquals("instance:" + Boolean.TRUE, "" + l.get(0));
        assertEquals("after:" + Boolean.TRUE, "" + l.get(1));
    }

    static class ConsumerThrowingSQLExceptionImpl implements ConsumerThrowingSQLException<Object> {

        int acceptCalledCount = 0;

        @Override
        public void accept(Object object) throws SQLException {
            acceptCalledCount += 1;
        }
    }

}
