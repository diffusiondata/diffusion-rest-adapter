package com.pushtechnology.adapters.rest.adapter;

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
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;

/**
 * Unit tests for {@link ServiceReadyForPublishing}.
 *
 * @author Push Technology Limited
 */
public final class ServiceReadyForPublishingTest {
    @Mock
    private TopicManagementClient managementClient;
    @Mock
    private ServiceSession serviceSession;
    @Mock
    private TopicUpdateControl.Updater updater;
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

        serviceReadyHandler = new ServiceReadyForPublishing(managementClient, serviceSession, serviceConfig);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(managementClient, serviceSession, updater);
    }

    @Test
    public void acceptSuccess() {
        serviceReadyHandler.accept(updater);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAdded("topic/a");

        verify(serviceSession).addEndpoint(endpointConfig);
    }

    @Test
    public void acceptNothingToDo() {
        serviceReadyHandler.accept(updater);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAddFailed("topic/a", EXISTS);

        verify(serviceSession).addEndpoint(endpointConfig);
    }

    @Test
    public void acceptFailure() {
        serviceReadyHandler.accept(updater);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onTopicAddFailed("topic/a", PERMISSIONS_FAILURE);
    }

    @Test
    public void acceptDiscard() {
        serviceReadyHandler.accept(updater);
        verify(managementClient)
            .addEndpoint(eq(serviceConfig), eq(endpointConfig), callbackCaptor.capture());
        verify(serviceSession).start();

        callbackCaptor.getValue().onDiscard();
    }
}
