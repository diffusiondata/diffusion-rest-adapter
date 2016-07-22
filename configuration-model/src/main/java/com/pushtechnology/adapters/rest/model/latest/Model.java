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
import lombok.Value;

/**
 * Configuration model. Version 9.
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
    public static final int VERSION = 9;

    /**
     * The Diffusion server.
     */
    private DiffusionConfig diffusion;

    /**
     * The REST services to poll.
     */
    List<ServiceConfig> services;

    /**
     * The location of the trust store.
     */
    String truststore;
}
