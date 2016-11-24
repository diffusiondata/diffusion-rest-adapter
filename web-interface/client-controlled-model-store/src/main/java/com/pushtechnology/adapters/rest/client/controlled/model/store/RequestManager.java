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

import static com.pushtechnology.diffusion.client.Diffusion.content;
import static com.pushtechnology.diffusion.client.Diffusion.dataTypes;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.fromMap;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.toMapOf;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.transform.transformer.TransformationException;
import com.pushtechnology.diffusion.transform.transformer.Transformer;
import com.pushtechnology.diffusion.transform.transformer.Transformers;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

/**
 * Request manager to support responding to requests.
 * @author Push Technology Limited
 */
@ThreadSafe
public final class RequestManager {
    private static final Logger LOG = LoggerFactory.getLogger(RequestManager.class);
    private static final Transformer<Content, Map<String, Object>> DESERIALISER = Transformers
        .builder(Content.class)
        .transform(Content::toBytes)
        .transform(dataTypes().json()::readValue)
        .transform(toMapOf(Object.class))
        .build();
    private static final Transformer<Map<String, Object>, Content> SERIALISER = Transformers
        .<Map<String, Object>>builder()
        .transform(fromMap())
        .transform(Bytes::toByteArray)
        .transform(content()::newContent)
        .build();
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
        messagingControl.addMessageHandler(path, new Handler(path, handler));
    }

    /**
     * Responder for requests.
     */
    public interface Responder {
        /**
         * Respond to a request with an error.
         */
        void error(String message);

        /**
         * Respond to a request with a result.
         */
        void respond(Object response);
    }

    /**
     * Handler for requests.
     */
    public interface RequestHandler {
        /**
         * Called when a request is received.
         */
        void onRequest(Map<String, Object> request, Responder responder);
    }

    @Immutable
    private final class Handler extends MessagingControl.MessageHandler.Default {
        private final String path;
        private final RequestHandler handler;

        /*package*/ Handler(String path, RequestHandler handler) {
            this.path = path;
            this.handler = handler;
        }

        @Override
        public void onMessage(SessionId sessionId, String requestPath, Content content, ReceiveContext receiveContext) {
            if (!path.equals(requestPath)) {
                LOG.error("Received a message on the wrong path");
                return;
            }

            final Map<String, Object> request;
            try {
                request = DESERIALISER.transform(content);
            }
            catch (TransformationException e) {
                LOG.error("Did not receive a valid JSON value: {}", content);
                return;
            }

            final Object id = request.get("id");
            if (id == null) {
                LOG.error("Request did not include an ID: {}", request);
                return;
            }

            handler.onRequest(request, new Responder() {
                @Override
                public void error(String message) {
                    final Map<String, Object> responseObject = new HashMap<>();
                    responseObject.put("error", message);
                    responseObject.put("id", id);

                    sendResponse(responseObject);
                }

                @Override
                public void respond(Object response) {
                    final Map<String, Object> responseObject = new HashMap<>();
                    responseObject.put("response", response);
                    responseObject.put("id", id);

                    sendResponse(responseObject);
                }

                private void sendResponse(Map<String, Object> responseObject) {
                    LOG.info("Responding to request id {}", id);

                    try {
                        messagingControl.send(
                            sessionId,
                            path,
                            SERIALISER.transform(responseObject),
                            new MessagingControl.SendCallback.Default());
                    }
                    catch (TransformationException e) {
                        throw new IllegalStateException("Failed to create response", e);
                    }
                }
            });
        }
    }
}
