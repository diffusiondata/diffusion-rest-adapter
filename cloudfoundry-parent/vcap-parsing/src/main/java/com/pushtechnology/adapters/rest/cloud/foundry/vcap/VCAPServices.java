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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object for the VCAP_SERVICE environmental variable.
 *
 * @author Push Technology Limited
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class VCAPServices {
    private final ServiceEntry<DiffusionCloudCredentials> diffusionCloud;

    /**
     * Constructor.
     */
    public VCAPServices(@JsonProperty("push-reappt") List<ServiceEntry<DiffusionCloudCredentials>> diffusionClouds) {
        if (diffusionClouds.size() < 1) {
            throw new IllegalArgumentException("The push-reappt key should contain a non-empty list");
        }
        diffusionCloud = diffusionClouds.get(0);
    }

    /**
     * @return the Diffusion cloud services
     */
    public ServiceEntry<DiffusionCloudCredentials> getDiffusionCloud() {
        return diffusionCloud;
    }
}
