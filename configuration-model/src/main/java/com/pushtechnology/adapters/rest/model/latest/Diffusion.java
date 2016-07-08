package com.pushtechnology.adapters.rest.model.latest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Diffusion configuration. Version 4.
 * <p>
 * Description of a Diffusion server to publish to.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Diffusion {
    /**
     * The host of the Diffusion server.
     */
    String host;
    /**
     * The port the Diffusion server listens on.
     */
    int port;
    /**
     * The principal. Can be {@code null}.
     */
    String principal;
    /**
     * The password. Can be {@code null}.
     */
    String password;
}
