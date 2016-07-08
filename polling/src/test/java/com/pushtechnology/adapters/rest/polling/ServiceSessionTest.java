package com.pushtechnology.adapters.rest.polling;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.PublishingClient;
import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link ServiceSession}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionTest {
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private PollClient pollClient;
    @Mock
    private PublishingClient diffusionClient;
    @Mock
    private JSON json;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    @Captor
    private ArgumentCaptor<FutureCallback<JSON>> callbackCaptor;

    private final Endpoint endpoint = Endpoint
        .builder()
        .url("/a/url")
        .build();
    private final Service service = Service
        .builder()
        .host("localhost")
        .port(80)
        .pollPeriod(5000L)
        .endpoints(singletonList(endpoint))
        .build();

    private ServiceSession serviceSession;

    @Before
    public void setUp() {
        initMocks(this);

        serviceSession = new ServiceSession(executor, pollClient, service, diffusionClient);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, pollClient, diffusionClient);
    }

    @Test
    public void startSuccessfulPoll() {
        serviceSession.start();

        verify(executor).schedule(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        final FutureCallback<JSON> callback = callbackCaptor.getValue();

        callback.completed(json);

        verify(diffusionClient).publish(endpoint, json);
        verify(executor).schedule(isA(Runnable.class), eq(5000L), eq(MILLISECONDS));
    }

    @Test
    public void startFailedPoll() {
        serviceSession.start();

        verify(executor).schedule(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        final FutureCallback<JSON> callback = callbackCaptor.getValue();

        callback.failed(new Exception("Intentional exception"));

        verify(executor).schedule(isA(Runnable.class), eq(5000L), eq(MILLISECONDS));
    }

    @Test
    public void stop() {
        serviceSession.start();

        verify(executor).schedule(runnableCaptor.capture(), eq(0L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        serviceSession.stop();

        runnable.run();
    }
}
