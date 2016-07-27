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

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Unit tests for {@link ServiceSessionGroupImpl}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionGroupImplTest {

    @Mock
    private TopicManagementClient topicManagementClient;

    @Mock
    private PublishingClient publishingClient;

    @Mock
    private ServiceSession serviceSession;

    @Mock
    private ServiceSessionFactory serviceSessionFactory;

    @Mock
    private CompletableFuture<ServiceConfig> future;

    private ServiceConfig serviceConfig = ServiceConfig.builder().build();

    @Before
    public void setUp() {
        initMocks(this);

        when(publishingClient.addService(serviceConfig)).thenReturn(future);
        when(serviceSessionFactory.create(serviceConfig)).thenReturn(serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(topicManagementClient, publishingClient, future, serviceSessionFactory, serviceSession);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void start() {
        final ServiceSessionGroup serviceSessionGroup = new ServiceSessionGroupImpl(
            Model.builder().services(singletonList(serviceConfig)).build(),
            topicManagementClient,
            publishingClient,
            serviceSessionFactory);

        serviceSessionGroup.start();

        verify(serviceSessionFactory).create(serviceConfig);
        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(future).thenAccept(isA(Consumer.class));
    }

    @Test
    public void close() {
        final ServiceSessionGroup serviceSessionGroup = new ServiceSessionGroupImpl(
            Model.builder().services(singletonList(serviceConfig)).build(),
            topicManagementClient,
            publishingClient,
            serviceSessionFactory);

        serviceSessionGroup.close();

        verify(publishingClient).removeService(serviceConfig);
    }
}
