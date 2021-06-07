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

package com.pushtechnology.adapters.rest.model.conversion.v13;

import com.pushtechnology.adapters.rest.model.conversion.Converter;
import com.pushtechnology.adapters.rest.model.v14.ServiceConfig;

/**
 * Converter between different version 13 of the model and version 14.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ServiceConfigConverter
        implements Converter<com.pushtechnology.adapters.rest.model.v13.ServiceConfig, ServiceConfig> {
    /**
     * The converter.
     */
    public static final ServiceConfigConverter INSTANCE = new ServiceConfigConverter();

    private ServiceConfigConverter() {
    }

    @Override
    public ServiceConfig convert(com.pushtechnology.adapters.rest.model.v13.ServiceConfig serviceConfig) {
        return ServiceConfig
            .builder()
            .name(serviceConfig.getName())
            .host(serviceConfig.getHost())
            .port(serviceConfig.getPort())
            .secure(serviceConfig.isSecure())
            .endpoints(serviceConfig.getEndpoints())
            .pollPeriod(serviceConfig.getPollPeriod())
            .topicPathRoot(serviceConfig.getTopicPathRoot())
            .security(SecurityConfigConverter.INSTANCE.convert(serviceConfig.getSecurity()))
            .build();
    }
}
