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

import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceEventListener serviceListener;
    @Mock
    private EndpointResponse response;
    @Mock
    private UpdateContext<JSON> updateContext;
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
    private final EndpointConfig fastEndpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("/a/url")
        .produces("json")
        .topicPath("url")
        .pollPeriod(1000L)
        .build();
    private final ServiceConfig serviceWithFastEndpointConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .topicPathRoot("test")
        .port(80)
        .pollPeriod(5000L)
        .endpoints(singletonList(fastEndpointConfig))
        .build();
    private final EndpointConfig inferEndpointConfig = EndpointConfig
        .builder()
        .name("inferred-endpoint")
        .url("path")
        .topicPath("topic")
        .produces("auto")
        .build();
    private final ServiceConfig serviceWithInferedEndpoint = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .endpoints(singletonList(inferEndpointConfig))
        .topicPathRoot("root")
        .build();

    private ServiceSessionImpl serviceSession;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        serviceSession = new ServiceSessionImpl(executor, endpointClient, serviceConfig, handlerFactory, topicManagementClient, publishingClient, serviceListener);
        when(executor
            .scheduleWithFixedDelay(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(taskFuture);
        when(endpointClient
            .request(isA(ServiceConfig.class), isA(EndpointConfig.class)))
            .thenReturn(CompletableFuture.completedFuture(response), pollFuture0, pollFuture1);
        when(handlerFactory.create(serviceConfig, endpointConfig)).thenReturn(handler);
        when(handlerFactory.create(serviceConfig, fastEndpointConfig)).thenReturn(handler);

        when(executor
                 .scheduleWithFixedDelay(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(taskFuture);
        when(handlerFactory.create(serviceConfig, endpointConfig)).thenReturn(handler);

        when(response.getContentType()).thenCallRealMethod();
        when(response.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(response.getResponse()).thenReturn("{}".getBytes(Charset.forName("UTF-8")));
        when(topicManagementClient.addEndpoint(isNotNull(), isNotNull())).thenReturn(CompletableFuture.completedFuture(null));
        when(publishingClient.createUpdateContext(isNotNull(), isNotNull(), isNotNull(), eq(Diffusion.dataTypes().json()))).thenReturn(updateContext);
        doAnswer(answer((ServiceConfig service, Runnable task) -> { task.run(); return null; })).when(publishingClient).forService(
            isNotNull(), isNotNull());
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, endpointClient, handlerFactory, taskFuture, topicManagementClient, serviceListener);
    }

    @Test
    public void onStandby() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession.onStandby();
        verify(serviceListener).onStandby(serviceConfig);
    }

    @Test
    public void onStandbyThenClose() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession.onStandby();
        verify(serviceListener).onStandby(serviceConfig);

        serviceSession.onClose();
        verify(serviceListener).onRemove(serviceConfig, false);
    }

    @Test
    public void startSuccessfulPoll() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));
        verify(handler).accept(endpointResponse, null);
    }

    @Test
    public void startSuccessfulPollThenClose() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));
        verify(handler).accept(endpointResponse, null);

        serviceSession.onClose();
        verify(serviceListener).onRemove(serviceConfig, true);
    }

    @Test
    public void startSuccessfulPollWithFastPoll() {
        when(endpointClient.request(eq(serviceWithFastEndpointConfig), eq(fastEndpointConfig))).thenReturn(completedFuture(endpointResponse));

        serviceSession = new ServiceSessionImpl(executor, endpointClient, serviceWithFastEndpointConfig, handlerFactory, topicManagementClient, publishingClient, serviceListener);

        serviceSession.onActive();
        serviceSession.addEndpoint(fastEndpointConfig);
        verify(handlerFactory).create(serviceWithFastEndpointConfig, fastEndpointConfig);
        verify(serviceListener).onActive(serviceWithFastEndpointConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(1000L), eq(1000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceWithFastEndpointConfig), eq(fastEndpointConfig));
    }

    @Test
    public void startFailedPoll() {
        final Exception ex = new Exception("Intentional exception");
        final CompletableFuture<EndpointResponse> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(future);

        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));
        verify(handler).accept(null, ex);
    }

    @Test
    public void stop() {
        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(endpointClient).request(serviceConfig, endpointConfig);
        verify(topicManagementClient).addEndpoint(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
    }

    @Test
    public void stopBeforePoll() {
        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(endpointClient).request(serviceConfig, endpointConfig);
        verify(topicManagementClient).addEndpoint(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
        verify(pollFuture0).whenComplete(isNotNull());
    }

    @Test
    public void stopBeforeSecondPoll() {
        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(endpointClient).request(serviceConfig, endpointConfig);
        verify(topicManagementClient).addEndpoint(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));

        runnable.run();

        verify(endpointClient, times(3)).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();

        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);
        verify(pollFuture1).whenComplete(isNotNull());
    }

    @Test
    public void stopDuringPoll() {
        final CompletableFuture<EndpointResponse> future = new CompletableFuture<>();
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(future);

        serviceSession.onActive();
        serviceSession.addEndpoint(endpointConfig);
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(serviceListener).onActive(serviceConfig);

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(endpointClient, times(2)).request(eq(serviceConfig), eq(endpointConfig));

        serviceSession.stop();
        verify(topicManagementClient).removeEndpoint(serviceConfig, endpointConfig);
        verify(taskFuture).cancel(false);

        future.complete(endpointResponse);
    }


    @Test
    public void initialiseEndpoint() {
        when(endpointClient.request(eq(serviceConfig), eq(endpointConfig))).thenReturn(completedFuture(response));

        serviceSession.onActive();

        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(serviceConfig.getPollPeriod()), eq(serviceConfig.getPollPeriod()), eq(MILLISECONDS));
        verify(endpointClient).request(eq(serviceConfig), eq(endpointConfig));
        verify(serviceListener).onActive(serviceConfig);

        verify(response, times(2)).getHeader("content-type");
        verify(response, times(2)).getContentType();
        verify(response).getResponse();
        verify(topicManagementClient).addEndpoint(
            eq(serviceConfig),
            eq(endpointConfig));
        verify(publishingClient).createUpdateContext(eq(serviceConfig), eq(endpointConfig), isNotNull(), isNotNull());
        verify(updateContext).publish(isNotNull());
        verify(handlerFactory).create(serviceConfig, endpointConfig);
        verify(publishingClient).forService(eq(serviceConfig), isNotNull());
    }

    @Test
    public void initialiseEndpointInfer() {
        serviceSession = new ServiceSessionImpl(executor, endpointClient, serviceWithInferedEndpoint, handlerFactory, topicManagementClient, publishingClient, serviceListener);

        when(endpointClient.request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig))).thenReturn(completedFuture(response));

        serviceSession.onActive();

        verify(executor).scheduleWithFixedDelay(isA(Runnable.class), eq(serviceWithInferedEndpoint.getPollPeriod()), eq(serviceWithInferedEndpoint.getPollPeriod()), eq(MILLISECONDS));
        verify(endpointClient).request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig));
        verify(serviceListener).onActive(serviceWithInferedEndpoint);

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
        verify(updateContext).publish(isNotNull());
        verify(handlerFactory).create(serviceWithInferedEndpoint, inferredEndpoint);
        verify(publishingClient).forService(eq(serviceWithInferedEndpoint), isNotNull());
    }
}
