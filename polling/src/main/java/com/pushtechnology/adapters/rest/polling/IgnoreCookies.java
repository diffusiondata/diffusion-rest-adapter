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

import java.net.CookieHandler;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link CookieHandler} that ignores cookies.
 *
 * @author Push Technology Limited
 */
public final class IgnoreCookies extends CookieHandler {
    private static final CookieHandler HANDLER = new IgnoreCookies();

    /**
     * Handler that ignores cookies.
     */
    public static CookieHandler handler() {
        return HANDLER;
    }

    private IgnoreCookies() {
    }

    @Override
    public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) {
        return Collections.emptyMap();
    }

    @Override
    public void put(URI uri, Map<String, List<String>> responseHeaders) {
    }
}
