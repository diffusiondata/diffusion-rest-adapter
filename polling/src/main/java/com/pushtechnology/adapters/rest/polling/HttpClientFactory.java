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

package com.pushtechnology.adapters.rest.polling;

import java.net.http.HttpClient;

import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Factory for {@link HttpClient}.
 * @author Push Technology Limited
 */
public interface HttpClientFactory {
    /**
     * @return a new {@link HttpClient}
     */
    HttpClient create(Model model, SSLContext sslContext);
}
