package com.pushtechnology.adapters.rest.polling;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

/**
 * Factory for {@link CloseableHttpAsyncClient}s
 *.
 * @author Push Technology Limited
 */
public interface HttpClientFactory {
    /**
     * @return a new HTTP client
     */
    CloseableHttpAsyncClient create();
}
