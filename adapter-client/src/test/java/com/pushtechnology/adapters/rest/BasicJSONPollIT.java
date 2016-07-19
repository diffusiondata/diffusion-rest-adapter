package com.pushtechnology.adapters.rest;

import static com.pushtechnology.diffusion.client.session.Session.State.CLOSED_BY_CLIENT;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTED_ACTIVE;
import static com.pushtechnology.diffusion.client.session.Session.State.CONNECTING;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

import com.pushtechnology.adapters.rest.client.RESTAdapterClient;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.FixedModelStore;
import com.pushtechnology.adapters.rest.resources.IncrementingResource;
import com.pushtechnology.adapters.rest.resources.TimestampResource;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.Topics;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Basic integration test for adapting JSON resources.
 *
 * @author Push Technology Limited
 */
public final class BasicJSONPollIT {
    private static final Model MODEL = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("localhost")
            .port(8080)
            .principal("control")
            .password("password")
            .build())
        .services(singletonList(
            ServiceConfig
                .builder()
                .host("localhost")
                .port(8081)
                .pollPeriod(500)
                .topicRoot("rest")
                .endpoints(asList(
                    EndpointConfig
                        .builder()
                        .name("increment")
                        .topic("increment")
                        .url("/rest/increment")
                        .build(),
                    EndpointConfig
                        .builder()
                        .name("timestamp")
                        .topic("timestamp")
                        .url("/rest/timestamp")
                        .build()
                ))
                .build()))
        .build();

    private static Server jettyServer;

    @Mock
    private Session.Listener listener;
    @Mock
    private Topics.ValueStream<JSON> stream;
    @Mock
    private Topics.CompletionCallback callback;

    @BeforeClass
    public static void startApplicationServer() throws Exception {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/rest");
        final ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter(
            "jersey.config.server.provider.classnames",
            TimestampResource.class.getCanonicalName() + "," + IncrementingResource.class.getCanonicalName());
        jettyServer = new Server(8081);
        jettyServer.setHandler(context);
        jettyServer.start();

    }

    @AfterClass
    public static void stopApplicationServer() throws Exception {
        jettyServer.stop();
    }

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void test() throws IOException {
        final RESTAdapterClient client = startClient();
        final Session session = startSession();

        final Topics topics = session.feature(Topics.class);
        topics.addFallbackStream(JSON.class, stream);
        topics.subscribe("?rest/", callback);

        verify(callback, timed()).onComplete();
        verify(stream, timed()).onSubscription(eq("rest/increment"), isA(TopicSpecification.class));

        stopSession(session);
        client.close();
    }

    private static VerificationWithTimeout timed() {
        return timeout(5000);
    }

    private static RESTAdapterClient startClient() {
        final RESTAdapterClient client = RESTAdapterClient.create(new FixedModelStore(MODEL));
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
