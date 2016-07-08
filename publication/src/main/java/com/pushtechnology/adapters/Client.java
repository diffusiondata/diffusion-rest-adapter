
package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.v3.Service;
import com.pushtechnology.adapters.rest.model.v3.Endpoint;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.json.JSON;

public final class Client {
    /**
     * The Diffusion UCI client uses SLF4J for logging. Only the API is included, you need to add your own API.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
    private final String host;
    private final int port;
    private Session session;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Start the client running. Connects the client to Diffusion.
     */
    public synchronized void start() {
        // Use the session factory to open a new session
        session = Diffusion
            .sessions()
            .listener(new Listener())
            .serverHost(host)
            .serverPort(port)
            .secureTransport(false)
            .transports(WEBSOCKET)
            .open();
    }

    public synchronized void initialise(Service service) {
        final TopicControl topicControl = session.feature(TopicControl.class);

        service.getEndpoints().stream().forEach(endpoint -> {
            topicControl.addTopic(endpoint.getTopic(), TopicType.JSON, new TopicControl.AddCallback.Default());
        });
    }

    /**
     * Stop the client running.
     */
    public synchronized void stop() {
        session.close();
    }

    /**
     * Update the topic associated with an endpoint.
     */
    public synchronized void publish(Endpoint endpoint, JSON json) {
        if (!session.getState().isConnected()) {
            return;
        }

        session
            .feature(TopicUpdateControl.class)
            .updater()
            .valueUpdater(JSON.class)
            .update(endpoint.getTopic(), json, new UpdateCallback.Default());
    }

    /**
     * A simple session state listener that logs out state changes.
     */
    private static final class Listener implements Session.Listener {

        @Override
        public void onSessionStateChanged(Session session, Session.State oldState, Session.State newState) {
            LOG.info("{} {} -> {}", session, oldState, newState);
        }
    }
}
