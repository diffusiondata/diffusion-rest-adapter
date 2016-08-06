/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.polling.BinaryParsingHandler;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.polling.JSONParsingHandler;
import com.pushtechnology.adapters.rest.polling.StringParsingHandler;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Factory to create parsing handlers.
 *
 * @author Push Technology Limited
 */
public final class ParsingHandlerFactory {
    /**
     * Create a handler that parses from an endpoint type and delegates to a result.
     */
    @SuppressWarnings("unchecked")
    public <T> FutureCallback<EndpointResponse> create(Class<T> type, FutureCallback<T> handler) {
        if (type.equals(JSON.class)) {
            return new StringParsingHandler(new JSONParsingHandler((FutureCallback<JSON>) handler));
        }
        else if (type.equals(Binary.class)) {
            return new BinaryParsingHandler((FutureCallback<Binary>) handler);
        }
        else if (type.equals(String.class)) {
            return new StringParsingHandler((FutureCallback<String>) handler);
        }
        else {
            throw new IllegalArgumentException("Unsupported type \"" + type + "\"");
        }
    }
}
