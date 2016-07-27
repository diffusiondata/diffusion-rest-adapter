package com.pushtechnology.adapters.rest.session.management;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

/**
 * Unit tests for {@link DiffusionSessionFactory}.
 *
 * @author Push Technology Limited
 */
public final class DiffusionSessionFactoryTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private final SessionLostListener listener = new SessionLostListener(null);
    private final Model noAuthModel = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("localhost")
            .port(8080)
            .build())
        .build();

    private final Model authModel = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("localhost")
            .port(8080)
            .principal("control")
            .password("password")
            .build())
        .build();

    @Before
    public void setUp() {
        initMocks(this);

        when(sessionFactory.transports(WEBSOCKET)).thenReturn(sessionFactory);
        when(sessionFactory.reconnectionTimeout(10000)).thenReturn(sessionFactory);
        when(sessionFactory.listener(listener)).thenReturn(sessionFactory);
        when(sessionFactory.serverHost("localhost")).thenReturn(sessionFactory);
        when(sessionFactory.serverPort(8080)).thenReturn(sessionFactory);
        when(sessionFactory.secureTransport(false)).thenReturn(sessionFactory);
        when(sessionFactory.principal("control")).thenReturn(sessionFactory);
        when(sessionFactory.password("password")).thenReturn(sessionFactory);
        when(sessionFactory.open()).thenReturn(session);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, sessionFactory);
    }

    @Test
    public void provideNoAuth() {
        final DiffusionSessionFactory diffusionSessionFactory = new DiffusionSessionFactory(sessionFactory);

        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).reconnectionTimeout(10000);

        assertEquals(session, diffusionSessionFactory.provide(noAuthModel, listener, null));

        verify(sessionFactory).listener(listener);
        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).open();
    }

    @Test
    public void provideAuth() {
        final DiffusionSessionFactory diffusionSessionFactory = new DiffusionSessionFactory(sessionFactory);

        verify(sessionFactory).transports(WEBSOCKET);
        verify(sessionFactory).reconnectionTimeout(10000);

        assertEquals(session, diffusionSessionFactory.provide(authModel, listener, null));

        verify(sessionFactory).listener(listener);
        verify(sessionFactory).serverHost("localhost");
        verify(sessionFactory).serverPort(8080);
        verify(sessionFactory).secureTransport(false);
        verify(sessionFactory).principal("control");
        verify(sessionFactory).password("password");
        verify(sessionFactory).open();
    }
}
