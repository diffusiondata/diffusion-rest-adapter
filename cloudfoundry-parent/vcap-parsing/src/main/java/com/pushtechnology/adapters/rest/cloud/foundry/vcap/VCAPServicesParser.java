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

package com.pushtechnology.adapters.rest.cloud.foundry.vcap;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parser for VCAP_SERVICES.
 *
 * @author Push Technology Limited
 */
public final class VCAPServicesParser {
    private final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

    /**
     * Parse a VCAP_SERVICES value.
     */
    public VCAPServices parse(String vcapServices) throws IOException {
        return mapper.readValue(vcapServices, VCAPServices.class);
    }
}