package com.pushtechnology.adapters.rest.model.v3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Diffusion configuration. Version 3.
 * <p>
 * Description of a Diffusion server to publish to.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Diffusion {
    String host;
    int port;
}
