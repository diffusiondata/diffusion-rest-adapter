/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

import static com.pushtechnology.diffusion.transform.transformer.Transformers.fromMap;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.toMapOf;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl.RequestHandler.Responder;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.Transformers;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Request manager to support responding to requests.
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RequestManager {
    private static final Logger LOG = LoggerFactory.getLogger(RequestManager.class);
    private static final UnsafeTransformer<JSON, Map<String, Object>> DESERIALISER = Transformers
        .builder(JSON.class)
        .unsafeTransform(toMapOf(Object.class))
        .buildUnsafe();
    private static final UnsafeTransformer<Map<String, Object>, JSON> SERIALISER = Transformers
        .<Map<String, Object>>builder()
        .unsafeTransform(fromMap())
        .buildUnsafe();
    private final MessagingControl messagingControl;

    /**
     * Constructor.
     */
    public RequestManager(MessagingControl messagingControl) {
        this.messagingControl = messagingControl;
    }

    /**
     * Add a handler.
     */
    public void addHandler(String path, RequestHandler handler) {
        messagingControl.addRequestHandler(path, JSON.class, JSON.class, new Handler(path, handler));
    }

    /**
     * Handler for requests.
     */
    public interface RequestHandler {
        /**
         * Called when a request is received.
         */
        void onRequest(Map<String, Object> request, Responder<Map<String, Object>> responder);
    }

    @Immutable
    private static final class Handler implements MessagingControl.RequestHandler<JSON, JSON> {
        private final String path;
        private final RequestHandler handler;

        /*package*/ Handler(String path, RequestHandler handler) {
            this.path = path;
            this.handler = handler;
        }

        @Override
        public void onRequest(
            JSON json,
            RequestContext requestContext,
            Responder responder) {

            final String requestPath = requestContext.getPath();

            if (!path.equals(requestPath)) {
                LOG.error("Received a message on the wrong path");
                return;
            }

            final Map<String, Object> request;
            try {
                request = DESERIALISER.transform(json);
            }
            // CHECKSTYLE.OFF: IllegalCatch
            catch (Exception e) {
                LOG.error("Did not receive a valid JSON value: {}", json);
                return;
            }
            // CHECKSTYLE.ON: IllegalCatch

            handler.onRequest(request, new Responder<Map<String, Object>>() {

                @Override
                public void respond(Map<String, Object> response) {
                    try {
                        responder.respond(SERIALISER.transform(response));
                    }
                    // CHECKSTYLE.OFF: IllegalCatch
                    catch (Exception e) {
                        throw new IllegalStateException("Failed to create response", e);
                    }
                    // CHECKSTYLE.ON: IllegalCatch
                }

                @Override
                public void reject(String message) {
                    responder.reject(message);
                }
            });
        }

        @Override
        public void onClose() {
        }

        @Override
        public void onError(ErrorReason errorReason) {
            LOG.error("Error with request handler {}", errorReason);
        }
    }
}
