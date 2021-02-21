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
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClient;
import com.pushtechnology.adapters.rest.polling.EndpointPollHandlerFactory;
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.publication.EventedUpdateSource;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceEventListener serviceListener;
    @Mock
    private EndpointResponse response;
    @Mock
    private EventedUpdateSource source;
    @Mock
    private UpdateContext<JSON> updateContext;
    @Captor
    private ArgumentCaptor<Consumer<Session.SessionLock>> consumerCaptor;
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
        .endpoints(emptyList())
        .build();
    private final EndpointConfig inferEndpointConfig = EndpointConfig
        .builder()
        .name("inferred-endpoint")
        .url("path")
        .topicPath("topic")
        .produces("auto")
        .build();
    private final ServiceConfig serviceWithEndpoint = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .endpoints(singletonList(endpointConfig))
        .topicPathRoot("root")
        .build();
    private final ServiceConfig serviceWithInferedEndpoint = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .endpoints(singletonList(inferEndpointConfig))
        .topicPathRoot("root")
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

        when(response.getContentType()).thenCallRealMethod();
        when(response.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(response.getResponse()).thenReturn("{}".getBytes(Charset.forName("UTF-8")));
        when(topicManagementClient.addEndpoint(isNotNull(), isNotNull())).thenReturn(CompletableFuture.completedFuture(null));
        when(publishingClient.addService(isNotNull())).thenReturn(source);
        when(publishingClient.createUpdateContext(isNotNull(), isNotNull(), isNotNull(), eq(Diffusion.dataTypes().json()))).thenReturn(updateContext);
        doAnswer(answer((ServiceConfig service, Runnable task) -> { task.run(); return null; })).when(publishingClient).forService(
            isNotNull(), isNotNull());
        when(source.onStandby(isNotNull())).thenReturn(source);
        when(source.onActive(isNotNull())).thenReturn(source);
        when(source.onClose(isNotNull())).thenReturn(source);
        when(endpointClient.request(serviceWithEndpoint, endpointConfig)).thenReturn(CompletableFuture.completedFuture(response));

        serviceSessionFactory = new ServiceSessionFactoryImpl(
            executor,
            endpointClient,
            handlerFactory,
            topicManagementClient,
            publishingClient,
            serviceListener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            executor,
            endpointClient,
            handlerFactory,
            taskFuture,
            topicManagementClient,
            endpointClient,
            publishingClient,
            source,
            response);
    }

    @Test
    public void startSuccessfulPoll() {
        final ServiceSession serviceSession = serviceSessionFactory.create(serviceWithEndpoint);

        verify(publishingClient).addService(serviceWithEndpoint);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());

        consumerCaptor.getValue().accept(null);
        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(serviceWithEndpoint.getPollPeriod()), eq(serviceWithEndpoint.getPollPeriod()), eq(MILLISECONDS));

        assertTrue(serviceSession instanceof ServiceSessionImpl);

        verify(response, times(2)).getContentType();
        verify(response,  times(2)).getHeader("content-type");
        verify(response).getResponse();
        verify(endpointClient).request(serviceWithEndpoint, endpointConfig);
        verify(handlerFactory).create(serviceWithEndpoint, endpointConfig);
        verify(topicManagementClient).addEndpoint(serviceWithEndpoint, endpointConfig);
        verify(publishingClient).forService(eq(serviceWithEndpoint), isA(Runnable.class));
        verify(publishingClient).createUpdateContext(serviceWithEndpoint, endpointConfig, JSON.class, Diffusion.dataTypes().json());
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

        verify(serviceListener).onRemove(serviceConfig);
    }

    @Test
    public void initialiseEndpoint() {
        when(endpointClient.request(eq(serviceWithEndpoint), eq(endpointConfig))).thenReturn(completedFuture(response));

        final ServiceSession serviceSession = serviceSessionFactory.create(serviceWithEndpoint);

        verify(publishingClient).addService(serviceWithEndpoint);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());
        consumerCaptor.getValue().accept(null);

        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(serviceWithEndpoint.getPollPeriod()), eq(serviceWithEndpoint.getPollPeriod()), eq(MILLISECONDS));
        verify(endpointClient).request(eq(serviceWithEndpoint), eq(endpointConfig));

        verify(response, times(2)).getHeader("content-type");
        verify(response, times(2)).getContentType();
        verify(response).getResponse();
        verify(topicManagementClient).addEndpoint(
            eq(serviceWithEndpoint),
            eq(endpointConfig));
        verify(publishingClient).createUpdateContext(eq(serviceWithEndpoint), eq(endpointConfig), isNotNull(), isNotNull());
        verify(updateContext).publish(isNotNull());
        verify(handlerFactory).create(serviceWithEndpoint, endpointConfig);
        verify(publishingClient).forService(eq(serviceWithEndpoint), isNotNull());
    }

    @Test
    public void initialiseEndpointInfer() {
        when(endpointClient.request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig))).thenReturn(completedFuture(response));

        final ServiceSession serviceSession = serviceSessionFactory.create(serviceWithInferedEndpoint);

        verify(publishingClient).addService(serviceWithInferedEndpoint);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());
        consumerCaptor.getValue().accept(null);

        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(serviceWithInferedEndpoint.getPollPeriod()), eq(serviceWithInferedEndpoint.getPollPeriod()), eq(MILLISECONDS));
        verify(endpointClient).request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig));

        verify(response, times(3)).getHeader("content-type");
        verify(response, times(3)).getContentType();
        verify(response).getResponse();

        final EndpointConfig inferredEndpoint = EndpointConfig
            .builder()
            .name("inferred-endpoint")
            .url("path")
            .topicPath("topic")
            .produces("json")
            .build();
        verify(topicManagementClient).addEndpoint(serviceWithInferedEndpoint, inferredEndpoint);
        verify(publishingClient).createUpdateContext(eq(serviceWithInferedEndpoint), eq(inferredEndpoint), isNotNull(), isNotNull());
        verify(publishingClient).addService(serviceWithInferedEndpoint);
        verify(updateContext).publish(isNotNull());
        verify(handlerFactory).create(serviceWithInferedEndpoint, inferredEndpoint);
        verify(publishingClient).forService(eq(serviceWithInferedEndpoint), isNotNull());
    }
}
