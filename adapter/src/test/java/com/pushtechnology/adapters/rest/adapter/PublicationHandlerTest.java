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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.CancellationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PublicationHandler}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class PublicationHandlerTest {
    @Mock
    private JSON json;
    @Mock
    private UpdateContext<JSON> updateContext;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("topic")
        .url("http://localhost/json")
        .produces("json")
        .build();

    private PublicationHandler<JSON> pollHandler;

    @BeforeEach
    public void setUp() {
        pollHandler = new PublicationHandler<>(endpointConfig, updateContext);
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(updateContext, json);
    }

    @Test
    public void completed() {
        pollHandler.accept(json, null);

        verify(updateContext).publish(json);
    }

    @Test
    public void failed() {
        pollHandler.accept(null, new Exception("Intentional for test"));
    }

    @Test
    public void cancelled() {
        pollHandler.accept(null, new CancellationException());
    }
}
