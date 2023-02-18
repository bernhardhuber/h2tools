/*
 * Copyright 2023 berni3.
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
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author berni3
 */
public class Supports {

    /**
     * Define a consumer for jdbc operations.
     *
     * @author berni3
     * @param <T>
     */
    @FunctionalInterface
    public interface ConsumerThrowingSQLException<T> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws java.sql.SQLException
         */
        void accept(T t) throws SQLException;

        /**
         * Returns a composed {@code Consumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing
         * either operation throws an exception, it is relayed to the caller of
         * the composed operation. If performing this operation throws an
         * exception, the {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code Consumer} that performs in sequence this
         * operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         */
        default ConsumerThrowingSQLException<T> andThen(ConsumerThrowingSQLException<? super T> after) {
            Objects.requireNonNull(after);
            return (T t) -> {
                accept(t);
                after.accept(t);
            };
        }

    }

    @FunctionalInterface
    public interface FunctionThrowingSQLException<T, R> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws java.sql.SQLException
         */
        R apply(T t) throws SQLException;

        /**
         * Returns a composed function that first applies the {@code before}
         * function to its input, and then applies this function to the result.
         * If evaluation of either function throws an exception, it is relayed
         * to the caller of the composed function.
         *
         * @param <V> the type of input to the {@code before} function, and to
         * the composed function
         * @param before the function to apply before this function is applied
         * @return a composed function that first applies the {@code before}
         * function and then applies this function
         * @throws NullPointerException if before is null
         *
         * @see #andThen(FunctionThrowingSQLException)
         */
        default <V> FunctionThrowingSQLException<V, R> compose(FunctionThrowingSQLException<? super V, ? extends T> before) {
            Objects.requireNonNull(before);
            return (V v) -> apply(before.apply(v));
        }

        /**
         * Returns a composed function that first applies this function to its
         * input, and then applies the {@code after} function to the result. If
         * evaluation of either function throws an exception, it is relayed to
         * the caller of the composed function.
         *
         * @param <V> the type of output of the {@code after} function, and of
         * the composed function
         * @param after the function to apply after this function is applied
         * @return a composed function that first applies this function and then
         * applies the {@code after} function
         * @throws NullPointerException if after is null
         *
         * @see #compose(FunctionThrowingSQLException)
         */
        default <V> FunctionThrowingSQLException<T, V> andThen(FunctionThrowingSQLException<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t) -> after.apply(apply(t));
        }

        /**
         * Returns a function that always returns its input argument.
         *
         * @param <T> the type of the input and output objects to the function
         * @return a function that always returns its input argument
         */
        static <T> FunctionThrowingSQLException<T, T> identity() {
            return t -> t;
        }
    }

    public static class Holder<T> {

        private T t;

        public Holder() {
            this(null);
        }

        public Holder(T initial) {
            this.t = t;
        }

        public T get() {
            return t;
        }

        public void set(T t) {
            this.t = t;
        }
    }
}
