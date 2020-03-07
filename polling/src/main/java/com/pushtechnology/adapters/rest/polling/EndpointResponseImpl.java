/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import net.jcip.annotations.ThreadSafe;

/**
 * Implementation of {@link EndpointResponse} for {@link HttpResponse}.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class EndpointResponseImpl implements EndpointResponse {
    private final HttpResponse httpResponse;
    private volatile byte[] content = null;

    /**
     * Constructor.
     */
    public EndpointResponseImpl(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public int getStatusCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public String getHeader(String headerName) {
        return httpResponse.getFirstHeader(headerName).getValue();
    }

    @Override
    public byte[] getResponse() throws IOException {
        final byte[] currentContent = content;

        if (currentContent != null) {
            return currentContent;
        }
        else {
            synchronized (this) {
                if (content == null) {
                    final HttpEntity entity = httpResponse.getEntity();

                    final InputStream contentStream = entity.getContent();
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    int next = contentStream.read();
                    while (next != -1) {
                        baos.write(next);
                        next = contentStream.read();
                    }

                    final byte[] newContent = baos.toByteArray();
                    content = newContent;
                    return newContent;
                }
                else {
                    return content;
                }
            }
        }
    }
}
