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

import static com.pushtechnology.adapters.rest.client.controlled.model.store.ClientControlledModelStore.CONTROL_PATH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.callbacks.TopicTreeHandler;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl.AddCallback.Default;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.datatype.json.JSONDataType;

/**
 * Implementation of {@link ModelPublisher}.
 *
 * @author Push Technology Limited
 */
/*package*/ final class ModelPublisherImpl implements ModelPublisher {
    private static final CBORFactory CBOR_FACTORY = new CBORFactory();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(CBOR_FACTORY);
    private static final JSONDataType JSON_DATA_TYPE = Diffusion.dataTypes().json();
    private final TopicControl topicControl;

    private ModelPublisherImpl(TopicControl topicControl) {
        this.topicControl = topicControl;
    }

    @Override
    public void initialise(Model model) {
        model.getServices().forEach(serviceConfig ->
            topicControl.addTopic(
                toTopicPath(serviceConfig),
                TopicType.JSON,
                fromPojo(serviceConfig),
                new Default())
        );
    }

    @Override
    public void update() {
    }

    private String toTopicPath(ServiceConfig serviceConfig) {
        return CONTROL_PATH + "/services/" + serviceConfig.getName();
    }

    /**
     * @return a new model publisher
     */
    public static ModelPublisher create(Session session) {
        final TopicControl topicControl = session.feature(TopicControl.class);

        topicControl.removeTopicsWithSession(CONTROL_PATH, new TopicTreeHandler.Default());

        return new ModelPublisherImpl(topicControl);
    }

    private static <T> JSON fromPojo(T pojo) {
        final byte[] pojoBytes;
        try {
            pojoBytes = OBJECT_MAPPER.writeValueAsBytes(pojo);
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        return JSON_DATA_TYPE.readValue(pojoBytes);
    }
}
