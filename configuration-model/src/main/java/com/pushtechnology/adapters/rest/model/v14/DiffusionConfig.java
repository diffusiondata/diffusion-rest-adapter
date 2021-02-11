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

package com.pushtechnology.adapters.rest.model.v14;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.DEFAULT_CONNECTION_TIMEOUT;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.MAXIMUM_MESSAGE_SIZE_MIN;

import com.pushtechnology.diffusion.client.session.SessionAttributes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

/**
 * Diffusion configuration. Version 14.
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
     * The host of the Diffusion server. Defaults to the localhost.
     */
    @NonNull
    @Default
    String host = "localhost";
    /**
     * The port the Diffusion server listens on. Defaults to 8080.
     */
    @Default
    int port = 8080;
    /**
     * If a secure connection should be used. Defaults to false.
     */
    @Default
    boolean secure = false;
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
     * Defaults to {@link SessionAttributes#DEFAULT_CONNECTION_TIMEOUT}.
     */
    @Default
    int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    /**
     * The Diffusion session reconnection timeout.
     * Defaults to {@link SessionAttributes#DEFAULT_RECONNECTION_TIMEOUT}.
     */
    @Default
    int reconnectionTimeout = DEFAULT_RECONNECTION_TIMEOUT;
    /**
     * The Diffusion session maximum message size.
     * Defaults to {@link SessionAttributes#MAXIMUM_MESSAGE_SIZE_MIN}.
     */
    @Default
    int maximumMessageSize = MAXIMUM_MESSAGE_SIZE_MIN;
    /**
     * The Diffusion session input buffer size.
     * Defaults to {@link SessionAttributes#DEFAULT_INPUT_BUFFER_SIZE}.
     */
    @Default
    int inputBufferSize = DEFAULT_INPUT_BUFFER_SIZE;
    /**
     * The Diffusion session output buffer size.
     * Defaults to {@link SessionAttributes#DEFAULT_OUTPUT_BUFFER_SIZE}.
     */
    @Default
    int outputBufferSize = DEFAULT_OUTPUT_BUFFER_SIZE;
    /**
     * The Diffusion session recovery buffer size.
     * Defaults to {@link SessionAttributes#DEFAULT_RECOVERY_BUFFER_SIZE}.
     */
    @Default
    int recoveryBufferSize = DEFAULT_RECOVERY_BUFFER_SIZE;
}
