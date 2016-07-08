package com.pushtechnology.adapters.rest.model.v2;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Service configuration. Version 2.
 * <p>
 * Description of a REST service to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Service {
    /**
     * The host of the service.
     */
    String host;

    /**
     * The port to connect to.
     */
    int port;

    /**
     * The endpoints the service makes available.
     */
    List<Endpoint> endpoints;

    /**
     * The time in milliseconds between polls.
     */
    long pollPeriod;
}
