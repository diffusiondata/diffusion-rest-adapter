/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.model.latest;

import java.util.List;

import com.pushtechnology.adapters.rest.model.AnyModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Configuration model. Version 13.
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
    public static final int VERSION = 13;

    /**
     * If the client should run.
     */
    private boolean active;

    /**
     * The Diffusion server.
     */
    @NonNull
    private DiffusionConfig diffusion;

    /**
     * The REST services to poll.
     */
    @NonNull
    List<ServiceConfig> services;

    /**
     * The location of the trust store.
     * <p>
     * The location will first be resolved as a classpath resource. If this
     * fails it will fallback to trying to load the store from this
     * location on the file system.
     */
    String truststore;
}
