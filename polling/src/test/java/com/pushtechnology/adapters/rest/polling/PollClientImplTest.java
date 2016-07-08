package com.pushtechnology.adapters.rest.polling;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link PollClientImpl}.
 *
 * @author Matt Champion on 08/07/2016
 */
public final class PollClientImplTest {
    @Mock
    private CloseableHttpAsyncClient httpClient;
    @Mock
    private HttpClientFactory httpClientFactory;
    @Mock
    private FutureCallback<JSON> callback;

    private final Service service = Service.builder().host("localhost").port(8080).build();
    private final Endpoint endpoint = Endpoint.builder().url("/a/url.json").build();

    private PollClientImpl pollClient;

    @Before
    public void setUp() {
        initMocks(this);

        when(httpClientFactory.create()).thenReturn(httpClient);

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
        pollClient.request(service, endpoint, callback);
    }

    @Test
    public void stopBeforeRunning() throws IOException {
        pollClient.stop();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        start();

        pollClient.request(service, endpoint, callback);

        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class));
    }

    @Test
    public void stop() throws IOException {
        start();

        pollClient.stop();

        verify(httpClient).close();
    }
}