/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.model.conversion;

/**
 * Converter between different versions of configuration objects.
 *
 * @param <M> the type of object this converter consumes
 * @param <N> the type of object this converter produces
 * @author Push Technology Limited
 */
public interface Converter<M, N> {
    /**
     * Convert an object to a later version.
     * @param object The source object
     * @return The object converted to a later version
     * @throws IllegalArgumentException if the converter does not know how to convert supplied model
     */
    N convert(M object);
}
