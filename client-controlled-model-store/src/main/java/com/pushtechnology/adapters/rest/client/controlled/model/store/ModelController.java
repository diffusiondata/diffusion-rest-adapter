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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;
import com.pushtechnology.diffusion.client.content.Content;
import com.pushtechnology.diffusion.client.features.control.topics.MessagingControl;
import com.pushtechnology.diffusion.client.session.SessionId;
import com.pushtechnology.diffusion.client.types.ReceiveContext;

import net.jcip.annotations.ThreadSafe;

/**
 * A {@link MessagingControl.MessageHandler} for modifying the model store.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
/*package*/ final class ModelController extends MessagingControl.MessageHandler.Default {
    private static final Logger LOG = LoggerFactory.getLogger(ModelController.class);
    private final CBORFactory factory = new CBORFactory();
    private final ObjectMapper mapper = new ObjectMapper(factory);
    private final AsyncMutableModelStore modelStore;
    private final ModelPublisher modelPublisher;

    /**
     * Constructor.
     */
    /*package*/ ModelController(AsyncMutableModelStore modelStore, ModelPublisher modelPublisher) {
        this.modelStore = modelStore;
        this.modelPublisher = modelPublisher;
    }

    @Override
    public void onMessage(SessionId sessionId, String path, Content content, ReceiveContext receiveContext) {
        if (!ClientControlledModelStore.CONTROL_PATH.equals(path)) {
            LOG.error("Received a message on the wrong path");
            return;
        }

        final JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(content.toBytes());
        }
        catch (IOException e) {
            LOG.error("Did not receive a valid JSON value: {}", content);
            return;
        }

        onJsonMessage(jsonNode);
    }

    private void onJsonMessage(JsonNode jsonNode) {
        final JsonNode typeNode = jsonNode.get("type");
        if (typeNode == NullNode.instance || typeNode == null) {
            LOG.error("Unknown type. Ignoring message.");
            return;
        }

        final String type = typeNode.asText();
        if ("create-service".equals(type)) {
            onCreateService(jsonNode);
            return;
        }

        LOG.error("Unknown type. Ignoring message.");
    }

    private void onCreateService(JsonNode jsonNode) {
        final JsonNode serviceNode = jsonNode.get("service");
        final ServiceConfig serviceConfig = ServiceConfig
            .builder()
            .name(serviceNode.get("name").asText())
            .host(serviceNode.get("host").asText())
            .port(serviceNode.get("port").asInt())
            .secure(serviceNode.get("secure").asBoolean())
            .endpoints(emptyList())
            .pollPeriod(serviceNode.get("pollPeriod").asInt())
            .topicRoot(serviceNode.get("topicRoot").asText())
            .build();

        modelStore.apply(model -> {
            final List<ServiceConfig> serviceConfigs = model.getServices().stream().collect(toList());
            serviceConfigs.add(serviceConfig);

            return Model
                .builder()
                .active(true)
                .diffusion(model.getDiffusion())
                .services(serviceConfigs)
                .truststore(model.getTruststore())
                .build();
        });
        modelPublisher.update();
    }
}
