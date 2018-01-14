/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.adapter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A deferred function that may complete asynchronously.
 *
 * @author Matt Champion 14/01/2018
 */
public interface AsyncFunction<T, U> extends Function<T, CompletableFuture<U>> {
    /**
     * Create an asyn function from an exceptional function.
     */
    static <T, U, E extends Exception> AsyncFunction<T, U> create(ExceptionalFunction<T, U, E> func) {
        return value -> {
            try {
                return CompletableFuture.completedFuture(func.apply(value));
            }
            catch (Exception e) {
                final CompletableFuture<U> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        };
    }

    /**
     * A function that can fail to be applied.
     */
    interface ExceptionalFunction<T, U, E extends Exception> {
        /**
         * Apply the function.
         * @param value the value to apply the function to
         * @return the result
         * @throws E an exception if the function cannot be applied
         */
        U apply(T value) throws E;
    }
}
