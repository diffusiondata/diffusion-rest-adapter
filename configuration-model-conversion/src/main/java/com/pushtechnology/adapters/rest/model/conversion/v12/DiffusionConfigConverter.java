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

package com.pushtechnology.adapters.rest.model.conversion.v12;

import com.pushtechnology.adapters.rest.model.conversion.Converter;
import com.pushtechnology.adapters.rest.model.v13.DiffusionConfig;
import com.pushtechnology.diffusion.client.session.SessionAttributes;

/**
 * Converter between different version 12 of the model and version 13.
 *
 * @author Push Technology Limited
 */
/*package*/ final class DiffusionConfigConverter
        implements Converter<com.pushtechnology.adapters.rest.model.v12.DiffusionConfig, DiffusionConfig> {
    /**
     * The converter.
     */
    public static final DiffusionConfigConverter INSTANCE = new DiffusionConfigConverter();

    private DiffusionConfigConverter() {
    }

    @Override
    public DiffusionConfig convert(com.pushtechnology.adapters.rest.model.v12.DiffusionConfig diffusionConfig) {
        return DiffusionConfig
            .builder()
            .host(diffusionConfig.getHost())
            .port(diffusionConfig.getPort())
            .principal(diffusionConfig.getPrincipal())
            .password(diffusionConfig.getPassword())
            .secure(diffusionConfig.isSecure())
            .connectionTimeout(SessionAttributes.DEFAULT_CONNECTION_TIMEOUT)
            .reconnectionTimeout(SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT)
            .maximumMessageSize(SessionAttributes.DEFAULT_MAXIMUM_MESSAGE_SIZE)
            .inputBufferSize(SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE)
            .outputBufferSize(SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE)
            .recoveryBufferSize(SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE)
            .build();
    }
}
