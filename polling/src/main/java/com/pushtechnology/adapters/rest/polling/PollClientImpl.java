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

package com.pushtechnology.adapters.rest.polling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Implementation of {@link PollClient}.
 * @author Push Technology Limited
 */
public final class PollClientImpl implements PollClient {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".+; charset=(\\S+)");
    private final HttpClientFactory httpClientFactory;
    private volatile CloseableHttpAsyncClient currentClient;

    /**
     * Constructor.
     */
    public PollClientImpl(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public void start() {
        if (currentClient != null) {
            return;
        }

        final CloseableHttpAsyncClient client = httpClientFactory.create();
        currentClient = client;

        client.start();
    }

    @Override
    public void request(Service service, Endpoint endpoint, final FutureCallback<JSON> callback) {
        final CloseableHttpAsyncClient client = this.currentClient;
        if (client == null) {
            throw new IllegalStateException("Client not running");
        }

        client.execute(
            new HttpHost(service.getHost(), service.getPort(), "http"),
            new HttpGet(endpoint.getUrl()),
            new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse httpResponse) {
                    try {
                        final HttpEntity entity = httpResponse.getEntity();

                        final InputStream content = entity.getContent();
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        int next = content.read();
                        while (next != -1) {
                            baos.write(next);
                            next = content.read();
                        }

                        callback.completed(Diffusion
                            .dataTypes()
                            .json()
                            .fromJsonString(new String(baos.toByteArray(), getResponseCharset(httpResponse))));
                    }
                    catch (IOException e) {
                        failed(e);
                    }
                }

                @Override
                public void failed(Exception e) {
                    callback.failed(e);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }
            });
    }

    @Override
    public void stop() throws IOException {
        final CloseableHttpAsyncClient client = currentClient;
        if (client == null) {
            return;
        }

        client.close();
        currentClient = null;
    }

    private Charset getResponseCharset(HttpResponse response) {
        final Header[] headers = response.getHeaders("content-type");
        if (headers.length > 0) {
            final String contentType = headers[0].getValue();
            final Matcher matcher = CHARSET_PATTERN.matcher(contentType);

            if (matcher.matches()) {
                final String charset = matcher.group(1);
                return Charset.forName(charset);
            }
        }

        return Charset.forName("ISO-8859-1");
    }
}
