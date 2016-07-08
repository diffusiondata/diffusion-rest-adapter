package com.pushtechnology.adapters;

import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Publishing client to update Diffusion.
 *
 * @author Push Technology Limited
 */
public interface PublishingClient {
    /**
     * Start the client running. Connects the client to Diffusion.
     */
    void start();

    /**
     * Initialise a service to publish to.
     */
    void initialise(Service service);

    /**
     * Stop the client running.
     */
    void stop();

    /**
     * Update the topic associated with an endpoint.
     */
    void publish(Endpoint endpoint, JSON json);
}
