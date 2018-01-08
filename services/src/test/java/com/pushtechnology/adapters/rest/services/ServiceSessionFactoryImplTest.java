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

package com.pushtechnology.adapters.rest.services;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Unit tests for {@link ServiceSessionFactoryImpl}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionFactoryImplTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private EndpointClient endpointClient;
    @Mock
    private EndpointPollHandlerFactory handlerFactory;
    @Mock
    private ScheduledFuture taskFuture;
    @Mock
    private BiConsumer<EndpointResponse, Throwable> handler;
    @Mock
    private TopicManagementClient topicManagementClient;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("/a/url")
        .produces("json")
        .topicPath("url")
        .build();
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .topicPathRoot("test")
        .port(80)
        .pollPeriod(5000L)
        .endpoints(singletonList(endpointConfig))
        .build();

    private ServiceSessionFactory serviceSessionFactory;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(executor
            .scheduleWithFixedDelay(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(taskFuture);
        when(handlerFactory.create(serviceConfig, endpointConfig)).thenReturn(handler);

        serviceSessionFactory = new ServiceSessionFactoryImpl(
            executor,
            endpointClient,
            handlerFactory,
            topicManagementClient);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, endpointClient, handlerFactory, taskFuture, topicManagementClient);
    }

    @Test
    public void startSuccessfulPoll() {
        final ServiceSession serviceSession = serviceSessionFactory.create(serviceConfig);

        assertTrue(serviceSession instanceof ServiceSessionImpl);

        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(5000L), eq(5000L), eq(MILLISECONDS));
    }
}
