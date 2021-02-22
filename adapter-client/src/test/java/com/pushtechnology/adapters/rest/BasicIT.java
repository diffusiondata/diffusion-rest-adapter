/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.client.features.Topics.UnsubscribeReason.REMOVAL;
import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
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
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

import com.pushtechnology.adapters.rest.adapter.ServiceListener;
import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.PrometheusConfig;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.MutableModelStore;
import com.pushtechnology.adapters.rest.resources.ConstantResource;
import com.pushtechnology.adapters.rest.resources.IncrementingResource;
import com.pushtechnology.adapters.rest.resources.TimestampResource;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Basic integration test for adapting resources against embedded services.
 *
 * @author Push Technology Limited
 */
@Category(EmbeddedServices.class)
public final class BasicIT {
    private static final DiffusionConfig DIFFUSION_CONFIG = DiffusionConfig
        .builder()
        .host("localhost")
        .port(8080)
        .secure(false)
        .principal("control")
        .password("password")
        .connectionTimeout(10000)
        .reconnectionTimeout(10000)
        .maximumMessageSize(32000)
        .inputBufferSize(32000)
        .outputBufferSize(32000)
        .recoveryBufferSize(256)
        .build();
    private static final EndpointConfig INCREMENT_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topicPath("increment")
        .url("/rest/increment")
        .produces("json")
        .build();
    private static final EndpointConfig TIMESTAMP_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topicPath("timestamp")
        .url("/rest/timestamp")
        .produces("json")
        .build();
    private static final EndpointConfig INCREMENT_BINARY_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topicPath("increment")
        .url("/rest/increment")
        .produces("binary")
        .build();
    private static final EndpointConfig TIMESTAMP_BINARY_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topicPath("timestamp")
        .url("/rest/timestamp")
        .produces("binary")
        .build();
    private static final EndpointConfig INCREMENT_STRING_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topicPath("increment")
        .url("/rest/increment")
        .produces("string")
        .build();
    private static final EndpointConfig TIMESTAMP_STRING_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topicPath("timestamp")
        .url("/rest/timestamp")
        .produces("string")
        .build();
    private static final EndpointConfig TIMESTAMP_AUTO_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topicPath("timestamp")
        .url("/rest/timestamp")
        .produces("auto")
        .build();
    private static final EndpointConfig AUTHENTICATED_INCREMENT_ENDPOINT = EndpointConfig
        .builder()
        .name("increment")
        .topicPath("increment")
        .url("/auth/rest/increment")
        .produces("json")
        .build();
    private static final EndpointConfig AUTHENTICATED_TIMESTAMP_ENDPOINT = EndpointConfig
        .builder()
        .name("timestamp")
        .topicPath("timestamp")
        .url("/auth/rest/timestamp")
        .produces("json")
        .build();
    private static final EndpointConfig CONSTANT_JSON_ENDPOINT = EndpointConfig
        .builder()
        .name("constant")
        .topicPath("constant")
        .url("/rest/constant")
        .produces("json")
        .build();
    private static final EndpointConfig CONSTANT_BINARY_ENDPOINT = EndpointConfig
        .builder()
        .name("constant")
        .topicPath("constant")
        .url("/rest/constant")
        .produces("binary")
        .build();
    private static final EndpointConfig CONSTANT_STRING_ENDPOINT = EndpointConfig
        .builder()
        .name("constant")
        .topicPath("constant")
        .url("/rest/constant")
        .produces("string")
        .build();
    private static final BasicAuthenticationConfig BASIC_AUTHENTICATION_CONFIG = BasicAuthenticationConfig
        .builder()
        .userid("principal")
        .password("credential")
        .build();
    private static final ServiceConfig INSECURE_SERVICE = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/json")
        .endpoints(asList(INCREMENT_ENDPOINT, TIMESTAMP_ENDPOINT))
        .build();
    private static final ServiceConfig INSECURE_BINARY_SERVICE = ServiceConfig
        .builder()
        .name("service-1")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/binary")
        .endpoints(asList(INCREMENT_BINARY_ENDPOINT, TIMESTAMP_BINARY_ENDPOINT))
        .build();
    private static final ServiceConfig INSECURE_STRING_SERVICE = ServiceConfig
        .builder()
        .name("service-2")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/string")
        .endpoints(asList(INCREMENT_STRING_ENDPOINT, TIMESTAMP_STRING_ENDPOINT))
        .build();
    private static final ServiceConfig SECURE_SERVICE = ServiceConfig
        .builder()
        .name("service-3")
        .host("localhost")
        .port(8444)
        .secure(true)
        .pollPeriod(500)
        .topicPathRoot("rest/tls")
        .security(SecurityConfig.builder().basic(BASIC_AUTHENTICATION_CONFIG).build())
        .endpoints(asList(AUTHENTICATED_INCREMENT_ENDPOINT, AUTHENTICATED_TIMESTAMP_ENDPOINT))
        .build();
    private static final ServiceConfig INFERRED_SERVICE = ServiceConfig
        .builder()
        .name("service-4")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/auto")
        .endpoints(singletonList(TIMESTAMP_AUTO_ENDPOINT))
        .build();
    private static final ServiceConfig CONSTANT_JSON_SERVICE = ServiceConfig
        .builder()
        .name("service-5")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/json")
        .endpoints(singletonList(CONSTANT_JSON_ENDPOINT))
        .build();
    private static final ServiceConfig CONSTANT_BINARY_SERVICE = ServiceConfig
        .builder()
        .name("service-6")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/binary")
        .endpoints(singletonList(CONSTANT_BINARY_ENDPOINT))
        .build();
    private static final ServiceConfig CONSTANT_STRING_SERVICE = ServiceConfig
        .builder()
        .name("service-7")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/string")
        .endpoints(singletonList(CONSTANT_STRING_ENDPOINT))
        .build();
    private static final ServiceConfig OVERLAPPING_SERVICE_0 = ServiceConfig
        .builder()
        .name("service-8")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/overlap")
        .endpoints(singletonList(CONSTANT_STRING_ENDPOINT))
        .build();
    private static final ServiceConfig OVERLAPPING_SERVICE_1 = ServiceConfig
        .builder()
        .name("service-9")
        .host("localhost")
        .port(8081)
        .secure(false)
        .pollPeriod(500)
        .topicPathRoot("rest/overlap")
        .endpoints(singletonList(INCREMENT_STRING_ENDPOINT))
        .build();

    private static final String STRING_CONSTANT = "{\"cromulent\":\"good\",\"embiggen\":\"to make larger\"}";
    private static final JSON JSON_CONSTANT = dataTypes()
        .json()
        .fromJsonString(STRING_CONSTANT);
    private static final Binary BINARY_CONSTANT = dataTypes()
        .binary()
        .readValue(STRING_CONSTANT.getBytes(Charset.forName("UTF-8")));

    private static Server jettyServer;

    @Mock
    private Session.Listener listener;
    @Mock
    private ServiceListener serviceListener;
    @Mock
    private ServiceListener backupServiceListener;
    @Mock
    private Topics.ValueStream<JSON> stream;
    @Mock
    private Topics.ValueStream<Binary> binaryStream;
    @Mock
    private Topics.ValueStream<Long> int64Stream;
    @Mock
    private Topics.ValueStream<Double> doubleStream;
    @Mock
    private Topics.ValueStream<String> stringStream;
    @Captor
    private ArgumentCaptor<TopicSpecification> specificationCaptor;

    private MutableModelStore modelStore;

    @BeforeClass
    public static void startApplicationServer() throws Exception {
        final ServletContextHandler context0 = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context0.setContextPath("/rest");
        final ServletHolder jerseyServlet0 = context0.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet0.setInitOrder(0);
        jerseyServlet0.setInitParameter(
            "jersey.config.server.provider.classnames",
            Stream
                .<Class<?>>builder()
                .add(TimestampResource.class)
                .add(IncrementingResource.class)
                .add(ConstantResource.class)
                .build()
                .map(Class::getCanonicalName)
                .collect(joining(",")));

        final Constraint constraint = new Constraint();
        constraint.setName("constraint-0");
        constraint.setRoles(new String[]{ "test" });
        constraint.setAuthenticate(true);

        final ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        final UserStore userStore = new UserStore();
        userStore.addUser("principal", Credential.getCredential("credential"), new String[]{ "test" });
        final HashLoginService loginService = new HashLoginService("login-service-0");
        loginService.setUserStore(userStore);

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
            Stream
                .<Class<?>>builder()
                .add(TimestampResource.class)
                .add(IncrementingResource.class)
                .add(ConstantResource.class)
                .build()
                .map(Class::getCanonicalName)
                .collect(joining(",")));

        final HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(context0);
        handlers.addHandler(context1);

        jettyServer = new Server();
        jettyServer.setHandler(handlers);

        final ServerConnector httpConnector = new ServerConnector(jettyServer);
        httpConnector.setPort(8081);

        final HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory.Client(true);
        sslContextFactory.setKeyStorePath(BasicIT.class.getResource("/testKeystore.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setExcludeProtocols();
        sslContextFactory.setExcludeCipherSuites();

        final ServerConnector httpsConnector = new ServerConnector(
            jettyServer,
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
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(listener, backupServiceListener);
    }

    @Test
    public void testInitialisation() throws IOException {
        modelStore.setModel(modelWith(INSECURE_SERVICE, SECURE_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(SECURE_SERVICE);
        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(SECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/tls/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/tls/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/tls/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/tls/increment"), isNotNull(), isNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(SECURE_SERVICE);
        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testBinary() throws IOException {
        modelStore.setModel(modelWith(INSECURE_BINARY_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onActive(INSECURE_BINARY_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(Binary.class, binaryStream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(binaryStream, timed()).onSubscription(eq("rest/binary/timestamp"), isNotNull());
        verify(binaryStream, timed()).onSubscription(eq("rest/binary/increment"), isNotNull());

        verify(binaryStream, timed()).onValue(
            eq("rest/binary/timestamp"),
            isNotNull(),
            isNull(),
            isNotNull());
        verify(binaryStream, timed()).onValue(
            eq("rest/binary/increment"),
            isNotNull(),
            isNull(),
            isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(INSECURE_BINARY_SERVICE);
    }

    @Test
    public void testString() throws IOException {
        modelStore.setModel(modelWith(INSECURE_STRING_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onActive(INSECURE_STRING_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(String.class, stringStream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stringStream, timed()).onSubscription(eq("rest/string/timestamp"), isNotNull());
        verify(stringStream, timed()).onSubscription(eq("rest/string/increment"), isNotNull());

        verify(stringStream, timed()).onValue(
            eq("rest/string/timestamp"),
            isNotNull(),
            isNull(),
            isNotNull());
        verify(stringStream, timed()).onValue(
            eq("rest/string/increment"),
            isNotNull(),
            isNull(),
            isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(INSECURE_STRING_SERVICE);
    }

    @Test
    public void testReconfigurationFromInactiveToActive() throws IOException {
        modelStore.setModel(modelWith());
        final RESTAdapterClient client = startClient();

        verify(serviceListener, never()).onActive(SECURE_SERVICE);
        verify(serviceListener, never()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        modelStore.setModel(modelWith(INSECURE_SERVICE, SECURE_SERVICE));

        verify(serviceListener, timed()).onStandby(SECURE_SERVICE);
        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(SECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/tls/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/tls/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/tls/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/tls/increment"), isNotNull(), isNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(SECURE_SERVICE);
        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testReconfigurationFromDiffusionOnlyToInsecure() throws IOException {
        modelStore.setModel(modelWith());
        final RESTAdapterClient client = startClient();

        verify(serviceListener, never()).onActive(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        modelStore.setModel(modelWith(INSECURE_SERVICE));
        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
    }

    @Test
    public void testReconfigurationFromInsecureToDiffusionOnly() throws IOException {
        modelStore.setModel(modelWith(INSECURE_SERVICE));
        final RESTAdapterClient client = startClient();
        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);
        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());

        modelStore.setModel(modelWith());

        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);

        verify(stream, timed()).onUnsubscription(eq("rest/json/timestamp"), isNotNull(), eq(REMOVAL));
        verify(stream, timed()).onUnsubscription(eq("rest/json/increment"), isNotNull(), eq(REMOVAL));

        stopSession(session);
        client.close();
    }

    @Test
    public void testReconfigurationAddingAServiceToExisting() throws IOException {
        modelStore.setModel(modelWith(INSECURE_SERVICE));
        final RESTAdapterClient client = startClient();
        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);
        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addStream("?rest/json/", JSON.class, stream);
        topics.addStream("?rest/binary/", Binary.class, binaryStream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());

        modelStore.setModel(modelWith(INSECURE_SERVICE, INSECURE_BINARY_SERVICE));

        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_BINARY_SERVICE);
        verify(serviceListener, timed().times(2)).onActive(INSECURE_SERVICE);

        verify(binaryStream, timed()).onSubscription(eq("rest/binary/timestamp"), isNotNull());
        verify(binaryStream, timed()).onSubscription(eq("rest/binary/increment"), isNotNull());

        verify(binaryStream, timed()).onValue(eq("rest/binary/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(binaryStream, timed()).onValue(eq("rest/binary/increment"), isNotNull(), isNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed().times(2)).onRemove(INSECURE_SERVICE);
        verify(serviceListener, timed()).onRemove(INSECURE_BINARY_SERVICE);
    }

    @Test
    public void testStandByAndSwitchOver() throws IOException {
        modelStore.setModel(modelWith(INSECURE_SERVICE));
        final RESTAdapterClient client0 = startClient();

        verify(serviceListener, timed()).onStandby(INSECURE_SERVICE);
        verify(serviceListener, timed()).onActive(INSECURE_SERVICE);

        final RESTAdapterClient client1 = startClient(backupServiceListener);

        verify(backupServiceListener, timed()).onStandby(INSECURE_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stream, timed()).onSubscription(eq("rest/json/timestamp"), isNotNull());
        verify(stream, timed()).onSubscription(eq("rest/json/increment"), isNotNull());

        verify(stream, timed()).onValue(eq("rest/json/timestamp"), isNotNull(), isNull(), isNotNull());
        verify(stream, timed()).onValue(eq("rest/json/increment"), isNotNull(), isNull(), isNotNull());

        client0.close();
        verify(serviceListener, timed()).onRemove(INSECURE_SERVICE);
        verify(backupServiceListener, timed()).onActive(INSECURE_SERVICE);

        verify(stream, never()).onUnsubscription(eq("rest/json/timestamp"), isNotNull(), eq(REMOVAL));
        verify(stream, never()).onUnsubscription(eq("rest/json/increment"), isNotNull(), eq(REMOVAL));

        client1.close();
        verify(backupServiceListener, timed()).onRemove(INSECURE_SERVICE);

        // Depends on automatic topic removal
        verify(stream, timeout(120000L)).onUnsubscription(eq("rest/json/timestamp"), isNotNull(), eq(REMOVAL));
        verify(stream, timeout(120000L)).onUnsubscription(eq("rest/json/increment"), isNotNull(), eq(REMOVAL));

        stopSession(session);
    }

    @Test
    public void testInference() throws IOException {
        modelStore.setModel(modelWith(INFERRED_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(INFERRED_SERVICE);
        verify(serviceListener, timed()).onActive(INFERRED_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.addFallbackStream(Long.class, int64Stream);
        topics.addFallbackStream(Double.class, doubleStream);
        topics.subscribe("?rest/");

        verify(stream, timed()).onSubscription(eq("rest/auto/timestamp"), specificationCaptor.capture());

        assertEquals(TopicType.JSON, specificationCaptor.getValue().getType());

        verify(stream, timed()).onValue(eq("rest/auto/timestamp"), isNotNull(), isNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(INFERRED_SERVICE);
    }

    @Test
    public void testJSONValue() throws IOException {
        modelStore.setModel(modelWith(CONSTANT_JSON_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_JSON_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_JSON_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("rest/json/constant");

        verify(stream, timed()).onSubscription(eq("rest/json/constant"), isNotNull());

        verify(stream, timed()).onValue(
            eq("rest/json/constant"),
            isNotNull(),
            isNull(),
            eq(JSON_CONSTANT));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(CONSTANT_JSON_SERVICE);
    }

    @Test
    public void testConstantValuesNotRemoved() throws IOException {
        modelStore.setModel(modelWith(CONSTANT_JSON_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_JSON_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_JSON_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("rest/json/constant");

        verify(stream, timed()).onSubscription(eq("rest/json/constant"), isNotNull());

        verify(stream, timed()).onValue(
            eq("rest/json/constant"),
            isNotNull(),
            isNull(),
            eq(JSON_CONSTANT));

        verify(stream, after(5000L).times(0)).onUnsubscription(eq("rest/json/constant"), isNotNull(), isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(CONSTANT_JSON_SERVICE);
    }

    @Test
    public void testBinaryValue() throws IOException {
        modelStore.setModel(modelWith(CONSTANT_BINARY_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_BINARY_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_BINARY_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(Binary.class, binaryStream);
        topics.subscribe("rest/binary/constant");

        verify(binaryStream, timed()).onSubscription(eq("rest/binary/constant"), isNotNull());

        verify(binaryStream, timed()).onValue(
            eq("rest/binary/constant"),
            isNotNull(),
            isNull(),
            eq(BINARY_CONSTANT));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(CONSTANT_BINARY_SERVICE);
    }

    @Test
    public void testStringValue() throws IOException {
        modelStore.setModel(modelWith(CONSTANT_STRING_SERVICE));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_STRING_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_STRING_SERVICE);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(String.class, stringStream);
        topics.subscribe("rest/string/constant");

        verify(stringStream, timed()).onSubscription(eq("rest/string/constant"), isNotNull());

        verify(stringStream, timed()).onValue(
            eq("rest/string/constant"),
            isNotNull(),
            isNull(),
            eq(STRING_CONSTANT));

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(CONSTANT_STRING_SERVICE);
    }

    @Test
    public void testOverlappingServices() throws IOException {
        modelStore.setModel(modelWith(OVERLAPPING_SERVICE_0, OVERLAPPING_SERVICE_1));
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(OVERLAPPING_SERVICE_0);
        verify(serviceListener, timed()).onStandby(OVERLAPPING_SERVICE_1);
        verify(serviceListener, timed()).onActive(OVERLAPPING_SERVICE_0);
        verify(serviceListener, timed()).onActive(OVERLAPPING_SERVICE_1);

        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(String.class, stringStream);
        topics.subscribe("?rest/overlap/");

        verify(stringStream, timed()).onSubscription(eq("rest/overlap/constant"), isNotNull());
        verify(stringStream, timed()).onSubscription(eq("rest/overlap/increment"), isNotNull());

        verify(stringStream, timed()).onValue(
            eq("rest/overlap/constant"),
            isNotNull(),
            isNull(),
            eq(STRING_CONSTANT));
        verify(stringStream, timed()).onValue(
            eq("rest/overlap/increment"),
            isNotNull(),
            isNull(),
            isNotNull());

        stopSession(session);
        client.close();

        verify(serviceListener, timed()).onRemove(OVERLAPPING_SERVICE_0);
        verify(serviceListener, timed()).onRemove(OVERLAPPING_SERVICE_1);
    }

    @Test
    public void prometheusHealthy() throws IOException {
        modelStore.setModel(Model
            .builder()
            .active(true)
            .diffusion(DIFFUSION_CONFIG)
            .services(asList(CONSTANT_STRING_SERVICE))
            .metrics(MetricsConfig.builder().prometheus(PrometheusConfig.builder().port(9000).build()).build())
            .truststore("testKeystore.jks")
            .build());
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_STRING_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_STRING_SERVICE);

        final HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:9000/-/healthy").openConnection();

        assertEquals(200, connection.getResponseCode());

        final byte[] bytes = connection.getInputStream().readAllBytes();
        final String response = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString();

        assertThat(response, containsString("Exporter is Healthy."));

        client.close();
        verify(serviceListener, timed()).onRemove(CONSTANT_STRING_SERVICE);
    }

    @Test
    public void prometheusMetrics() throws IOException {
        modelStore.setModel(Model
            .builder()
            .active(true)
            .diffusion(DIFFUSION_CONFIG)
            .services(asList(CONSTANT_STRING_SERVICE))
            .metrics(MetricsConfig.builder().prometheus(PrometheusConfig.builder().port(9001).build()).build())
            .truststore("testKeystore.jks")
            .build());
        final RESTAdapterClient client = startClient();

        verify(serviceListener, timed()).onStandby(CONSTANT_STRING_SERVICE);
        verify(serviceListener, timed()).onActive(CONSTANT_STRING_SERVICE);

        final HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:9001/metrics").openConnection();

        assertEquals(200, connection.getResponseCode());

        final byte[] bytes = connection.getInputStream().readAllBytes();
        final String response = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes)).toString();

        assertThat(response, containsString("# TYPE process_cpu_seconds_total counter"));
        assertThat(response, containsString("# TYPE process_open_fds gauge"));
        assertThat(response, containsString("# TYPE jvm_memory_bytes_used gauge"));
        assertThat(response, containsString("# TYPE jvm_threads_peak gauge"));

        assertThat(response, containsString("# TYPE poll_requests_total counter"));
        assertThat(response, containsString("# TYPE updates_published_total counter"));
        assertThat(response, containsString("# TYPE topics_created_total counter"));

        client.close();
        verify(serviceListener, timed()).onRemove(CONSTANT_STRING_SERVICE);
    }

    private static VerificationWithTimeout timed() {
        return timeout(5000);
    }

    private RESTAdapterClient startClient() {
        return startClient(serviceListener);
    }

    private RESTAdapterClient startClient(ServiceListener listener) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        final RESTAdapterClient client = RESTAdapterClient.create(
            Paths.get("."),
            modelStore,
            executor,
            executor::shutdown,
            listener,
            (session, oldState, newState) -> {});
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

    private static Model modelWith(ServiceConfig... services) {
        return Model
            .builder()
            .active(true)
            .diffusion(DIFFUSION_CONFIG)
            .services(asList(services))
            .truststore("testKeystore.jks")
            .build();
    }
}
