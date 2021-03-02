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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import org.junit.Test;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link ServiceEventListener}.
 *
 * @author Push Technology Limited
 */
public final class ServiceEventListenerTest {

    @Test
    public void onActive() {
        ServiceEventListener.NULL_LISTENER.onActive(null);
    }

    @Test
    public void onStandby() {
        ServiceEventListener.NULL_LISTENER.onStandby(null);
    }

    @Test
    public void onRemove() {
        ServiceEventListener.NULL_LISTENER.onRemove(null, false);
    }

    @Test
    public void onEndpointAdd() {
        ServiceEventListener.NULL_LISTENER.onEndpointAdd(null, null);
    }

    @Test
    public void onEndpointFail() {
        ServiceEventListener.NULL_LISTENER.onEndpointFail(null, null);
    }

    @Test
    public void onEndpointRemove() {
        ServiceEventListener.NULL_LISTENER.onEndpointRemove(null, null, false);
    }
}
