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
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.json.JSON;

import net.jcip.annotations.ThreadSafe;

/**
 * Implementation of {@link HttpComponent}.
 * <p>
 * Synchronises on the instance for the {@link #start()} and {@link #close()} methods. Calls to the
 * {@link #request(ServiceConfig, EndpointConfig, FutureCallback)} may happen concurrently and atomically access state
 * modified by the {@link #start()} and {@link #close()} methods. The request may complete after the
 * {@link HttpComponent} is stopped.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class HttpComponentImpl implements HttpComponent {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".+; charset=(\\S+)");
    private final HttpClientFactory httpClientFactory;
    private volatile CloseableHttpAsyncClient currentClient;

    /**
     * Constructor.
     */
    public HttpComponentImpl(HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public synchronized void start() {
        if (currentClient != null) {
            return;
        }

        final CloseableHttpAsyncClient client = httpClientFactory.create();
        currentClient = client;

        client.start();
    }

    @Override
    public Future<?> request(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            final FutureCallback<JSON> callback) {
        final CloseableHttpAsyncClient client = this.currentClient;
        if (client == null) {
            throw new IllegalStateException("Client not running");
        }

        return client.execute(
            new HttpHost(serviceConfig.getHost(), serviceConfig.getPort(), "http"),
            new HttpGet(endpointConfig.getUrl()),
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
    public synchronized void close() throws IOException {
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
