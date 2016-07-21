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

package com.pushtechnology.adapters.rest.client;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpComponent;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link PublicationComponentImpl}.
 *
 * @author Push Technology Limited
 */
public final class PublicationComponentImplTest {
    @Mock
    private Session session;
    @Mock
    private TopicManagementClient topicManagementClient;
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private HttpComponent httpComponent;
    @Mock
    private CompletableFuture<ServiceConfig> completableFuture;
    @Mock
    private ScheduledExecutorService executor;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicRoot("a")
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

    private AtomicBoolean isActive;
    private PublicationComponent publicationComponent;

    @Before
    public void setUp() {
        initMocks(this);

        when(publishingClient.addService(serviceConfig)).thenReturn(completableFuture);

        isActive = new AtomicBoolean(true);
        publicationComponent = new PublicationComponentImpl(
            isActive,
            session,
            topicManagementClient,
            publishingClient,
            new PollingComponentFactory(executor));
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, topicManagementClient, publishingClient, httpComponent, executor);
    }

    @Test
    public void close() throws IOException {
        publicationComponent.close();

        assertFalse(isActive.get());
        verify(session).close();
    }

    @Test
    public void createPolling() {
        final PollingComponent pollingComponent = publicationComponent.createPolling(model, httpComponent);
        pollingComponent.close();

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
    }

    @Test
    public void closeInactive() throws IOException {
        PublicationComponent.INACTIVE.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createPollingInactive() {
        PublicationComponent.INACTIVE.createPolling(model, httpComponent);
    }
}
