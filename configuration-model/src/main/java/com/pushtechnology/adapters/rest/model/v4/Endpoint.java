
package com.pushtechnology.adapters.rest.model.v4;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Endpoint configuration. Version 4.
 * <p>
 * Description of a REST endpoint to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Endpoint {
    /**
     * The name of the endpoint.
     */
    String name;
    /**
     * The URL of the endpoint.
     */
    String url;
    /**
     * The topic to map the endpoint to.
     */
    String topic;
}
