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

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static com.pushtechnology.adapters.rest.client.controlled.model.store.ClientControlledModelStore.create;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;

/**
 * Integration tests for {@link ClientControlledModelStore}.
 *
 * @author Push Technology Limited
 */
public final class ClientControlledModelStoreIT {
    private final DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void startClose() {
        final ClientControlledModelStore modelStore = create(newSingleThreadScheduledExecutor(), diffusionConfig, null);

        modelStore.start();

        modelStore.close();
    }
}
