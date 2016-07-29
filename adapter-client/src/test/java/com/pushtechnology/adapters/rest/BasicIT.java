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

package com.pushtechnology.adapters.rest;

import static com.pushtechnology.diffusion.client.features.Topics.UnsubscribeReason.REMOVAL;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.MutableModelStore;
import com.pushtechnology.adapters.rest.resources.IncrementingResource;
import com.pushtechnology.adapters.rest.resources.TimestampResource;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Basic integration test for adapting resources.
 *
 * @author Push Technology Limited
 */
public final class BasicIT {
    private static final DiffusionConfig DIFFUSION_CONFIG = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .principal("control")
        .password("password")
        .build();
    private static final EndpointConfig INCREMENT_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topic("increment")
        .url("/rest/increment")
        .build();
    private static final EndpointConfig TIMESTAMP_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topic("timestamp")
        .url("/rest/timestamp")
        .build();
    private static final EndpointConfig AUTHENTICATED_INCREMENT_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topic("increment")
        .url("/auth/rest/increment")
        .build();
    private static final EndpointConfig AUTHENTICATED_TIMESTAMP_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topic("timestamp")
        .url("/auth/rest/timestamp")
        .build();
    private static final BasicAuthenticationConfig BASIC_AUTHENTICATION_CONFIG = BasicAuthenticationConfig
        .builder()
        .principal("principal")
        .credential("credential")
        .build();
    private static final ServiceConfig INSECURE_SERVICE = ServiceConfig
        .builder()
        .host("localhost")
        .port(8081)
        .pollPeriod(500)
        .topicRoot("rest")
        .endpoints(asList(INCREMENT_ENDPOINT, TIMESTAMP_ENDPOINT))
        .build();
    private static final ServiceConfig SECURE_SERVICE = ServiceConfig
        .builder()
        .host("localhost")
        .port(8444)
        .secure(true)
        .pollPeriod(500)
        .topicRoot("restTLS")
        .security(SecurityConfig.builder().basic(BASIC_AUTHENTICATION_CONFIG).build())
        .endpoints(asList(AUTHENTICATED_INCREMENT_ENDPOINT, AUTHENTICATED_TIMESTAMP_ENDPOINT))
        .build();
    private static final Model EMPTY_MODEL = Model.builder().active(true).build();
    private static final Model DIFFUSION_ONLY = Model
        .builder()
        .active(true)
        .diffusion(DIFFUSION_CONFIG)
        .truststore("testKeystore.jks")
        .build();
    private static final Model SERVICES_ONLY = Model
        .builder()
        .active(true)
        .services(asList(INSECURE_SERVICE, SECURE_SERVICE))
        .truststore("testKeystore.jks")
        .build();
    private static final Model INSECURE = Model
        .builder()
        .active(true)
        .diffusion(DIFFUSION_CONFIG)
        .services(singletonList(INSECURE_SERVICE))
        .truststore("testKeystore.jks")
        .build();
    private static final Model FULL_MODEL = Model
        .builder()
        .active(true)
        .diffusion(DIFFUSION_CONFIG)
        .services(asList(INSECURE_SERVICE, SECURE_SERVICE))
        .truststore("testKeystore.jks")
        .build();

    private static Server jettyServer;

    @Mock
    private Session.Listener listener;
    @Mock
    private ServiceListener serviceListener;
    @Mock
    private Topics.ValueStream<JSON> stream;
    @Mock
    private Topics.CompletionCallback callback;

    private MutableModelStore modelStore;

    @BeforeClass
    public static void startApplicationServer() throws Exception {
        final ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context0.setContextPath("/rest");
        final ServletHolder jerseyServlet0 = context0.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet0.setInitOrder(0);
        jerseyServlet0.setInitParameter(
            "jersey.config.server.provider.classnames",
            TimestampResource.class.getCanonicalName() + "," + IncrementingResource.class.getCanonicalName());

        final Constraint constraint = new Constraint();
        constraint.setName("constraint-0");
        constraint.setRoles(new String[]{ "test" });
        constraint.setAuthenticate(true);

        final ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        final HashLoginService loginService = new HashLoginService("login-service-0");
        loginService.putUser("principal", Credential.getCredential("credential"), new String[]{ "test" });

        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.addConstraintMapping(constraintMapping);
        securityHandler.setLoginService(loginService);
        securityHandler.setRealmName("realm-name0");

        final ServletContextHandler context1 = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context1.setContextPath("/auth/rest");
        context1.setSecurityHandler(securityHandler);
        final ServletHolder jerseyServlet1 = context1.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet1.setInitOrder(0);
        jerseyServlet1.setInitParameter(
            "jersey.config.server.provider.classnames",
            TimestampResource.class.getCanonicalName() + "," + IncrementingResource.class.getCanonicalName());

        final HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(context0);
        handlers.addHandler(context1);

        jettyServer = new Server();
        jettyServer.setHandler(handlers);

        final ServerConnector httpConnector = new ServerConnector(jettyServer);
        httpConnector.setPort(8081);

        final HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory(true);
        sslContextFactory.setKeyStorePath(BasicIT.class.getResource("/testKeystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setExcludeProtocols();
        sslContextFactory.setExcludeCipherSuites();

        final ServerConnector httpsConnector = new ServerConnector(jettyServer,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(8444);

        jettyServer.setConnectors(new Connector[] { httpConnector, httpsConnector });

        jettyServer.start();
    }

    @AfterClass
    public static void stopApplicationServer() throws Exception {
        jettyServer.stop();
    }

    @Before
    public void setup() {
        initMocks(this);

        modelStore = new MutableModelStore();
        modelStore.setModel(FULL_MODEL);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(listener, callback, serviceListener);
    }

    @Test
    public void testInitialisation() throws IOException {
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onActive(SECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?rest/", callback);
        topics.subscribe("?restTLS/", callback);

        verify(callback, timed().times(2)).onComplete();
        verify(stream, timed()).onSubscription(eq("rest/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("rest/increment"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("restTLS/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("restTLS/increment"), isA(TopicSpecification.class));

        verify(stream, timed()).onValue(eq("rest/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("rest/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("restTLS/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("restTLS/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(SECURE_SERVICE);
        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testReconfigurationFromInactiveToActive() throws IOException {
        modelStore.setModel(EMPTY_MODEL);
        final RESTAdapterClient client = startClient();

        verify(serviceListener, never()).onActive(SECURE_SERVICE);
        verify(serviceListener, never()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?rest/", callback);
        topics.subscribe("?restTLS/", callback);

        verify(callback, timed().times(2)).onComplete();

        modelStore.setModel(FULL_MODEL);

        verify(serviceListener, timed()).onActive(SECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        verify(stream, timed()).onSubscription(eq("rest/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("rest/increment"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("restTLS/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("restTLS/increment"), isA(TopicSpecification.class));

        verify(stream, timed()).onValue(eq("rest/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("rest/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("restTLS/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("restTLS/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(SECURE_SERVICE);
        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testReconfigurationFromDiffusionOnlyToInsecure() throws IOException {
        modelStore.setModel(DIFFUSION_ONLY);
        final RESTAdapterClient client = startClient();

        verify(serviceListener, never()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?rest/", callback);
        topics.subscribe("?restTLS/", callback);

        verify(callback, timed().times(2)).onComplete();

        modelStore.setModel(INSECURE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        verify(stream, timed()).onSubscription(eq("rest/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("rest/increment"), isA(TopicSpecification.class));

        verify(stream, timed()).onValue(eq("rest/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("rest/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testReconfigurationFromInsecureToDiffusionOnly() throws IOException {
        modelStore.setModel(INSECURE);
        final RESTAdapterClient client = startClient();
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);
        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?rest/", callback);
        topics.subscribe("?restTLS/", callback);

        verify(callback, timed().times(2)).onComplete();

        verify(stream, timed()).onSubscription(eq("rest/timestamp"), isA(TopicSpecification.class));
        verify(stream, timed()).onSubscription(eq("rest/increment"), isA(TopicSpecification.class));

        verify(stream, timed()).onValue(eq("rest/timestamp"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));
        verify(stream, timed()).onValue(eq("rest/increment"), isA(TopicSpecification.class), isNull(JSON.class), isA(JSON.class));

        modelStore.setModel(DIFFUSION_ONLY);

        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);

        verify(stream, timed()).onUnsubscription(eq("rest/timestamp"), isA(TopicSpecification.class), eq(REMOVAL));
        verify(stream, timed()).onUnsubscription(eq("rest/increment"), isA(TopicSpecification.class), eq(REMOVAL));

        stopSession(session);
        client.close();
    }

    private static VerificationWithTimeout timed() {
        return timeout(5000);
    }

    private RESTAdapterClient startClient() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final RESTAdapterClient client = RESTAdapterClient.create(
            modelStore,
            executor,
            executor::shutdown,
            serviceListener);
        client.start();
        return client;
    }

    private Session startSession() {
        final Session session = Diffusion
            .sessions()
            .serverHost("localhost")
            .serverPort(8080)
            .listener(listener)
            .open();
        verify(listener, timed()).onSessionStateChanged(session, CONNECTING, CONNECTED_ACTIVE);
        return session;
    }

    private void stopSession(Session session) {
        session.close();
        verify(listener, timed()).onSessionStateChanged(session, CONNECTED_ACTIVE, CLOSED_BY_CLIENT);
    }
}
