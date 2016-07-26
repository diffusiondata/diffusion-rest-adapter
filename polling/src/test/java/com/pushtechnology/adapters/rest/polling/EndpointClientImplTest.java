package com.pushtechnology.adapters.rest.polling;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link EndpointClientImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointClientImplTest {
    @Mock
    private HttpClientFactory clientFactory;
    @Mock
    private CloseableHttpAsyncClient httpClient;
    @Mock
    private FutureCallback<JSON> callback;
    @Mock
    private Future<HttpResponse> future;
    @Mock
    private HttpResponse response;
    @Mock
    private HttpEntity entity;
    @Captor
    private ArgumentCaptor<FutureCallback<HttpResponse>> callbackCaptor;

    private final EndpointConfig endpointConfig = EndpointConfig.builder().url("/a/url.json").build();
    private final ServiceConfig serviceConfig = ServiceConfig.builder().host("localhost").port(8080).build();
    private final Model model = Model.builder().services(singletonList(serviceConfig)).build();

    private EndpointClientImpl httpComponent;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(clientFactory.create(model, null)).thenReturn(httpClient);
        when(httpClient.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class)))
            .thenReturn(future);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(new ByteArrayInputStream("{}".getBytes()));
        when(response.getHeaders("content-type")).thenReturn(new Header[0]);

        httpComponent = new EndpointClientImpl(model, null, clientFactory);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(httpClient, callback, callback);
    }

    @Test
    public void start() {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final Future<?> handle =  httpComponent.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class));
    }

    @Test
    public void requestResponse() {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final Future<?> handle =  httpComponent.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), callbackCaptor.capture());

        final FutureCallback<HttpResponse> responseCallback = callbackCaptor.getValue();

        responseCallback.completed(response);
        verify(callback).completed(Diffusion
            .dataTypes()
            .json()
            .fromJsonString("{}"));
    }

    @Test
    public void requestResponseFailed() {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final Future<?> handle =  httpComponent.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), callbackCaptor.capture());

        final FutureCallback<HttpResponse> responseCallback = callbackCaptor.getValue();

        final Exception exception = new Exception("Intentional for test");
        responseCallback.failed(exception);
        verify(callback).failed(exception);
    }

    @Test
    public void requestResponseCancelled() {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        final Future<?> handle =  httpComponent.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), callbackCaptor.capture());

        final FutureCallback<HttpResponse> responseCallback = callbackCaptor.getValue();

        responseCallback.cancelled();
        verify(callback).cancelled();
    }

    @Test(expected = IllegalStateException.class)
    public void requestBeforeStart() {
        httpComponent.request(serviceConfig, endpointConfig, callback);
    }

    @Test
    public void close() throws IOException {
        httpComponent.start();

        verify(clientFactory).create(model, null);
        verify(httpClient).start();

        httpComponent.close();

        verify(httpClient).close();
    }
}
