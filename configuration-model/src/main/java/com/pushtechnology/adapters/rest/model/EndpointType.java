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

package com.pushtechnology.adapters.rest.model;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * The supported endpoint types that can be processed by the adapter.
 *
 * @author Push Technology Limited
 */
public enum EndpointType {
    JSON(asList("json", "application/json", "text/json"), TopicType.JSON, com.pushtechnology.diffusion.datatype.json.JSON.class) {
        @Override
        public boolean canHandle(String contentType) {
            return contentType != null &&
                (contentType.startsWith("application/json") || contentType.startsWith("text/json"));
        }
    },
    PLAIN_TEXT(asList("string", "text/plain"), TopicType.BINARY, String.class) {
        @Override
        public boolean canHandle(String contentType) {
            return contentType != null && (contentType.startsWith("text/plain") || JSON.canHandle(contentType));
        }
    },
    BINARY(asList("binary", "application/octet-stream"), TopicType.BINARY, com.pushtechnology.diffusion.datatype.binary.Binary.class) {
        @Override
        public boolean canHandle(String contentType) {
            return true;
        }
    };

    private static final Map<String, EndpointType> identifierLookup = new HashMap<>();

    static {
        for (EndpointType type : EndpointType.values()) {
            type.identifiers
                .stream()
                .map(identifier -> identifierLookup.putIfAbsent(identifier, type))
                .filter(current -> current != null)
                .forEach(result -> {
                    throw new IllegalStateException(
                        "The EndpointType " + type + " has already been registered for an identifier");
                });
        }
    }

    private final Collection<String> identifiers;
    private final TopicType topicType;
    private final Class<?> valueType;

    EndpointType(Collection<String> identifiers, TopicType topicType, Class<?> valueType) {
        this.identifiers = identifiers;
        this.topicType = topicType;
        this.valueType = valueType;
    }

    /**
     * @return the topic type of the endpoint type
     */
    public TopicType getTopicType() {
        return topicType;
    }

    /**
     * @return the value type of the endpoint type
     */
    public Class<?> getValueType() {
        return valueType;
    }

    /**
     * @return an identifier for the endpoint type
     */
    public String getIdentifier() {
        return identifiers.iterator().next();
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
        final EndpointType endpointType = identifierLookup.get(identifier);
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
