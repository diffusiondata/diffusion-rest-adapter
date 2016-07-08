package com.pushtechnology.adapters.rest.polling;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import com.pushtechnology.adapters.rest.model.v2.Endpoint;
import com.pushtechnology.adapters.rest.model.v2.Service;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Poll client for endpoints.
 * @author Push Technology Limited
 */
public final class PollClient {
    private volatile CloseableHttpAsyncClient currentClient;

    /**
     * Constructor.
     */
    public PollClient() {
    }

    /**
     * Start the client.
     */
    public void start() {
        if (currentClient != null) {
            return;
        }

        final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
            .disableCookieManagement()
            .disableAuthCaching()
            .build();
        currentClient = client;

        client.start();
    }

    /**
     * Poll an endpoint using the client.
     * @param service the service
     * @param endpoint the endpoint
     * @param callback handler for the response
     */
    public void request(Service service, Endpoint endpoint, final FutureCallback<JSON> callback) {
        currentClient.execute(
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

                        callback.completed(Diffusion.dataTypes().json().fromJsonString(new String(baos.toByteArray())));
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

    /**
     * Stop the client.
     */
    public void stop() throws IOException {
        final CloseableHttpAsyncClient client = currentClient;
        if (client == null) {
            return;
        }

        client.close();
    }
}
