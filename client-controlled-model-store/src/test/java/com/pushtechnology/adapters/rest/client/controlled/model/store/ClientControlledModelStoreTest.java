package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link ClientControlledModelStore}.
 *
 * @author Matt Champion on 19/08/2016
 */
public final class ClientControlledModelStoreTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private DiffusionConfig diffusionConfig = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();

    @Before
    public void setUp() {
        initMocks(this);

        when(sessionFactory.transports(WEBSOCKET)).thenReturn(sessionFactory);
        when(sessionFactory.listener(isA(Session.Listener.class))).thenReturn(sessionFactory);
        when(sessionFactory.serverHost("localhost")).thenReturn(sessionFactory);
        when(sessionFactory.serverPort(8080)).thenReturn(sessionFactory);
        when(sessionFactory.secureTransport(false)).thenReturn(sessionFactory);
        when(sessionFactory.principal("control")).thenReturn(sessionFactory);
        when(sessionFactory.password("password")).thenReturn(sessionFactory);
        when(sessionFactory.connectionTimeout(10000)).thenReturn(sessionFactory);
        when(sessionFactory.reconnectionTimeout(10000)).thenReturn(sessionFactory);
        when(sessionFactory.maximumMessageSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.inputBufferSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.outputBufferSize(32000)).thenReturn(sessionFactory);
        when(sessionFactory.recoveryBufferSize(256)).thenReturn(sessionFactory);
        when(sessionFactory.open()).thenReturn(session);
    }

    @Test
    public void startClose() {
        final ClientControlledModelStore modelStore = new ClientControlledModelStore(diffusionConfig, null);
        modelStore.start(new DiffusionSessionFactory(sessionFactory));

        verify(sessionFactory).open();

        modelStore.close();
    }
}
