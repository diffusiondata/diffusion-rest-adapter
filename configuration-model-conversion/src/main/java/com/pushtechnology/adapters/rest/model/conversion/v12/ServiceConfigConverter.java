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

import static java.util.stream.Collectors.toList;

import com.pushtechnology.adapters.rest.model.conversion.Converter;
import com.pushtechnology.adapters.rest.model.v13.ServiceConfig;

/**
 * Converter between different version 12 of the model and version 13.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ServiceConfigConverter
        implements Converter<com.pushtechnology.adapters.rest.model.v12.ServiceConfig, ServiceConfig> {
    /**
     * The converter.
     */
    public static final ServiceConfigConverter INSTANCE = new ServiceConfigConverter();

    private ServiceConfigConverter() {
    }

    @Override
    public ServiceConfig convert(com.pushtechnology.adapters.rest.model.v12.ServiceConfig serviceConfig) {
        return ServiceConfig
            .builder()
            .name(serviceConfig.getHost() + ":" + serviceConfig.getPort() + ":" + serviceConfig.isSecure())
            .host(serviceConfig.getHost())
            .port(serviceConfig.getPort())
            .secure(serviceConfig.isSecure())
            .endpoints(serviceConfig
               .getEndpoints()
               .stream()
               .map(EndpointConfigConverter.INSTANCE::convert)
               .collect(toList()))
            .pollPeriod(serviceConfig.getPollPeriod())
            .topicPathRoot(serviceConfig.getTopicRoot())
            .security(SecurityConfigConverter.INSTANCE.convert(serviceConfig.getSecurity()))
            .build();
    }
}
