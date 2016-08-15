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

package com.pushtechnology.adapters.rest.model.v11;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Diffusion configuration. Version 11.
 * <p>
 * Description of a Diffusion server to publish to.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class DiffusionConfig {
    /**
     * The host of the Diffusion server.
     */
    String host;
    /**
     * The port the Diffusion server listens on.
     */
    int port;
    /**
     * If a secure connection should be used.
     */
    boolean secure;
    /**
     * The principal. Can be {@code null}.
     */
    String principal;
    /**
     * The password. Can be {@code null}.
     */
    String password;
    /**
     * The Diffusion session connection timeout.
     */
    int connectionTimeout;
    /**
     * The Diffusion session reconnection timeout.
     */
    int reconnectionTimeout;
    /**
     * The Diffusion session maximum message size.
     */
    int maximumMessageSize;
    /**
     * The Diffusion session input buffer size.
     */
    int inputBufferSize;
    /**
     * The Diffusion session output buffer size.
     */
    int outputBufferSize;
    /**
     * The Diffusion session recovery buffer size.
     */
    int recoveryBufferSize;
}
