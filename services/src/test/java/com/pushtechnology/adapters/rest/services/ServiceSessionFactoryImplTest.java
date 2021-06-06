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

package com.pushtechnology.adapters.rest.services;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.publication.EventedUpdateSource;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.session.Session;

/**
 * Unit tests for {@link ServiceSessionFactoryImpl}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness=Strictness.LENIENT)
public final class ServiceSessionFactoryImplTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private EndpointClient endpointClient;
    @Mock
    private EndpointPollHandlerFactory handlerFactory;
    @Mock
    private TopicManagementClient topicManagementClient;
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceEventListener serviceListener;
    @Mock
    private EventedUpdateSource source;
    @Captor
    private ArgumentCaptor<Consumer<Session.SessionLock>> consumerCaptor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .topicPathRoot("test")
        .port(80)
        .pollPeriod(5000L)
        .endpoints(emptyList())
        .build();

    private ServiceSessionFactory serviceSessionFactory;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        when(publishingClient.addService(isNotNull())).thenReturn(source);
        when(source.onStandby(isNotNull())).thenReturn(source);
        when(source.onActive(isNotNull())).thenReturn(source);
        when(source.onClose(isNotNull())).thenReturn(source);

        serviceSessionFactory = new ServiceSessionFactoryImpl(
            executor,
            endpointClient,
            handlerFactory,
            topicManagementClient,
            publishingClient,
            serviceListener);
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(
            executor,
            endpointClient,
            handlerFactory,
            topicManagementClient,
            publishingClient,
            source,
            serviceListener);
    }

    @Test
    public void active() {
        final ServiceSession serviceSession = serviceSessionFactory.create(serviceConfig);

        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());

        consumerCaptor.getValue().accept(null);

        verify(serviceListener).onActive(serviceConfig);
    }

    @Test
    public void standby() {
        final ServiceSession serviceSession = serviceSessionFactory.create(serviceConfig);

        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(runnableCaptor.capture());
        verify(source).onActive(isNotNull());
        verify(source).onClose(isNotNull());

        runnableCaptor.getValue().run();

        verify(serviceListener).onStandby(serviceConfig);
    }

    @Test
    public void close() {
        final ServiceSession serviceSession = serviceSessionFactory.create(serviceConfig);

        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(isNotNull());
        verify(source).onClose(runnableCaptor.capture());

        runnableCaptor.getValue().run();

        verify(serviceListener).onRemove(serviceConfig, false);
    }
}
