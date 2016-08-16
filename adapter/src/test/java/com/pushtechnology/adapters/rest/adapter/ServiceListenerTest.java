package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for {@link ServiceListener}.
 *
 * @author Push Technology Limited
 */
public final class ServiceListenerTest {

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void onActive() {
        ServiceListener.NULL_LISTENER.onActive(null);
    }

    @Test
    public void onStandby() {
        ServiceListener.NULL_LISTENER.onStandby(null);
    }

    @Test
    public void onRemove() {
        ServiceListener.NULL_LISTENER.onRemove(null);
    }
}
