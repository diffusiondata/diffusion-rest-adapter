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

package com.pushtechnology.adapters.rest.model.conversion.v14;

import com.pushtechnology.adapters.rest.model.conversion.Converter;
import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;

/**
 * Converter between different version 14 of the model and version 15.
 *
 * @author Push Technology Limited
 */
/*package*/ final class SecurityConfigConverter
        implements Converter<com.pushtechnology.adapters.rest.model.v14.SecurityConfig, SecurityConfig> {
    /**
     * The converter.
     */
    public static final SecurityConfigConverter INSTANCE = new SecurityConfigConverter();

    private SecurityConfigConverter() {
    }

    @Override
    public SecurityConfig convert(com.pushtechnology.adapters.rest.model.v14.SecurityConfig securityConfig) {
        if (securityConfig == null) {
            return SecurityConfig.builder().build();
        }

        return SecurityConfig
            .builder()
            .basic(securityConfig.getBasic() == null ?
                null :
                BasicAuthenticationConfig
                    .builder()
                    .userid(securityConfig.getBasic().getUserid())
                    .password(securityConfig.getBasic().getPassword())
                    .build())
            .build();
    }
}
