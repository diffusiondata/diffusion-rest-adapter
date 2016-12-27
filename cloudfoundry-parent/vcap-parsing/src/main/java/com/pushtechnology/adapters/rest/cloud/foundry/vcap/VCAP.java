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

/**
 * Value object for the VCAP_SERVICE environmental variable.
 *
 * @author Push Technology Limited
 */
public final class VCAP {
    private static final VCAPServicesParser PARSER = new VCAPServicesParser();

    private VCAP() {
    }

    /**
     * @return the port used for health checks
     */
    public static int getPort() {
        final String port = System.getenv("PORT");

        return port != null ? Integer.parseInt(port) : 3000;
    }

    /**
     * @return the parsed VCAP services
     */
    public static VCAPServices getServices() throws IOException {
        return PARSER.parse(System.getenv("VCAP_SERVICES"));
    }

}
