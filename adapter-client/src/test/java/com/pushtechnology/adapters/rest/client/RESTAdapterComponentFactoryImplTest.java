package com.pushtechnology.adapters.rest.client;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.PollClient;

/**
 * Unit tests for {@link RESTAdapterComponentFactoryImpl}.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterComponentFactoryImplTest {
    @Mock
    private RESTAdapterComponentFactory delegateFactory;
    @Mock
    private PollClient pollClient;
    @Mock
    private RESTAdapterClientCloseHandle closeHandle;
    @Mock
    private Component component;

    private RESTAdapterComponentFactory factory;

    @Before
    public void setUp() {
        initMocks(this);

        factory = new RESTAdapterComponentFactoryImpl(delegateFactory);
        when(delegateFactory.create(isA(Model.class), eq(pollClient), eq(closeHandle))).thenReturn(component);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(delegateFactory, pollClient, closeHandle, component);
    }

    @Test
    public void createNoDiffusion() {
        final Model model = Model
            .builder()
            .build();

        final Component restAdapterClientSnapshot = factory.create(model, pollClient, closeHandle);

        assertEquals(Component.INACTIVE, restAdapterClientSnapshot);
    }

    @Test
    public void createNoServices() {
        final Model model = Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .build())
            .build();

        final Component restAdapterClientSnapshot = factory.create(model, pollClient, closeHandle);

        assertEquals(Component.INACTIVE, restAdapterClientSnapshot);
    }

    @Test
    public void createEmptyServices() {
        final Model model = Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .build())
            .services(emptyList())
            .build();

        final Component restAdapterClientSnapshot = factory.create(model, pollClient, closeHandle);

        assertEquals(Component.INACTIVE, restAdapterClientSnapshot);
    }

    @Test
    public void createEmptyEndpoints() {
        final Model model = Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .build())
            .services(singletonList(ServiceConfig
                .builder()
                .endpoints(emptyList())
                .build()))
            .build();

        final Component restAdapterClientSnapshot = factory.create(model, pollClient, closeHandle);

        assertEquals(Component.INACTIVE, restAdapterClientSnapshot);
    }

    @Test
    public void create() {
        final Model model = Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .build())
            .services(singletonList(ServiceConfig
                .builder()
                .endpoints(singletonList(EndpointConfig.builder().build()))
                .build()))
            .build();

        final Component restAdapterClientSnapshot = factory.create(model, pollClient, closeHandle);

        assertEquals(component, restAdapterClientSnapshot);
        verify(delegateFactory).create(model, pollClient, closeHandle);
    }
}
