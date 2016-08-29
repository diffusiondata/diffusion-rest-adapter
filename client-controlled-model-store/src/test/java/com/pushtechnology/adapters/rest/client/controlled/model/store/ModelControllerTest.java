package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;
import com.pushtechnology.diffusion.content.ContentImpl;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link ModelController}.
 *
 * @author Push Technology Limited
 */
public final class ModelControllerTest {

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private SessionId sessionId;

    @Mock
    private ReceiveContext context;

    @Mock
    private ModelPublisher modelPublisher;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private Content emptyMessage;
    private Content unknownTypeMessage;
    private Content createServiceMessage;

    private AsyncMutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new AsyncMutableModelStore(executor);

        // Set the initial model
        modelStore.setModel(Model.builder().services(emptyList()).build());
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        emptyMessage = new ContentImpl(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{}")
            .toByteArray());
        unknownTypeMessage = new ContentImpl(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{\"type\":\"ha, ha\"}")
            .toByteArray());
        createServiceMessage = new ContentImpl(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{\"type\":\"create-service\",\"service\":{\"name\":\"\",\"host\":\"\",\"port\":80,\"secure\":\"false\",\"pollPeriod\":5000,\"topicRoot\":\"\"}}")
            .toByteArray());
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, sessionId, context, modelPublisher);
    }

    @Test
    public void wrongPath() {
        final ModelController controller = new ModelController(modelStore, modelPublisher);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH + "/child", createServiceMessage, context);
    }

    @Test
    public void onEmptyMessage() {
        final ModelController controller = new ModelController(modelStore, modelPublisher);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, new ContentImpl(new byte[0]), context);

        verify(executor).execute(runnableCaptor.capture());
    }

    @Test
    public void onUnknownMessage() {
        final ModelController controller = new ModelController(modelStore, modelPublisher);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, unknownTypeMessage, context);

        verify(executor).execute(runnableCaptor.capture());
    }

    @Test
    public void onEmptyObjectMessage() {
        final ModelController controller = new ModelController(modelStore, modelPublisher);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, emptyMessage, context);

        verify(executor).execute(runnableCaptor.capture());
    }

    @Test
    public void onCreateServiceMessage() {
        final ModelController controller = new ModelController(modelStore, modelPublisher);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, createServiceMessage, context);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(modelPublisher).update();
    }
}
