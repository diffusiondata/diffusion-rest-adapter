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

package com.pushtechnology.adapters.rest;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.MutableModelStore;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Integration test for adapting resources against live services.
 *
 * @author Push Technology Limited
 */
@Category(LiveServices.class)
public final class LiveIT {
    private static final DiffusionConfig DIFFUSION_CONFIG = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .secure(false)
        .principal("control")
        .password("password")
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();

    @Mock
    private Session.Listener listener;
    @Mock
    private ServiceListener serviceListener;
    @Mock
    private ServiceListener backupServiceListener;
    @Mock
    private Topics.ValueStream<JSON> stream;
    @Mock
    private Topics.ValueStream<Binary> binaryStream;
    @Mock
    private Topics.CompletionCallback callback;

    private MutableModelStore modelStore;

    @Before
    public void setup() {
        initMocks(this);

        modelStore = new MutableModelStore();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(listener, callback, serviceListener, backupServiceListener);
    }

    @Test
    public void testInitialValuesAreReceivedForEndpoints() throws IOException {
        final ServiceConfig icndb = ServiceConfig
            .builder()
            .name("icndb")
            .host("api.icndb.com")
            .port(80)
            .secure(false)
            .pollPeriod(10000)
            .endpoints(singletonList(EndpointConfig
                .builder()
                .name("Random Chuck Norris jokes")
                .produces("application/json")
                .topicPath("random")
                .url("/jokes/random")
                .build()))
            .topicPathRoot("icndb")
            .build();

        modelStore.setModel(Model
            .builder()
            .active(true)
            .diffusion(DIFFUSION_CONFIG)
            .services(asList(icndb))
            .metrics(MetricsConfig
                .builder()
                .build())
            .build());
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(icndb);
        verify(serviceListener, timed()).onActive(icndb);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?icndb/", callback);

        verify(callback, timed()).onComplete();
        verify(stream, timed()).onSubscription(eq("icndb/random"), isA(TopicSpecification.class));

        verify(stream, timed()).onValue(eq("icndb/random"), isA(TopicSpecification.class), isNull(), isA(JSON.class));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(icndb);
    }

    private static VerificationWithTimeout timed() {
        return timeout(5000);
    }

    private RESTAdapterClient startClient() {
        return startClient(serviceListener);
    }

    private RESTAdapterClient startClient(ServiceListener listener) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final RESTAdapterClient client = RESTAdapterClient.create(
            Paths.get("."),
            modelStore,
            executor,
            executor::shutdown,
            listener,
            (session, oldState, newState) -> {});
        client.start();
        return client;
    }

    private Session startSession() {
        final Session session = Diffusion
            .sessions()
            .serverHost("localhost")
            .serverPort(8080)
            .listener(listener)
            .open();
        verify(listener, timed()).onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
        return session;
    }

    private void stopSession(Session session) {
        session.close();
        verify(listener, timed()).onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_CLIENT);
    }
}
