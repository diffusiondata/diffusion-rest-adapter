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

package com.pushtechnology.adapters.rest.model.store;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.persistence.Persistence;

/**
 * Unit tests for {@link PollingPersistedModelStore}.
 *
 * @author Push Technology Limited
 */
public final class PollingPersistedModelStoreTest {
    @Mock
    private Persistence persistence;
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture future;
    @Mock
    private Consumer<Model> listener;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topicPath("topic")
        .url("http://localhost/json")
        .produces("json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicPathRoot("a")
        .build();

    private final DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .principal("control")
        .password("password")
        .build();

    private final Model model = Model
        .builder()
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig))
        .metrics(MetricsConfig
            .builder()
            .build())
        .build();

    private PollingPersistedModelStore modelStore;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(executor.scheduleAtFixedRate(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(future);
        when(persistence.loadModel()).thenReturn(Optional.of(model));

        modelStore = new PollingPersistedModelStore(persistence, executor, 5000L);
        modelStore.onModelChange(listener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(persistence, executor, future, listener);
    }

    @Test
    public void stopBeforeStart() {
        modelStore.stop();
    }

    @Test
    public void start() throws IOException {
        modelStore.start();

        verify(listener).accept(model);
        verify(persistence).loadModel();
        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void startStop() throws IOException {
        modelStore.start();

        verify(persistence).loadModel();
        verify(listener).accept(model);
        verify(executor).scheduleAtFixedRate(isA(Runnable.class), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));

        modelStore.stop();

        verify(future).cancel(false);
    }

    @Test
    public void doubleStart() throws IOException {
        modelStore.start();
        modelStore.start();

        verify(listener, times(2)).accept(model);
        verify(persistence, times(2)).loadModel();
        verify(executor, times(2))
            .scheduleAtFixedRate(isA(Runnable.class), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));
        verify(future).cancel(false);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void pollNone() throws IOException {
        when(persistence.loadModel()).thenReturn(Optional.of(model), Optional.empty());

        modelStore.start();

        verify(listener).accept(model);
        verify(persistence).loadModel();
        verify(executor).scheduleAtFixedRate(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));

        runnableCaptor.getValue().run();
        verify(persistence, times(2)).loadModel();
    }

    @Test
    public void pollModel() throws IOException {
        modelStore.start();

        verify(persistence).loadModel();
        verify(listener).accept(model);
        verify(executor).scheduleAtFixedRate(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));

        runnableCaptor.getValue().run();

        verify(persistence, times(2)).loadModel();
    }

    @Test
    public void pollException() throws IOException {
        when(persistence.loadModel()).then(new Answer<Optional<Model>>() {
            private int count = 0;
            @Override
            public Optional<Model> answer(InvocationOnMock invocation) throws Throwable {
                count += 1;

                if (count >= 2) {
                    throw new IOException("Intentional for test");
                }

                return Optional.of(model);
            }
        });

        modelStore.start();

        verify(persistence).loadModel();
        verify(listener).accept(model);
        verify(executor).scheduleAtFixedRate(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));

        runnableCaptor.getValue().run();

        verify(persistence, times(2)).loadModel();
    }

    @Test
    public void pollSame() throws IOException {
        modelStore.start();

        verify(persistence).loadModel();
        verify(listener).accept(model);
        verify(executor).scheduleAtFixedRate(runnableCaptor.capture(), eq(5000L), eq(5000L), eq(TimeUnit.MILLISECONDS));

        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();

        verify(persistence, times(3)).loadModel();
    }
}
