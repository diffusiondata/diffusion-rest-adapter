package com.pushtechnology.adapters.rest.polling;

import java.io.IOException;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Poll client for endpoints.
 * @author Push Technology Limited
 */
public interface PollClient {
    /**
     * Start the client.
     */
    void start();

    /**
     * Poll an endpoint using the client.
     * @param service the service
     * @param endpoint the endpoint
     * @param callback handler for the response
     * @throws IllegalStateException if the client is not running
     */
    void request(Service service, Endpoint endpoint, FutureCallback<JSON> callback);

    /**
     * Stop the client.
     */
    void stop() throws IOException;
}
