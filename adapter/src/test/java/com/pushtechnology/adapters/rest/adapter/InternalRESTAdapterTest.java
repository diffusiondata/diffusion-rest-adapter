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

package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpClientFactory;
import com.pushtechnology.adapters.rest.session.management.SessionLossHandler;
import com.pushtechnology.diffusion.client.callbacks.Registration;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link InternalRESTAdapter}.
 *
 * @author Push Technology Limited
 */
public final class InternalRESTAdapterTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private ScheduledExecutorService executorService;
    @Mock
    private SessionLossHandler sessionLossHandler;
    @Mock
    private ServiceListener serviceListener;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private HttpClientFactory httpClientFactory;
    @Mock
    private CloseableHttpAsyncClient httpClient;
    @Mock
    private TopicControl topicControl;
    @Mock
    private TopicUpdateControl updateControl;
    @Mock
    private Runnable shutdownHandler;
    @Mock
    private Registration registration;

    private final DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .principal("control")
        .password("password")
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();
    private final ServiceConfig serviceConfig0 = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .secure(false)
        .endpoints(singletonList(
            EndpointConfig
                .builder()
                .name("increment")
                .topicPath("increment")
                .url("/rest/increment")
                .produces("json")
                .build()))
        .topicPathRoot("root")
        .pollPeriod(5000)
        .build();
    private final ServiceConfig serviceConfig1 = ServiceConfig
        .builder()
        .name("service-1")
        .host("localhost")
        .secure(false)
        .endpoints(singletonList(
            EndpointConfig
                .builder()
                .name("increment")
                .topicPath("increment")
                .url("/rest/increment")
                .produces("json")
                .build()))
        .topicPathRoot("root2")
        .pollPeriod(5000)
        .build();
    private final Model model0 = Model
        .builder()
        .active(true)
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig0))
        .metrics(MetricsConfig.builder().counting(false).build())
        .build();
    private final Model model1 = Model
        .builder()
        .active(true)
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig1))
        .metrics(MetricsConfig.builder().counting(false).build())
        .build();
    private final Model model2 = Model
        .builder()
        .active(true)
        .diffusion(diffusionConfig)
        .services(emptyList())
        .metrics(MetricsConfig.builder().counting(false).build())
        .build();
    private final Model inactiveModel = Model
        .builder()
        .active(false)
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig0))
        .metrics(MetricsConfig.builder().counting(false).build())
        .build();

    private InternalRESTAdapter restAdapter;

    @Before
    public void setUp() {
        when(sessionFactory.serverHost(isNotNull())).thenReturn(sessionFactory);
        when(sessionFactory.serverPort(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.transports(isNotNull())).thenReturn(sessionFactory);
        when(sessionFactory.secureTransport(anyBoolean())).thenReturn(sessionFactory);
        when(sessionFactory.connectionTimeout(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.reconnectionTimeout(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.maximumMessageSize(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.inputBufferSize(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.outputBufferSize(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.recoveryBufferSize(anyInt())).thenReturn(sessionFactory);
        when(sessionFactory.listener(isNotNull())).thenReturn(sessionFactory);
        when(sessionFactory.principal(isNotNull())).thenReturn(sessionFactory);
        when(sessionFactory.password(isNotNull())).thenReturn(sessionFactory);
        when(sessionFactory.openAsync()).thenReturn(completedFuture(session));

        when(httpClientFactory.create(isNotNull(), any())).thenReturn(httpClient);

        when(session.feature(TopicControl.class)).thenReturn(topicControl);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);
        when(topicControl.removeTopicsWithSession(anyString())).thenReturn(completedFuture(registration));

        restAdapter = new InternalRESTAdapter(
            executorService,
            sessionFactory,
            httpClientFactory,
            serviceListener,
            sessionLossHandler,
            shutdownHandler,
            (session, oldState, newState) -> {});

        verify(sessionFactory).transports(WEBSOCKET);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(
            executorService,
            serviceListener,
            sessionLossHandler,
            sessionFactory,
            session,
            httpClientFactory,
            topicControl,
            updateControl,
            shutdownHandler,
            registration);
    }

    @Test
    public void startConnectAndStop() throws Exception {
        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        verify(httpClientFactory).create(model0, null);
        verify(httpClient).start();

        verify(session).feature(TopicControl.class);
        verify(session).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root"));
        verify(updateControl).registerUpdateSource(eq("root"), isNotNull());

        restAdapter.onReconfiguration(inactiveModel);

        verify(session).close();
        verify(httpClient).close();
        verify(shutdownHandler).run();
    }

    @Test
    public void startAndStop() throws Exception {
        final CompletableFuture<Session> sessionFuture = new CompletableFuture<>();
        when(sessionFactory.openAsync()).thenReturn(sessionFuture);

        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        restAdapter.onReconfiguration(inactiveModel);

        sessionFuture.complete(session);

        verify(session).close();
        verify(shutdownHandler).run();
    }

    @Test
    public void startConnectAndReconfigure() throws Exception {
        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        verify(httpClientFactory).create(model0, null);
        verify(httpClient).start();

        verify(session).feature(TopicControl.class);
        verify(session).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root"));
        verify(updateControl).registerUpdateSource(eq("root"), isNotNull());

        restAdapter.onReconfiguration(model1);

        verify(httpClientFactory).create(model1, null);
        verify(httpClient, times(2)).start();

        verify(session, times(2)).feature(TopicControl.class);
        verify(session, times(2)).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root2"));
        verify(updateControl).registerUpdateSource(eq("root2"), isNotNull());
    }

    @Test
    public void startReconfigureAndConnect() throws Exception {
        final CompletableFuture<Session> sessionFuture = new CompletableFuture<>();
        when(sessionFactory.openAsync()).thenReturn(sessionFuture);

        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        restAdapter.onReconfiguration(model1);

        sessionFuture.complete(session);

        verify(httpClientFactory).create(model1, null);
        verify(httpClient).start();

        verify(session).feature(TopicControl.class);
        verify(session).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root2"));
        verify(updateControl).registerUpdateSource(eq("root2"), isNotNull());
    }

    @Test
    public void startWithoutServices() throws Exception {
        restAdapter.onReconfiguration(model2);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();
    }

    @Test
    public void startConnectAndRemoveServices() throws Exception {
        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        verify(httpClientFactory).create(model0, null);
        verify(httpClient).start();

        verify(session).feature(TopicControl.class);
        verify(session).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root"));
        verify(updateControl).registerUpdateSource(eq("root"), isNotNull());

        restAdapter.onReconfiguration(model2);

        verify(httpClient).close();
    }

    @Test
    public void startRemoveServicesAndConnect() throws Exception {
        final CompletableFuture<Session> sessionFuture = new CompletableFuture<>();
        when(sessionFactory.openAsync()).thenReturn(sessionFuture);

        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        restAdapter.onReconfiguration(model2);

        sessionFuture.complete(session);
    }

    @Test
    public void startConnectAndClose() throws Exception {
        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        verify(httpClientFactory).create(model0, null);
        verify(httpClient).start();

        verify(session).feature(TopicControl.class);
        verify(session).feature(TopicUpdateControl.class);
        verify(topicControl).removeTopicsWithSession(eq("root"));
        verify(updateControl).registerUpdateSource(eq("root"), isNotNull());

        restAdapter.close();

        verify(session).close();
        verify(httpClient).close();
        verify(shutdownHandler).run();
    }

    @Test
    public void startAndClose() throws Exception {
        final CompletableFuture<Session> sessionFuture = new CompletableFuture<>();
        when(sessionFactory.openAsync()).thenReturn(sessionFuture);

        restAdapter.onReconfiguration(model0);

        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).connectionTimeout(10000);
        verify(sessionFactory).reconnectionTimeout(10000);
        verify(sessionFactory).maximumMessageSize(32000);
        verify(sessionFactory).inputBufferSize(32000);
        verify(sessionFactory).outputBufferSize(32000);
        verify(sessionFactory).recoveryBufferSize(256);
        verify(sessionFactory).listener(isNotNull());
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).openAsync();

        restAdapter.close();

        sessionFuture.complete(session);

        verify(session).close();
        verify(shutdownHandler).run();
    }
}
