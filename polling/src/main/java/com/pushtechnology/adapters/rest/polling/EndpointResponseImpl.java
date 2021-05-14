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

package com.pushtechnology.adapters.rest.polling;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;

import net.jcip.annotations.Immutable;

/**
 * Implementation of {@link EndpointResponse} for {@link HttpResponse}.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class EndpointResponseImpl implements EndpointResponse {
    private final HttpResponse<byte[]> httpResponse;
    private final byte[] content;

    /**
     * Factory method.
     */
    public static EndpointResponse create(HttpResponse<byte[]> httpResponse) throws IOException {
        return new EndpointResponseImpl(httpResponse, httpResponse.body());
    }

    private EndpointResponseImpl(HttpResponse<byte[]> httpResponse, byte[] content) {
        this.httpResponse = httpResponse;
        this.content = content;
    }

    @Override
    public int getStatusCode() {
        return httpResponse.statusCode();
    }

    @Override
    public String getHeader(String headerName) {
        return httpResponse.headers().firstValue(headerName).orElse(null);
    }

    @Override
    public byte[] getResponse() {
        return Arrays.copyOf(content, content.length);
    }

    @Override
    public int getResponseLength() {
        return content.length;
    }
}
