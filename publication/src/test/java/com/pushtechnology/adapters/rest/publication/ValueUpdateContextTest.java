package com.pushtechnology.adapters.rest.publication;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.RECOVERING_RECONNECT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.binary.Binary;

/**
 * Unit tests for {@link ValueUpdateContext}.
 *
 * @author Matt Champion 21/12/2017
 */
public final class ValueUpdateContextTest {
    @Mock
    private Session session;
    @Mock
    private TopicUpdateControl.Updater updater;
    @Mock
    private Binary binary;
    @Mock
    private PublicationListener publicationListener;
    @Mock
    private DataType<Binary> dataType;
    @Mock
    private Bytes bytes;
    @Mock
    private TopicUpdateControl updateControl;

    private ValueUpdateContext<Binary> updateContext;

    @Before
    public void setUp() {
        initMocks(this);

        when(dataType.toBytes(binary)).thenReturn(binary);
        when(bytes.toByteArray()).thenReturn(new byte[0]);
        when(session.feature(TopicUpdateControl.class)).thenReturn(updateControl);

        updateContext = new ValueUpdateContext<>(session, updater, "a/topic", dataType, publicationListener);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(session, updater, publicationListener, dataType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublish() {
        when(session.getState()).thenReturn(CONNECTED_ACTIVE);
        updateContext.publish(binary);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(TopicUpdateControl.Updater.UpdateContextCallback.class));
        verify(publicationListener).onPublicationRequest("a/topic", 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRecovery() {
        when(session.getState()).thenReturn(RECOVERING_RECONNECT);
        updateContext.publish(binary);
        verify(updater, never()).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(TopicUpdateControl.Updater.UpdateContextCallback.class));
        verify(publicationListener).onPublicationRequest("a/topic", 0);

        updateContext.onSessionStateChanged(session, RECOVERING_RECONNECT, CONNECTED_ACTIVE);

        verify(session).getState();
        verify(dataType).toBytes(binary);
        verify(updater).update(eq("a/topic"), eq(binary), eq("a/topic"), isA(TopicUpdateControl.Updater.UpdateContextCallback.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testClosed() {
        when(session.getState()).thenReturn(CLOSED_BY_CLIENT);

        try {
            updateContext.publish(binary);
        }
        finally {
            verify(session).getState();
        }
    }
}
