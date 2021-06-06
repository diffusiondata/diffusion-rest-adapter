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

package com.pushtechnology.adapters.rest.adapter;

import static com.pushtechnology.diffusion.client.session.Session.SessionLockScope.UNLOCK_ON_CONNECTION_LOSS;
import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.HttpClientFactory;
import com.pushtechnology.adapters.rest.session.management.SessionLossHandler;
import com.pushtechnology.diffusion.client.features.TopicUpdate;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link InternalRESTAdapter}.
 *
 * @author Push Technology Limited
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness= Strictness.LENIENT)
public final class InternalRESTAdapterTest {
    @Mock
    private ScheduledExecutorService executorService;
    @Mock
    private SessionLossHandler sessionLossHandler;
    @Mock
    private ServiceEventListener serviceListener;
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private TopicUpdate topicUpdate;
    @Mock
    private CompletableFuture<Session.SessionLock> registration;
    @Mock
    private HttpClientFactory httpClientFactory;
    @Mock
    private HttpClient httpClient;
    @Mock
    private TopicControl topicControl;
    @Mock
    private Runnable shutdownHandler;

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
        .metrics(MetricsConfig.builder().logging(false).build())
        .build();
    private final Model model1 = Model
        .builder()
        .active(true)
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig1))
        .metrics(MetricsConfig.builder().logging(false).build())
        .build();
    private final Model model2 = Model
        .builder()
        .active(true)
        .diffusion(diffusionConfig)
        .services(emptyList())
        .metrics(MetricsConfig.builder().logging(false).build())
        .build();
    private final Model inactiveModel = Model
        .builder()
        .active(false)
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig0))
        .metrics(MetricsConfig.builder().logging(false).build())
        .build();

    private InternalRESTAdapter restAdapter;

    @BeforeEach
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
        when(session.feature(TopicUpdate.class)).thenReturn(topicUpdate);
        when(session.lock(isNotNull(), isNotNull())).thenReturn(registration);

        restAdapter = new InternalRESTAdapter(
            Paths.get("."),
            executorService,
            sessionFactory,
            httpClientFactory,
            serviceListener,
            sessionLossHandler,
            shutdownHandler,
            (session, oldState, newState) -> {});

        verify(sessionFactory).transports(WEBSOCKET);
    }

    @AfterEach
    public void postConditions() {
        verifyNoMoreInteractions(
            executorService,
            serviceListener,
            sessionLossHandler,
            sessionFactory,
            session,
            httpClientFactory,
            topicControl,
            shutdownHandler);
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

        verify(session).lock("service-0", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model0.getServices().get(0));

        restAdapter.onReconfiguration(inactiveModel);

        verify(session).close();
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

        verify(session).lock("service-0", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model0.getServices().get(0));

        restAdapter.onReconfiguration(model1);

        verify(httpClientFactory).create(model1, null);

        verify(session).lock("service-1", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model1.getServices().get(0));
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

        verify(session).lock("service-1", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model1.getServices().get(0));
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

        verify(session).lock("service-0", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model0.getServices().get(0));

        restAdapter.onReconfiguration(model2);
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

        verify(session).lock("service-0", UNLOCK_ON_CONNECTION_LOSS);
        verify(serviceListener).onStandby(model0.getServices().get(0));

        restAdapter.close();

        verify(session).close();
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
