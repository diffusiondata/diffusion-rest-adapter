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

package com.pushtechnology.adapters.rest.endpoints;

import static com.pushtechnology.diffusion.transform.transformer.Transformers.byteArrayToBinary;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.chain;
import static com.pushtechnology.diffusion.transform.transformer.Transformers.toSuperClass;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.Bytes;
import com.pushtechnology.diffusion.transform.transformer.Transformer;

/**
 * The supported endpoint types that can be processed by the adapter.
 *
 * @author Push Technology Limited
 */
public enum EndpointType {
    /**
     * Endpoint type for JSON endpoints.
     */
    JSON(
        asList("json", "application/json", "text/json"),
        TopicType.JSON,
        chain(
            chain(
                EndpointResponseToStringTransformer.INSTANCE,
                StringToJSONTransformer.INSTANCE),
            toSuperClass())) {

        @Override
        public boolean canHandle(String contentType) {
            return contentType != null &&
                (contentType.startsWith("application/json") || contentType.startsWith("text/json"));
        }
    },
    /**
     * Endpoint type for text endpoints.
     */
    PLAIN_TEXT(
        asList("string", "text/plain"),
        TopicType.BINARY,
        chain(
            chain(
                EndpointResponseToStringTransformer.INSTANCE,
                StringToBinaryTransformer.INSTANCE),
            toSuperClass())) {

        @Override
        public boolean canHandle(String contentType) {
            return contentType != null && (contentType.startsWith("text/plain") || JSON.canHandle(contentType));
        }
    },
    /**
     * Endpoint type for Binary endpoints.
     */
    BINARY(
        asList("binary", "application/octet-stream"),
        TopicType.BINARY,
        chain(
            chain(
                EndpointResponseToBytesTransformer.INSTANCE,
                byteArrayToBinary()),
            toSuperClass())) {

        @Override
        public boolean canHandle(String contentType) {
            return true;
        }
    };

    private static final Map<String, EndpointType> IDENTIFIER_LOOKUP = new HashMap<>();

    static {
        for (EndpointType type : EndpointType.values()) {
            type.identifiers
                .stream()
                .map(identifier -> IDENTIFIER_LOOKUP.putIfAbsent(identifier, type))
                .filter(current -> current != null)
                .forEach(result -> {
                    throw new IllegalStateException(
                        "The EndpointType " + type + " has already been registered for an identifier");
                });
        }
    }

    private final Collection<String> identifiers;
    private final TopicType topicType;
    private final Transformer<EndpointResponse, Bytes> parser;

    /**
     * Constructor.
     */
    EndpointType(
            Collection<String> identifiers,
            TopicType topicType,
            Transformer<EndpointResponse, Bytes> parser) {

        this.identifiers = identifiers;
        this.topicType = topicType;
        this.parser = parser;
    }

    /**
     * @return the topic type of the endpoint type
     */
    public TopicType getTopicType() {
        return topicType;
    }

    /**
     * @return an identifier for the endpoint type
     */
    public String getIdentifier() {
        return identifiers.iterator().next();
    }

    /**
     * @return parser for endpoints
     */
    public Transformer<EndpointResponse, Bytes> getParser() {
        return parser;
    }

    /**
     * @return if the content type if valid for the endpoint type
     */
    public abstract boolean canHandle(String contentType);

    /**
     * @return the endpoint type resolved from the name or a supported media type
     * @throws IllegalArgumentException if the endpoint type is unknown
     */
    public static EndpointType from(String identifier) {
        final EndpointType endpointType = IDENTIFIER_LOOKUP.get(identifier);
        if (endpointType == null) {
            throw new IllegalArgumentException("Unknown endpoint type " + identifier);
        }
        return endpointType;
    }

    /**
     * @return best guess for the topic type for the provided content type
     */
    public static EndpointType inferFromContentType(String contentType) {
        if (JSON.canHandle(contentType)) {
            return JSON;
        }
        else if (PLAIN_TEXT.canHandle(contentType)) {
            return PLAIN_TEXT;
        }
        else {
            return BINARY;
        }
    }
}
