package com.pushtechnology.adapters.rest.model.store;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore.CreateResult;

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

    private final EndpointConfig endpointConfig0 = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final EndpointConfig endpointConfig1 = EndpointConfig
        .builder()
        .name("endpoint-1")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig0 = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig0))
        .topicRoot("a")
        .build();

    private final ServiceConfig serviceConfig1 = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(asList(endpointConfig0, endpointConfig1))
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
        .services(singletonList(serviceConfig0))
        .build();

    private final Model modelWithTwoEndpoints = Model
        .builder()
        .diffusion(diffusionConfig)
        .services(singletonList(serviceConfig1))
        .build();

    private final Model emptyModel = Model.builder().build();

    private final Model modelWithDiffusion = Model
        .builder()
        .diffusion(diffusionConfig)
        .services(emptyList())
        .build();

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
    public void onModelChangeApply() {
        modelStore.setModel(emptyModel);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(emptyModel);

        modelStore.apply(currentModel -> model);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(model);
    }

    @Test
    public void onModelChangeCreateService() {
        modelStore.setModel(modelWithDiffusion);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(modelWithDiffusion);

        final CreateResult result = modelStore.createService(serviceConfig0);

        assertEquals(CreateResult.SUCCESS, result);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(model);
    }

    @Test
    public void createServiceModelUnchanged() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore.createService(serviceConfig0);

        assertEquals(CreateResult.SUCCESS, result);
    }

    @Test
    public void createServiceConflict() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore.createService(ServiceConfig.builder().name("service-0").build());

        assertEquals(CreateResult.CONFLICT, result);
    }

    @Test
    public void onModelChangeCreateEndpoint() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore.createEndpoint("service-0", endpointConfig1);

        assertEquals(CreateResult.SUCCESS, result);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(consumer).accept(modelWithTwoEndpoints);
    }

    @Test
    public void createEndpointModelUnchanged() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore.createEndpoint("service-0", endpointConfig0);

        assertEquals(CreateResult.SUCCESS, result);
    }

    @Test
    public void createEndpointConflict() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore
            .createEndpoint("service-0", EndpointConfig.builder().name("endpoint-0").build());

        assertEquals(CreateResult.CONFLICT, result);
    }

    @Test
    public void createEndpointNoService() {
        modelStore.setModel(model);

        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        modelStore.onModelChange(consumer);

        verify(consumer).accept(model);

        final CreateResult result = modelStore.createEndpoint("service-1", endpointConfig0);

        assertEquals(CreateResult.PARENT_MISSING, result);
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
