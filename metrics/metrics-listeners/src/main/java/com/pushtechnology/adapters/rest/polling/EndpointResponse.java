/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.polling;

import java.io.IOException;

/**
 * Response to a request to an endpoint.
 * @author Push Technology Limited
 */
public interface EndpointResponse {
    /**
     * @return the HTTP status code of the response
     */
    int getStatusCode();

    /**
     * @return an HTTP response header value
     */
    String getHeader(String headerName);

    /**
     * @return the response body
     */
    byte[] getResponse() throws IOException;
}
