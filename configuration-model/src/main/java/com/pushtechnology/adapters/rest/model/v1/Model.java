package com.pushtechnology.adapters.rest.model.v1;

import java.util.List;

import com.pushtechnology.adapters.rest.model.AnyModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Configuration model. Version 1.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Model implements AnyModel {
    List<Service> services;
}
