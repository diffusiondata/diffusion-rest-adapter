package com.pushtechnology.adapters.rest.client.controlled.model.store;

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
import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;

/**
 * Unit tests for {@link ModelController}.
 *
 * @author Matt Champion on 22/08/2016
 */
public final class ModelControllerTest {

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private SessionId sessionId;

    @Mock
    private Content content;

    @Mock
    private ReceiveContext context;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private AsyncMutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new AsyncMutableModelStore(executor);

        // Set the initial model
        modelStore.setModel(Model.builder().build());
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, sessionId, content, context);
    }

    @Test
    public void wrongPath() {
        final ModelController controller = new ModelController(modelStore);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH + "/child", content, context);
    }

    @Test
    public void onMessage() {
        final ModelController controller = new ModelController(modelStore);

        controller.onMessage(sessionId, ClientControlledModelStore.CONTROL_PATH, content, context);

        verify(executor).execute(runnableCaptor.capture());
    }
}
