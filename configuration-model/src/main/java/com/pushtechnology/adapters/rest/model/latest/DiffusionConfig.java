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

import com.pushtechnology.diffusion.client.session.SessionAttributes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * Diffusion configuration. Version 13.
 * <p>
 * Description of a Diffusion server to publish to.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
@ToString(of = {"host", "port", "secure", "principal", "connectionTimeout", "reconnectionTimeout",
    "maximumMessageSize", "inputBufferSize", "outputBufferSize", "recoveryBufferSize"})
public class DiffusionConfig {
    /**
     * The host of the Diffusion server.
     */
    @NonNull
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
    @Default
    int connectionTimeout = SessionAttributes.DEFAULT_CONNECTION_TIMEOUT;
    /**
     * The Diffusion session reconnection timeout.
     */
    @Default
    int reconnectionTimeout = SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT;
    /**
     * The Diffusion session maximum message size.
     */
    @Default
    int maximumMessageSize = SessionAttributes.DEFAULT_MAXIMUM_MESSAGE_SIZE;
    /**
     * The Diffusion session input buffer size.
     */
    @Default
    int inputBufferSize = SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE;
    /**
     * The Diffusion session output buffer size.
     */
    @Default
    int outputBufferSize = SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE;
    /**
     * The Diffusion session recovery buffer size.
     */
    @Default
    int recoveryBufferSize = SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE;
}
