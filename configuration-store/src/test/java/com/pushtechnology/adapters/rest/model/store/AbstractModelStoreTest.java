package com.pushtechnology.adapters.rest.model.store;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link AbstractModelStore}.
 *
 * @author Matt Champion on 18/07/2016
 */
public final class AbstractModelStoreTest {
    @Mock
    private Consumer<Model> listener;

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

    private TestStore modelStore = new TestStore();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testListenerAdded() {
        modelStore.onModelChange(listener);

        verify(listener).accept(null);
    }

    @Test
    public void testNotifyListeners() {
        modelStore.onModelChange(listener);

        verify(listener).accept(null);

        modelStore.notifyListeners(model);

        verify(listener).accept(model);
    }

    private static final class TestStore extends AbstractModelStore {
        @Override
        public Model get() {
            return null;
        }
    }
}
