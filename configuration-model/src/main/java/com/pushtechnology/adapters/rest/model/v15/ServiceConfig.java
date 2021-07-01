/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.model.v15;

import static java.util.Collections.emptyList;

import java.util.List;

import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Service configuration. Version 15.
 * <p>
 * Description of a REST service to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class ServiceConfig {
    /**
     * The name of the service.
     */
    @NonNull
    String name;

    /**
     * The host of the service.
     */
    @NonNull
    String host;

    /**
     * The port to connect to. Defaults to 443.
     */
    @Builder.Default
    int port = 443;

    /**
     * If a secure transport should be used. Defaults to true.
     */
    @Builder.Default
    boolean secure = true;

    /**
     * The endpoints the service makes available. Defaults to an empty list.
     */
    @NonNull
    @Builder.Default
    List<EndpointConfig> endpoints = emptyList();

    /**
     * The time in milliseconds between polls. Defaults to 60000.
     */
    @Builder.Default
    long pollPeriod = 60000;

    /**
     * The topic path that is the root of the service.
     */
    @NonNull
    String topicPathRoot;

    /**
     * The security configuration for the service.
     */
    @Builder.Default
    SecurityConfig security = SecurityConfig.builder().build();
}
