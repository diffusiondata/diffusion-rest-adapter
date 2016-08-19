package com.pushtechnology.adapters.rest.model.store;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.picocontainer.annotations.Cache;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link AsyncMutableModelStore}.
 *
 * @author Matt Champion on 19/08/2016
 */
public final class AsyncMutableModelStoreTest {

    @Mock
    private Executor executor;
    @Mock
    private Consumer<Model> consumer;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicRoot("a")
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
        .build();

    private final Model emptyModel = Model.builder().build();

    private AsyncMutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new AsyncMutableModelStore(executor);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(consumer, executor);
    }

    @Test
    public void get() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        assertEquals(model, modelStore.get());
    }

    @Test
    public void onModelChangeNone() {
        modelStore.onModelChange(consumer);
    }

    @Test
    public void onModelChangeSet() {
        modelStore.setModel(emptyModel);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(emptyModel);
    }

    @Test
    public void onModelChangeChange() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(emptyModel);
    }

    @Test
    public void onModelChangeModification() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(emptyModel);

        modelStore.setModel(model);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(model);
    }

    @Test
    public void onModelChangeNoModification() {

        modelStore.onModelChange(consumer);
        modelStore.setModel(emptyModel);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(emptyModel);

        modelStore.setModel(emptyModel);
    }

    @Test
    public void onModelChangeQueued() {

        modelStore.onModelChange(consumer);

        modelStore.setModel(emptyModel);
        modelStore.setModel(model);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().get(0).run();
        runnableCaptor.getAllValues().get(1).run();

        verify(consumer).accept(model);
    }
}
