/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.topic.management;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.INVALID_DETAILS;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link ListenerNotifierWithoutInitialValue}.
 *
 * @author Matt Champion 13/05/2017
 */
public final class ListenerNotifierWithoutInitialValueTest {
    @Mock
    private TopicCreationListener topicCreationListener;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("stringEndpoint")
        .url("endpoint")
        .topicPath("stringEndpoint")
        .produces("string")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(80)
        .pollPeriod(5000)
        .topicPathRoot("service")
        .endpoints(singletonList(endpointConfig))
        .build();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicCreationListener);
    }

    @Test
    public void notifyTopicCreated() throws Exception {
        final ListenerNotifier listenerNotifier = new ListenerNotifierWithoutInitialValue(
            topicCreationListener,
            serviceConfig,
            endpointConfig);

        listenerNotifier.notifyTopicCreated();

        verify(topicCreationListener).onTopicCreated(serviceConfig, endpointConfig);
    }

    @Test
    public void notifyTopicCreationFailed() throws Exception {
        final ListenerNotifier listenerNotifier = new ListenerNotifierWithoutInitialValue(
            topicCreationListener,
            serviceConfig,
            endpointConfig);

        listenerNotifier.notifyTopicCreationFailed(INVALID_DETAILS);

        verify(topicCreationListener).onTopicCreationFailed(serviceConfig, endpointConfig, INVALID_DETAILS);
    }
}
