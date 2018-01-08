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
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Unit tests for {@link ServiceSessionImpl}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private EndpointClient endpointClient;
    @Mock
    private EndpointResponse endpointResponse;
    @Mock
    private EndpointPollHandlerFactory handlerFactory;
    @Mock
    private ScheduledFuture taskFuture;
    @Mock
    private CompletableFuture pollFuture0;
    @Mock
    private CompletableFuture pollFuture1;
    @Mock
    private BiConsumer<EndpointResponse, Throwable> handler;
    @Mock
    private TopicManagementClient topicManagementClient;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

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

    private ServiceSession serviceSession;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        serviceSession = new ServiceSessionImpl(executor, endpointClient, serviceConfig, handlerFactory, topicManagementClient);
        when(executor
            .scheduleWithFixedDelay(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(taskFuture);
        when(endpointClient
            .request(isA(ServiceConfig.class), isA(EndpointConfig.class)))
            .thenReturn(pollFuture0, pollFuture1);
        when(handlerFactory.create(serviceConfig, endpointConfig)).thenReturn(handler);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, endpointClient, handlerFactory, taskFuture, topicManagementClient);
    }

    @Test
    public void startSuccessfulPoll() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));
        verify(handler).accept(endpointResponse, null);
    }

    @Test
    public void startFailedPoll() {
        final Exception ex = new Exception("Intentional exception");
        final CompletableFuture<EndpointResponse> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(future);

        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));
        verify(handler).accept(null, ex);
    }

    @Test
    public void stop() {
        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
    }

    @Test
    public void stopBeforePoll() {
        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
        verify(pollFuture0).whenComplete(isNotNull());
    }

    @Test
    public void stopBeforeSecondPoll() {
        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
        verify(pollFuture1).whenComplete(isNotNull());
    }

    @Test
    public void stopDuringPoll() {
        final CompletableFuture<EndpointResponse> future = new CompletableFuture<>();
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(future);

        serviceSession.start();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();
        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);

        future.complete(endpointResponse);
    }
}
