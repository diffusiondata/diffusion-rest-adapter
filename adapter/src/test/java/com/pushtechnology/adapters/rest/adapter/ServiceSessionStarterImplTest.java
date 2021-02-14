/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
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
import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.adapters.rest.publication.EventedUpdateSource;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.publication.UpdateContext;
import com.pushtechnology.adapters.rest.services.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.session.Session.SessionLock;
import com.pushtechnology.diffusion.datatype.json.JSON;

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
    private ServiceEventListener serviceListener;

    @Mock
    private EndpointResponse response;

    @Mock
    private UpdateContext<JSON> updateContext;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<Consumer<SessionLock>> consumerCaptor;

    private ServiceSessionStarter binder;
    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .endpoints(emptyList())
        .topicPathRoot("root")
        .build();
    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
        .url("path")
        .topicPath("topic")
        .produces("json")
        .build();
    private final EndpointConfig inferEndpointConfig = EndpointConfig
        .builder()
        .name("endpoint")
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

    @Before
    public void setUp() {
        initMocks(this);

        binder = new ServiceSessionStarterImpl(
            topicManagementClient,
            endpointClient,
            publishingClient,
            serviceListener);

        when(response.getContentType()).thenCallRealMethod();
        when(response.getHeader("content-type")).thenReturn("application/json; charset=utf-8");
        when(response.getResponse()).thenReturn("{}".getBytes(Charset.forName("UTF-8")));
        when(topicManagementClient.addEndpoint(isNotNull(), isNotNull())).thenReturn(CompletableFuture.completedFuture(null));
        when(publishingClient.addService(isNotNull())).thenReturn(source);
        when(publishingClient.createUpdateContext(isNotNull(), isNotNull(), isNotNull(), eq(Diffusion.dataTypes().json()))).thenReturn(updateContext);
        doAnswer(answer((ServiceConfig service, Runnable task) -> { task.run(); return null; })).when(publishingClient).forService(isNotNull(), isNotNull());
        when(source.onStandby(isNotNull())).thenReturn(source);
        when(source.onActive(isNotNull())).thenReturn(source);
        when(source.onClose(isNotNull())).thenReturn(source);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            topicManagementClient,
            endpointClient,
            publishingClient,
            source,
            serviceSession,
            response);
    }

    @Test
    public void start() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());

        consumerCaptor.getValue().accept(null);
        verify(serviceSession).start();
    }

    @Test
    public void standby() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
        verify(publishingClient).addService(serviceConfig);
        verify(source).onStandby(runnableCaptor.capture());
        verify(source).onActive(isNotNull());
        verify(source).onClose(isNotNull());

        runnableCaptor.getValue().run();

        verify(serviceListener).onStandby(serviceConfig);
    }

    @Test
    public void close() {
        binder.start(serviceConfig, serviceSession);

        verify(topicManagementClient).addService(serviceConfig);
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

        binder.start(serviceWithEndpoint, serviceSession);

        verify(topicManagementClient).addService(serviceWithEndpoint);
        verify(publishingClient).addService(serviceWithEndpoint);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());
        consumerCaptor.getValue().accept(null);

        verify(serviceSession).start();
        verify(endpointClient).request(eq(serviceWithEndpoint), eq(endpointConfig));

        verify(response, times(2)).getHeader("content-type");
        verify(response, times(2)).getContentType();
        verify(response).getResponse();
        verify(topicManagementClient).addEndpoint(
            eq(serviceWithEndpoint),
            eq(endpointConfig));
        verify(publishingClient).createUpdateContext(eq(serviceWithEndpoint), eq(endpointConfig), isNotNull(), isNotNull());
        verify(updateContext).publish(isNotNull());
        verify(serviceSession).addEndpoint(endpointConfig);
        verify(publishingClient).forService(eq(serviceWithEndpoint), isNotNull());
    }

    @Test
    public void initialiseEndpointInfer() {
        when(endpointClient.request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig))).thenReturn(completedFuture(response));

        binder.start(serviceWithInferedEndpoint, serviceSession);

        verify(topicManagementClient).addService(serviceWithInferedEndpoint);
        verify(publishingClient).addService(serviceWithInferedEndpoint);
        verify(source).onStandby(isNotNull());
        verify(source).onActive(consumerCaptor.capture());
        verify(source).onClose(isNotNull());
        consumerCaptor.getValue().accept(null);

        verify(serviceSession).start();
        verify(endpointClient).request(eq(serviceWithInferedEndpoint), eq(inferEndpointConfig));

        verify(response, times(3)).getHeader("content-type");
        verify(response, times(3)).getContentType();
        verify(response).getResponse();

        verify(topicManagementClient).addEndpoint(
            eq(serviceWithInferedEndpoint),
            eq(endpointConfig));
        verify(publishingClient).createUpdateContext(eq(serviceWithInferedEndpoint), eq(endpointConfig), isNotNull(), isNotNull());
        verify(updateContext).publish(isNotNull());
        verify(serviceSession).addEndpoint(endpointConfig);
        verify(publishingClient).forService(eq(serviceWithInferedEndpoint), isNotNull());
    }
}
