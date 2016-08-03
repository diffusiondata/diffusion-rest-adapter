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

package com.pushtechnology.adapters.rest.model.conversion;

import static java.util.stream.Collectors.toList;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.session.SessionAttributes;

import net.jcip.annotations.Immutable;

/**
 * Converter between different version 10 of the model and version 11.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class V10Converter
        extends AbstractModelConverter<com.pushtechnology.adapters.rest.model.v10.Model, Model> {
    /**
     * The converter.
     */
    public static final V10Converter INSTANCE = new V10Converter();

    private V10Converter() {
        super(LatestConverter.INSTANCE, com.pushtechnology.adapters.rest.model.v10.Model.class);
    }

    @Override
    protected Model convertFrom(com.pushtechnology.adapters.rest.model.v10.Model model) {
        return Model
            .builder()
            .active(true)
            .services(model
                .getServices()
                .stream()
                .map(oldService -> ServiceConfig
                    .builder()
                    .host(oldService.getHost())
                    .port(oldService.getPort())
                    .secure(oldService.isSecure())
                    .endpoints(oldService
                        .getEndpoints()
                        .stream()
                        .map(oldEndpoint -> EndpointConfig
                            .builder()
                            .name(oldEndpoint.getName())
                            .url(oldEndpoint.getUrl())
                            .topic(oldEndpoint.getTopic())
                            .produces("json")
                            .build())
                        .collect(toList()))
                    .pollPeriod(oldService.getPollPeriod())
                    .topicRoot(oldService.getTopicRoot())
                    .security(SecurityConfig
                        .builder()
                        .basic(oldService.getSecurity().getBasic() == null ?
                            null :
                            BasicAuthenticationConfig
                                .builder()
                                .principal(oldService.getSecurity().getBasic().getPrincipal())
                                .credential(oldService.getSecurity().getBasic().getCredential())
                                .build())
                        .build())
                    .build())
                .collect(toList()))
            .diffusion(DiffusionConfig
                .builder()
                .host(model.getDiffusion().getHost())
                .port(model.getDiffusion().getPort())
                .principal(model.getDiffusion().getPrincipal())
                .password(model.getDiffusion().getPassword())
                .secure(model.getDiffusion().isSecure())
                .connectionTimeout(SessionAttributes.DEFAULT_CONNECTION_TIMEOUT)
                .reconnectionTimeout(SessionAttributes.DEFAULT_RECONNECTION_TIMEOUT)
                .maximumMessageSize(SessionAttributes.DEFAULT_MAXIMUM_MESSAGE_SIZE)
                .inputBufferSize(SessionAttributes.DEFAULT_INPUT_BUFFER_SIZE)
                .outputBufferSize(SessionAttributes.DEFAULT_OUTPUT_BUFFER_SIZE)
                .recoveryBufferSize(SessionAttributes.DEFAULT_RECOVERY_BUFFER_SIZE)
                .build())
            .truststore(model.getTruststore())
            .build();
    }
}
