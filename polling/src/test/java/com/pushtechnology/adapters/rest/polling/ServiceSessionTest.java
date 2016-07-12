package com.pushtechnology.adapters.rest.polling;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.concurrent.FutureCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.PublishingClient;
import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
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
    @Mock
    private ScheduledFuture taskFuture;
    @Mock
    private Future pollFuture0;
    @Mock
    private Future pollFuture1;
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

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        serviceSession = new ServiceSession(executor, pollClient, service, diffusionClient);
        when(executor
            .scheduleWithFixedDelay(isA(Runnable.class), isA(Long.class), isA(Long.class), isA(TimeUnit.class)))
            .thenReturn(taskFuture);
        when(pollClient
            .request(isA(Service.class), isA(Endpoint.class), isA(FutureCallback.class)))
            .thenReturn(pollFuture0, pollFuture1);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, pollClient, diffusionClient, taskFuture);
    }

    @Test
    public void startSuccessfulPoll() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        final FutureCallback<JSON> callback = callbackCaptor.getValue();

        callback.completed(json);

        verify(diffusionClient).publish(endpoint, json);
    }

    @Test
    public void startFailedPoll() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        final FutureCallback<JSON> callback = callbackCaptor.getValue();

        callback.failed(new Exception("Intentional exception"));
    }

    @Test
    public void stop() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        serviceSession.stop();

        verify(taskFuture).cancel(false);
    }

    @Test
    public void stopBeforePoll() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        serviceSession.stop();

        verify(taskFuture).cancel(false);
        verify(pollFuture0).cancel(false);
    }

    @Test
    public void stopBeforeSecondPoll() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        runnable.run();

        verify(pollClient, times(2)).request(eq(service), eq(endpoint), callbackCaptor.capture());

        serviceSession.stop();

        verify(taskFuture).cancel(false);
        verify(pollFuture1).cancel(false);
    }

    @Test
    public void stopDuringPoll() {
        serviceSession.start();

        verify(executor).scheduleWithFixedDelay(runnableCaptor.capture(), eq(0L), eq(5000L), eq(MILLISECONDS));

        final Runnable runnable = runnableCaptor.getValue();

        runnable.run();

        verify(pollClient).request(eq(service), eq(endpoint), callbackCaptor.capture());

        final FutureCallback<JSON> callback = callbackCaptor.getValue();

        serviceSession.stop();
        verify(taskFuture).cancel(false);

        callback.completed(json);
    }
}
