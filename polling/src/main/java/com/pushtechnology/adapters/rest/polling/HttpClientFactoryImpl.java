package com.pushtechnology.adapters.rest.polling;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * Implementation of {@link HttpClientFactory}s
 *.
 * @author Push Technology Limited
 */
public final class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public CloseableHttpAsyncClient create() {
        return HttpAsyncClients.custom()
            .disableCookieManagement()
            .disableAuthCaching()
            .build();
    }
}
