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

package com.pushtechnology.adapters.rest.endpoints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link EndpointType}.
 *
 * @author Push Technology Ltd.
 */
public final class EndpointTypeTest {
    @Test
    public void json() {
        assertEquals(TopicType.JSON, EndpointType.JSON_ENDPOINT_TYPE.getTopicType());
        assertEquals(JSON.class, EndpointType.JSON_ENDPOINT_TYPE.getValueType());
        assertTrue(EndpointType.JSON_ENDPOINT_TYPE.canHandle("text/json"));
        assertTrue(EndpointType.JSON_ENDPOINT_TYPE.canHandle("application/json"));
        assertFalse(EndpointType.JSON_ENDPOINT_TYPE.canHandle("text/plain"));
        assertFalse(EndpointType.JSON_ENDPOINT_TYPE.canHandle("application/octet-stream"));
    }

    @Test
    public void binary() {
        assertEquals(TopicType.BINARY, EndpointType.BINARY_ENDPOINT_TYPE.getTopicType());
        assertEquals(Binary.class, EndpointType.BINARY_ENDPOINT_TYPE.getValueType());
        assertTrue(EndpointType.BINARY_ENDPOINT_TYPE.canHandle("text/json"));
        assertTrue(EndpointType.BINARY_ENDPOINT_TYPE.canHandle("application/json"));
        assertTrue(EndpointType.BINARY_ENDPOINT_TYPE.canHandle("text/plain"));
        assertTrue(EndpointType.BINARY_ENDPOINT_TYPE.canHandle("text/plain; charset=utf-8"));
        assertTrue(EndpointType.BINARY_ENDPOINT_TYPE.canHandle("application/octet-stream"));
    }

    @Test
    public void plainText() {
        assertEquals(TopicType.STRING, EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.getTopicType());
        assertEquals(String.class, EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.getValueType());
        assertTrue(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.canHandle("text/json"));
        assertTrue(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.canHandle("application/json"));
        assertTrue(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.canHandle("text/plain"));
        assertTrue(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.canHandle("text/plain; charset=utf-8"));
        assertFalse(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE.canHandle("application/octet-stream"));
    }

    @Test
    public void fromNames() {
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.from("json"));
        assertEquals(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE, EndpointType.from("string"));
        assertEquals(EndpointType.BINARY_ENDPOINT_TYPE, EndpointType.from("binary"));
    }

    @Test
    public void fromMediaTypes() {
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.from("text/json"));
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.from("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE, EndpointType.from("text/plain"));
        assertEquals(EndpointType.BINARY_ENDPOINT_TYPE, EndpointType.from("application/octet-stream"));
    }

    @Test
    public void fromUnknown() {
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.from("text/json"));
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.from("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE, EndpointType.from("text/plain"));
        assertEquals(EndpointType.BINARY_ENDPOINT_TYPE, EndpointType.from("application/octet-stream"));
    }

    @Test
    public void infer() {
        assertEquals(EndpointType.JSON_ENDPOINT_TYPE, EndpointType.inferFromContentType("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE, EndpointType.inferFromContentType("text/plain"));
        assertEquals(EndpointType.PLAIN_TEXT_ENDPOINT_TYPE, EndpointType.inferFromContentType("text/plain; charset=utf-8"));
        assertEquals(EndpointType.BINARY_ENDPOINT_TYPE, EndpointType.inferFromContentType("application/octet-stream"));
        assertEquals(EndpointType.BINARY_ENDPOINT_TYPE, EndpointType.inferFromContentType("who/knows"));
    }
}
