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

package com.pushtechnology.adapters.rest.model.store;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link MutableModelStore}.
 *
 * @author Push Technology Limited
 */
public final class MutableModelStoreTest {

    @Mock
    private Consumer<Model> consumer;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("topic")
        .url("http://localhost/json")
        .produces("json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicPathRoot("a")
        .build();

    private final DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .principal("control")
        .password("password")
        .build();

    private final Model model = Model
        .builder()
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig))
        .build();

    private final Model emptyModel = Model
        .builder()
        .diffusion(diffusionConfig)
        .services(emptyList())
        .build();

    private MutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new MutableModelStore();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(consumer);
    }

    @Test
    public void get() {
        modelStore.setModel(model);
        assertEquals(model, modelStore.get());
    }

    @Test
    public void onModelChangeNone() {
        modelStore.onModelChange(consumer);
    }

    @Test
    public void onModelChangeSet() {
        modelStore.setModel(emptyModel);

        modelStore.onModelChange(consumer);

        verify(consumer).accept(emptyModel);
    }

    @Test
    public void onModelChangeChange() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(consumer).accept(emptyModel);
    }

    @Test
    public void onModelChangeModification() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(consumer).accept(emptyModel);

        modelStore.setModel(model);

        verify(consumer).accept(model);
    }

    @Test
    public void onModelChangeNoModification() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(consumer).accept(emptyModel);

        modelStore.setModel(emptyModel);
    }
}
