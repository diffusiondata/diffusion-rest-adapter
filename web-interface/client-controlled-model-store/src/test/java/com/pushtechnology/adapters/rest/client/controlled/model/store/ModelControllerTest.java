/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.client.controlled.model.store;

import static com.pushtechnology.adapters.rest.model.latest.MetricsConfig.Type.OFF;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl.RequestHandler.Responder;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;

/**
 * Unit tests for {@link ModelController}.
 *
 * @author Push Technology Limited
 */
public final class ModelControllerTest {

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private SessionId sessionId;

    @Mock
    private ReceiveContext context;

    @Mock
    private Responder<Map<String, Object>> responder;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private AsyncMutableModelStore modelStore;

    @Before
    public void setUp() {
        initMocks(this);

        modelStore = new AsyncMutableModelStore(executor);

        // Set the initial model
        modelStore.setModel(Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .host("localhost")
                .principal("control")
                .password("password")
                .build())
            .services(emptyList())
            .metrics(MetricsConfig
                .builder()
                .type(OFF)
                .build())
            .build());
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(executor, sessionId, context, responder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onEmptyMessage() {
        final ModelController controller = new ModelController(modelStore);

        controller.onRequest(emptyMap(), responder);
        verify(responder).respond(isA(Map.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onUnknownMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> unknownTypeMessage = new HashMap<>();
        unknownTypeMessage.put("type", "ha, ha");

        controller.onRequest(unknownTypeMessage, responder);
        verify(responder).respond(isA(Map.class));
    }

    @Test
    public void onCreateServiceMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-service");
        final Map<String, Object> service = new HashMap<>();
        service.put("name", "");
        service.put("host", "");
        service.put("port", 80);
        service.put("secure", false);
        service.put("pollPeriod", 50000);
        service.put("topicPathRoot", "");

        message.put("service", service);

        controller.onRequest(message, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertEquals(1, services.size());
        assertEquals(
            ServiceConfig
                .builder()
                .name("")
                .host("")
                .port(80)
                .secure(false)
                .pollPeriod(50000)
                .topicPathRoot("")
                .endpoints(emptyList())
                .build(),
            services.get(0));
    }

    @Test
    public void onCreateServiceWithBasicAuthenticationMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-service");
        final Map<String, Object> basicAuthentication = new HashMap<>();
        basicAuthentication.put("userid", "user");
        basicAuthentication.put("password", "password");
        final Map<String, Object> security = new HashMap<>();
        security.put("basic", basicAuthentication);
        final Map<String, Object> service = new HashMap<>();
        service.put("name", "");
        service.put("host", "");
        service.put("port", 80);
        service.put("secure", false);
        service.put("pollPeriod", 50000);
        service.put("topicPathRoot", "");
        service.put("security", security);

        message.put("service", service);

        controller.onRequest(message, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertEquals(1, services.size());
        assertEquals(
            ServiceConfig
                .builder()
                .name("")
                .host("")
                .port(80)
                .secure(false)
                .pollPeriod(50000)
                .topicPathRoot("")
                .endpoints(emptyList())
                .security(SecurityConfig
                    .builder()
                    .basic(BasicAuthenticationConfig
                        .builder()
                        .userid("user")
                        .password("password")
                        .build())
                    .build())
                .build(),
            services.get(0));
    }

    @Test
    public void createTwoServices() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message0 = new HashMap<>();
        message0.put("type", "create-service");
        final Map<String, Object> service0 = new HashMap<>();
        service0.put("name", "a");
        service0.put("host", "a");
        service0.put("port", 80);
        service0.put("secure", false);
        service0.put("pollPeriod", 50000);
        service0.put("topicPathRoot", "a");
        message0.put("service", service0);

        controller.onRequest(message0, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final Map<String, Object> message1 = new HashMap<>();
        message1.put("type", "create-service");
        final Map<String, Object> service1 = new HashMap<>();
        service1.put("name", "b");
        service1.put("host", "b");
        service1.put("port", 80);
        service1.put("secure", false);
        service1.put("pollPeriod", 50000);
        service1.put("topicPathRoot", "b");
        message1.put("service", service1);

        controller.onRequest(message1, responder);

        verify(executor, times(3)).execute(runnableCaptor.capture());
        verify(responder, times(2)).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertEquals(2, services.size());
        assertThat(
            services,
            contains(
                ServiceConfig
                    .builder()
                    .name("a")
                    .host("a")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("a")
                    .endpoints(emptyList())
                    .build(),
                ServiceConfig
                    .builder()
                    .name("b")
                    .host("b")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("b")
                    .endpoints(emptyList())
                    .build()));
    }

    @Test
    public void onDeleteServiceNoServiceName() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-service");

        controller.onRequest(message, responder);

        verify(executor, times(1)).execute(runnableCaptor.capture());
        verify(responder).reject("No service name provided");
    }

    @Test
    public void onDeleteServiceMissingService() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-service");
        message.put("serviceName", "b");

        controller.onRequest(message, responder);

        verify(executor).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());
    }

    @Test
    public void createTwoServicesAndDelete() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message0 = new HashMap<>();
        message0.put("type", "create-service");
        final Map<String, Object> service0 = new HashMap<>();
        service0.put("name", "a");
        service0.put("host", "a");
        service0.put("port", 80);
        service0.put("secure", false);
        service0.put("pollPeriod", 50000);
        service0.put("topicPathRoot", "a");
        message0.put("service", service0);

        controller.onRequest(message0, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final Map<String, Object> message1 = new HashMap<>();
        message1.put("type", "create-service");
        final Map<String, Object> service1 = new HashMap<>();
        service1.put("name", "b");
        service1.put("host", "b");
        service1.put("port", 80);
        service1.put("secure", false);
        service1.put("pollPeriod", 50000);
        service1.put("topicPathRoot", "b");
        message1.put("service", service1);

        controller.onRequest(message1, responder);

        verify(executor, times(3)).execute(runnableCaptor.capture());
        verify(responder, times(2)).respond(emptyMap());

        final Map<String, Object> deleteServiceMessage = new HashMap<>();
        deleteServiceMessage.put("type", "delete-service");
        deleteServiceMessage.put("serviceName", "b");

        controller.onRequest(deleteServiceMessage, responder);

        verify(executor, times(4)).execute(runnableCaptor.capture());
        verify(responder, times(3)).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertThat(
            services,
            contains(
                ServiceConfig
                    .builder()
                    .name("a")
                    .host("a")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("a")
                    .endpoints(emptyList())
                    .build()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void onCreateServiceMessageWithoutService() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-service");

        controller.onRequest(message, responder);
        verify(responder).reject("no service provided");
    }

    @Test
    public void onCreateEndpointMessageMissingService() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "create-endpoint");
        message.put("serviceName", "missing-service");
        final Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("name", "endpoint-0");
        endpoint.put("topicPath", "a/topic");
        endpoint.put("url", "/a/url");
        endpoint.put("produces", "json");

        message.put("endpoint", endpoint);

        controller.onRequest(message, responder);

        verify(executor).execute(runnableCaptor.capture());
        verify(responder).reject("service missing");
    }

    @Test
    public void onCreateEndpointMessage() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> serviceMessage = new HashMap<>();
        serviceMessage.put("type", "create-service");
        final Map<String, Object> service = new HashMap<>();
        service.put("name", "service-0");
        service.put("host", "a");
        service.put("port", 80);
        service.put("secure", false);
        service.put("pollPeriod", 50000);
        service.put("topicPathRoot", "a");
        serviceMessage.put("service", service);

        controller.onRequest(serviceMessage, responder);

        verify(executor, times(2)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final Map<String, Object> endpointMessage = new HashMap<>();
        endpointMessage.put("type", "create-endpoint");
        endpointMessage.put("serviceName", "service-0");
        final Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("name", "endpoint-0");
        endpoint.put("topicPath", "a/topic");
        endpoint.put("url", "/a/url");
        endpoint.put("produces", "json");

        endpointMessage.put("endpoint", endpoint);

        controller.onRequest(endpointMessage, responder);

        verify(executor, times(3)).execute(runnableCaptor.capture());
        verify(responder, times(2)).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertEquals(1, services.size());
        assertEquals(
            ServiceConfig
                .builder()
                .name("service-0")
                .host("a")
                .port(80)
                .secure(false)
                .pollPeriod(50000)
                .topicPathRoot("a")
                .endpoints(singletonList(EndpointConfig
                    .builder()
                    .name("endpoint-0")
                    .topicPath("a/topic")
                    .url("/a/url")
                    .produces("json")
                    .build()))
                .build(),
            services.get(0));
    }

    @Test
    public void createEndpointMessageWithTwoServices() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> serviceMessage0 = new HashMap<>();
        serviceMessage0.put("type", "create-service");
        final Map<String, Object> service0 = new HashMap<>();
        service0.put("name", "service-0");
        service0.put("host", "a");
        service0.put("port", 80);
        service0.put("secure", false);
        service0.put("pollPeriod", 50000);
        service0.put("topicPathRoot", "a");
        serviceMessage0.put("service", service0);

        final Map<String, Object> serviceMessage1 = new HashMap<>();
        serviceMessage1.put("type", "create-service");
        final Map<String, Object> service1 = new HashMap<>();
        service1.put("name", "service-1");
        service1.put("host", "b");
        service1.put("port", 80);
        service1.put("secure", false);
        service1.put("pollPeriod", 50000);
        service1.put("topicPathRoot", "b");
        serviceMessage1.put("service", service1);

        controller.onRequest(serviceMessage0, responder);
        controller.onRequest(serviceMessage1, responder);

        verify(executor, times(3)).execute(runnableCaptor.capture());
        verify(responder, times(2)).respond(emptyMap());

        final Map<String, Object> endpointMessage = new HashMap<>();
        endpointMessage.put("type", "create-endpoint");
        endpointMessage.put("serviceName", "service-0");
        final Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("name", "endpoint-0");
        endpoint.put("topicPath", "a/topic");
        endpoint.put("url", "/a/url");
        endpoint.put("produces", "json");

        endpointMessage.put("endpoint", endpoint);

        controller.onRequest(endpointMessage, responder);

        verify(executor, times(4)).execute(runnableCaptor.capture());
        verify(responder, times(3)).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertThat(
            services,
            contains(
                ServiceConfig
                    .builder()
                    .name("service-0")
                    .host("a")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("a")
                    .endpoints(singletonList(EndpointConfig
                        .builder()
                        .name("endpoint-0")
                        .topicPath("a/topic")
                        .url("/a/url")
                        .produces("json")
                        .build()))
                    .build(),
                ServiceConfig
                    .builder()
                    .name("service-1")
                    .host("b")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("b")
                    .endpoints(emptyList())
                    .build()));
    }

    @Test
    public void onListServices() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "list-services");

        controller.onRequest(message, responder);

        verify(executor).execute(runnableCaptor.capture());
        final List<Object> services = emptyList();
        final Map<String, Object> response = new HashMap<>();
        response.put("services", services);
        verify(responder).respond(response);
    }

    @Test
    public void onDeleteEndpointNoServiceName() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-endpoint");

        controller.onRequest(message, responder);

        verify(executor, times(1)).execute(runnableCaptor.capture());
        verify(responder).reject("No service name provided");
    }

    @Test
    public void onDeleteEndpointNoEndpointName() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-endpoint");
        message.put("serviceName", "service-0");

        controller.onRequest(message, responder);

        verify(executor).execute(runnableCaptor.capture());
        verify(responder).reject("No endpoint name provided");
    }

    @Test
    public void onDeleteEndpointMissingService() {
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-endpoint");
        message.put("serviceName", "service-0");
        message.put("endpointName", "endpoint-0");

        controller.onRequest(message, responder);

        verify(executor).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());
    }

    @Test
    public void onDeleteEndpoint() {
        modelStore.setModel(Model
            .builder()
            .diffusion(DiffusionConfig
                .builder()
                .host("localhost")
                .principal("control")
                .password("password")
                .build())
            .services(asList(
                ServiceConfig
                    .builder()
                    .name("service-0")
                    .host("a")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("a")
                    .endpoints(singletonList(
                        EndpointConfig
                            .builder()
                            .name("endpoint-0")
                            .url("/a/url")
                            .topicPath("a/topic")
                            .produces("json")
                            .build()))
                    .build(),
                ServiceConfig
                    .builder()
                    .name("service-1")
                    .host("b")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("b")
                    .endpoints(singletonList(
                        EndpointConfig
                            .builder()
                            .name("endpoint-0")
                            .url("/a/url")
                            .topicPath("a/topic")
                            .produces("json")
                            .build()))
                    .build()
            ))
            .metrics(MetricsConfig
                .builder()
                .type(OFF)
                .build())
            .build());
        final ModelController controller = new ModelController(modelStore);

        final Map<String, Object> message = new HashMap<>();
        message.put("type", "delete-endpoint");
        message.put("serviceName", "service-0");
        message.put("endpointName", "endpoint-0");

        controller.onRequest(message, responder);

        verify(executor, times(3)).execute(runnableCaptor.capture());
        verify(responder).respond(emptyMap());

        final List<ServiceConfig> services = modelStore.get().getServices();
        assertThat(
            services,
            contains(
                ServiceConfig
                    .builder()
                    .name("service-0")
                    .host("a")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("a")
                    .endpoints(emptyList())
                    .build(),
                ServiceConfig
                    .builder()
                    .name("service-1")
                    .host("b")
                    .port(80)
                    .secure(false)
                    .pollPeriod(50000)
                    .topicPathRoot("b")
                    .endpoints(singletonList(
                        EndpointConfig
                            .builder()
                            .name("endpoint-0")
                            .topicPath("a/topic")
                            .url("/a/url")
                            .produces("json")
                            .build()))
                    .build()));
    }
}
