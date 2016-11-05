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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.EventedUpdateSource;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Unit tests for {@link ServiceSessionStarterImpl}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionStarterImplTest {

    @Mock
    private TopicManagementClient topicManagementClient;

    @Mock
    private PublishingClient publishingClient;

    @Mock
    private EndpointClient endpointClient;

    @Mock
    private ServiceSession serviceSession;

    @Mock
    private EventedUpdateSource source;

    @Mock
    private ServiceListener serviceListener;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private ServiceSessionStarter binder;
    private ServiceConfig serviceConfig = ServiceConfig.builder().build();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        binder = new ServiceSessionStarterImpl(
            topicManagementClient,
            endpointClient,
            publishingClient,
            serviceListener);

        when(publishingClient.addService(serviceConfig)).thenReturn(source);
        when(source.onStandby(isA(Runnable.class))).thenReturn(source);
        when(source.onActive(isA(Consumer.class))).thenReturn(source);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            topicManagementClient,
            endpointClient,
            publishingClient,
            source,
            serviceSession);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void start() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(isA(Runnable.class));
        verify(source).onActive(isA(Consumer.class));
        verify(source).onClose(isA(Runnable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void standby() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(runnableCaptor.capture());
        verify(source).onActive(isA(Consumer.class));
        verify(source).onClose(isA(Runnable.class));

        runnableCaptor.getValue().run();

        verify(serviceListener).onStandby(serviceConfig);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void close() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(isA(Runnable.class));
        verify(source).onActive(isA(Consumer.class));
        verify(source).onClose(runnableCaptor.capture());

        runnableCaptor.getValue().run();

        verify(serviceListener).onRemove(serviceConfig);
    }
}
