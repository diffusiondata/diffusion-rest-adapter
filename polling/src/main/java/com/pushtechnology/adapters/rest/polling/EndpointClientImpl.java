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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
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
    private volatile HttpClient client;

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
    public CompletableFuture<EndpointResponse> request(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {

        if (client == null) {
            throw new IllegalStateException("Client not running");
        }

        final PollCompletionListener completionListener = pollListener.onPollRequest(serviceConfig, endpointConfig);

        final CompletableFuture<EndpointResponse> result = new CompletableFuture<>();

        final URI uri;
        try {
            uri = new URI(
                serviceConfig.isSecure() ? "https" : "http",
                null,
                serviceConfig.getHost(),
                serviceConfig.getPort(),
                endpointConfig.getUrl(),
                null,
                null);
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad URI", e);
        }

        client.sendAsync(
            HttpRequest.newBuilder().GET().uri(uri).build(),
            HttpResponse.BodyHandlers.ofByteArray())
        .thenAccept(httpResponse -> {
            final int statusCode = httpResponse.statusCode();
            if (statusCode >= 400) {
                result.completeExceptionally(new Exception("Received response " + statusCode));
                return;
            }

            try {
                final EndpointResponse response = EndpointResponseImpl.create(httpResponse);
                completionListener.onPollResponse(response);
                result.complete(response);
            }
            catch (IOException e) {
                completionListener.onPollFailure(e);
                result.completeExceptionally(e);
            }
        }).exceptionally(e -> {
            if (e instanceof CompletionException) {
                completionListener.onPollFailure(e.getCause());
                result.completeExceptionally(e.getCause());
            }
            else {
                completionListener.onPollFailure(e);
                result.completeExceptionally(e);
            }
            return null;
        });
        return result;
    }

    @Override
    public void start() {
        LOG.debug("Opening endpoint client");
        final HttpClient newClient = clientFactory.create(model, sslContext);
        client = newClient;
        LOG.debug("Opened endpoint client");
    }

    @Override
    public void close() {
        LOG.debug("Closing endpoint client");
        client = null;
        LOG.debug("Closed endpoint client");
    }
}
