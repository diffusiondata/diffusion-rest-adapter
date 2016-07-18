package com.pushtechnology.adapters.rest.client;

import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.EXISTS;
import static com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason.PERMISSIONS_FAILURE;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;

/**
 * Unit tests for {@link ServiceReadyForPublishing}.
 *
 * @author Matt Champion on 18/07/2016
 */
public final class ServiceReadyForPublishingTest {
    @Mock
    private TopicManagementClient managementClient;
    @Mock
    private ServiceSession serviceSession;
    @Captor
    private ArgumentCaptor<TopicControl.AddCallback> callbackCaptor;

    private final EndpointConfig endpointConfig = EndpointConfig
        .builder()
        .name("endpoint-0")
        .topic("topic")
        .url("http://localhost/json")
        .build();

    private final ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .host("localhost")
        .port(8080)
        .pollPeriod(60000)
        .endpoints(singletonList(endpointConfig))
        .topicRoot("a")
        .build();

    private ServiceReadyForPublishing serviceReadyHandler;

    @Before
    public void setUp() {
        initMocks(this);

        serviceReadyHandler = new ServiceReadyForPublishing(managementClient, serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(managementClient, serviceSession);
    }

    @Test
    public void acceptSuccess() {
        serviceReadyHandler.accept(serviceConfig);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAdded("topic/a");

        verify(serviceSession).addEndpoint(endpointConfig);
    }

    @Test
    public void acceptNothingToDo() {
        serviceReadyHandler.accept(serviceConfig);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAddFailed("topic/a", EXISTS);

        verify(serviceSession).addEndpoint(endpointConfig);
    }

    @Test
    public void acceptFailure() {
        serviceReadyHandler.accept(serviceConfig);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAddFailed("topic/a", PERMISSIONS_FAILURE);
    }

    @Test
    public void acceptDiscard() {
        serviceReadyHandler.accept(serviceConfig);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onDiscard();
    }
}
