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

import java.io.IOException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.PollListener;
import com.pushtechnology.adapters.rest.metrics.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

import net.jcip.annotations.ThreadSafe;

/**
 * Implementation of {@link EndpointClient}.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class EndpointClientImpl implements EndpointClient {
    private static final Logger LOG = LoggerFactory.getLogger(EndpointClientImpl.class);
    private final Model model;
    private final SSLContext sslContext;
    private final HttpClientFactory clientFactory;
    private final PollListener pollListener;
    private volatile CloseableHttpAsyncClient client;

    /**
     * Constructor.
     */
    public EndpointClientImpl(
            Model model,
            SSLContext sslContext,
            HttpClientFactory clientFactory,
            PollListener pollListener) {
        this.model = model;
        this.sslContext = sslContext;
        this.clientFactory = clientFactory;
        this.pollListener = pollListener;
    }

    @Override
    public Future<?> request(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig,
            final FutureCallback<EndpointResponse> callback) {

        if (client == null) {
            throw new IllegalStateException("Client not running");
        }

        final PollCompletionListener completionListener = pollListener.onPollRequest(serviceConfig, endpointConfig);

        return client.execute(
            new HttpHost(serviceConfig.getHost(), serviceConfig.getPort(), serviceConfig.isSecure() ? "https" : "http"),
            new HttpGet(endpointConfig.getUrl()),
            new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse httpResponse) {
                    final StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() >= 400) {
                        callback.failed(new Exception("Received response " + statusLine));
                        return;
                    }

                    completionListener.onPollResponse(httpResponse);
                    callback.completed(new EndpointResponseImpl(httpResponse));
                }

                @Override
                public void failed(Exception e) {
                    completionListener.onPollFailure(e);
                    callback.failed(e);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                }
            });
    }

    @PostConstruct
    @Override
    public void start() {
        LOG.debug("Opening endpoint client");
        final CloseableHttpAsyncClient newClient = clientFactory.create(model, sslContext);
        newClient.start();
        client = newClient;
        LOG.debug("Opened endpoint client");
    }

    @PreDestroy
    @Override
    public void close() {
        LOG.debug("Closing endpoint client");
        try {
            client.close();
        }
        catch (IOException e) {
            // The implementation does not throw an IOException
            throw new IllegalStateException(e);
        }
        LOG.debug("Closed endpoint client");
    }
}
