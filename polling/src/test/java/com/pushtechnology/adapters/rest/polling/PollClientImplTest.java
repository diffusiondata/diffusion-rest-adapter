package com.pushtechnology.adapters.rest.polling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PollClientImpl}.
 *
 * @author Push Technology Limited
 */
public final class PollClientImplTest {
    @Mock
    private CloseableHttpAsyncClient httpClient;
    @Mock
    private HttpClientFactory httpClientFactory;
    @Mock
    private FutureCallback<JSON> callback;
    @Mock
    private Future<HttpResponse> future;

    private final ServiceConfig serviceConfig = ServiceConfig.builder().host("localhost").port(8080).build();
    private final EndpointConfig endpointConfig = EndpointConfig.builder().url("/a/url.json").build();

    private PollClientImpl pollClient;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(httpClientFactory.create()).thenReturn(httpClient);
        when(httpClient.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class)))
            .thenReturn(future);

        pollClient = new PollClientImpl(httpClientFactory);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(httpClient, httpClientFactory, callback);
    }

    @Test
    public void start() {
        pollClient.start();

        verify(httpClientFactory).create();
        verify(httpClient).start();
    }

    @Test(expected = IllegalStateException.class)
    public void requestBeforeStart() {
        pollClient.request(serviceConfig, endpointConfig, callback);
    }

    @Test
    public void stopBeforeRunning() throws IOException {
        pollClient.stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        start();

        final Future<?> handle = pollClient.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class));
    }

    @Test
    public void stop() throws IOException {
        start();

        pollClient.stop();

        verify(httpClient).close();
    }
}
