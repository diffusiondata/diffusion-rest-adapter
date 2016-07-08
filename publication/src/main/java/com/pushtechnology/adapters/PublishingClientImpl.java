
package com.pushtechnology.adapters;

import static com.pushtechnology.diffusion.client.session.SessionAttributes.Transport.WEBSOCKET;
import static com.pushtechnology.diffusion.client.topics.details.TopicType.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicUpdateControl.Updater.UpdateCallback;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Implements {@link PublishingClient}.
 *
 * @author Push Technology Limited
 */
public final class PublishingClientImpl implements PublishingClient {
    private static final Logger LOG = LoggerFactory.getLogger(PublishingClientImpl.class);
    private final String host;
    private final int port;
    private Session session;

    public PublishingClientImpl(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
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

    @Override
    public synchronized void initialise(Service service) {
        final TopicControl topicControl = session.feature(TopicControl.class);

        service
            .getEndpoints()
            .forEach(endpoint -> topicControl.addTopic(endpoint.getTopic(), JSON, new AddCallback.Default()));
    }

    @Override
    public synchronized void stop() {
        session.close();
    }

    @Override
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
