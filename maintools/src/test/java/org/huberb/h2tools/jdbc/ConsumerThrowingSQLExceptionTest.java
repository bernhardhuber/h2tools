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
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class ConsumerThrowingSQLExceptionTest {

    public ConsumerThrowingSQLExceptionTest() {
    }

    /**
     * Test of accept method, of class ConsumerThrowingSQLException.
     */
    @Test
    public void testAccept() throws SQLException {
        Object t = null;
        ConsumerThrowingSQLExceptionImpl instance = new ConsumerThrowingSQLExceptionImpl();
        instance.accept(t);
        assertEquals(1, instance.acceptCalledCount);
    }

    /**
     * Test of andThen method, of class ConsumerThrowingSQLException.
     */
    @Test
    public void testAndThen() throws SQLException {
        ConsumerThrowingSQLExceptionImpl after = new ConsumerThrowingSQLExceptionImpl();
        ConsumerThrowingSQLExceptionImpl instance = new ConsumerThrowingSQLExceptionImpl();

        ConsumerThrowingSQLException result = instance.andThen(after);

        Object t = null;
        result.accept(t);

        assertEquals(1, instance.acceptCalledCount);
        assertEquals(1, after.acceptCalledCount);
    }

    class ConsumerThrowingSQLExceptionImpl implements ConsumerThrowingSQLException<Object> {

        int acceptCalledCount = 0;

        public void accept(Object object) throws SQLException {
            acceptCalledCount += 1;
        }
    }

}
