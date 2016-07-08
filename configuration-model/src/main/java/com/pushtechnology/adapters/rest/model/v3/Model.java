package com.pushtechnology.adapters.rest.model.v3;

import java.util.List;

import com.pushtechnology.adapters.rest.model.AnyModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Configuration model. Version 3.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Model implements AnyModel {
    /**
     * The version of the model.
     */
    public static final int VERSION = 3;

    /**
     * The Diffusion server.
     */
    private Diffusion diffusion;

    /**
     * The REST services to poll.
     */
    List<Service> services;
}
