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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Unit tests for {@link EndpointType}.
 *
 * @author Push Technology Ltd.
 */
public final class EndpointTypeTest {
    @Test
    public void json() {
        assertEquals(TopicType.JSON, EndpointType.JSON.getTopicType());
        assertTrue(EndpointType.JSON.canHandle("text/json"));
        assertTrue(EndpointType.JSON.canHandle("application/json"));
        assertFalse(EndpointType.JSON.canHandle("text/plain"));
        assertFalse(EndpointType.JSON.canHandle("application/octet-stream"));
    }

    @Test
    public void binary() {
        assertEquals(TopicType.BINARY, EndpointType.BINARY.getTopicType());
        assertTrue(EndpointType.BINARY.canHandle("text/json"));
        assertTrue(EndpointType.BINARY.canHandle("application/json"));
        assertTrue(EndpointType.BINARY.canHandle("text/plain"));
        assertTrue(EndpointType.BINARY.canHandle("text/plain; charset=utf-8"));
        assertTrue(EndpointType.BINARY.canHandle("application/octet-stream"));
    }

    @Test
    public void plainText() {
        assertEquals(TopicType.STRING, EndpointType.PLAIN_TEXT.getTopicType());
        assertTrue(EndpointType.PLAIN_TEXT.canHandle("text/json"));
        assertTrue(EndpointType.PLAIN_TEXT.canHandle("application/json"));
        assertTrue(EndpointType.PLAIN_TEXT.canHandle("text/plain"));
        assertTrue(EndpointType.PLAIN_TEXT.canHandle("text/plain; charset=utf-8"));
        assertFalse(EndpointType.PLAIN_TEXT.canHandle("application/octet-stream"));
    }

    @Test
    public void fromNames() {
        assertEquals(EndpointType.JSON, EndpointType.from("json"));
        assertEquals(EndpointType.PLAIN_TEXT, EndpointType.from("string"));
        assertEquals(EndpointType.BINARY, EndpointType.from("binary"));
    }

    @Test
    public void fromMediaTypes() {
        assertEquals(EndpointType.JSON, EndpointType.from("text/json"));
        assertEquals(EndpointType.JSON, EndpointType.from("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT, EndpointType.from("text/plain"));
        assertEquals(EndpointType.BINARY, EndpointType.from("application/octet-stream"));
    }

    @Test
    public void fromUnknown() {
        assertEquals(EndpointType.JSON, EndpointType.from("text/json"));
        assertEquals(EndpointType.JSON, EndpointType.from("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT, EndpointType.from("text/plain"));
        assertEquals(EndpointType.BINARY, EndpointType.from("application/octet-stream"));
    }

    @Test
    public void infer() {
        assertEquals(EndpointType.JSON, EndpointType.inferFromContentType("application/json"));
        assertEquals(EndpointType.PLAIN_TEXT, EndpointType.inferFromContentType("text/plain"));
        assertEquals(EndpointType.PLAIN_TEXT, EndpointType.inferFromContentType("text/plain; charset=utf-8"));
        assertEquals(EndpointType.BINARY, EndpointType.inferFromContentType("application/octet-stream"));
        assertEquals(EndpointType.BINARY, EndpointType.inferFromContentType("who/knows"));
    }
}
