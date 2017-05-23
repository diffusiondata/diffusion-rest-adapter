/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.model.v13;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * Endpoint configuration. Version 13.
 * <p>
 * Description of a REST endpoint to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
@ToString(of = "name")
public class EndpointConfig {
    /**
     * The name of the endpoint.
     */
    @NonNull
    String name;
    /**
     * The URL of the endpoint.
     */
    @NonNull
    String url;
    /**
     * The topic path to map the endpoint to. It is relative to the service
     * topic path root.
     */
    @NonNull
    String topicPath;
    /**
     * The type of content produced by the endpoint.
     * <p>
     * Supports the values:
     * <ul>
     *     <li>auto</li>
     *     <li>json</li>
     *     <li>application/json</li>
     *     <li>text/json</li>
     *     <li>string</li>
     *     <li>text/plain</li>
     *     <li>binary</li>
     *     <li>application/octet-stream</li>
     * </ul>
     */
    @NonNull
    String produces;
}
