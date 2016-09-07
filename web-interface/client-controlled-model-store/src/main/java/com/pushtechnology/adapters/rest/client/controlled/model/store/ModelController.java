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
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.model.store.AsyncMutableModelStore;

import net.jcip.annotations.ThreadSafe;

/**
 * A {@link RequestManager.RequestHandler} for modifying the model store.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
/*package*/ final class ModelController implements RequestManager.RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ModelController.class);
    private final AsyncMutableModelStore modelStore;

    /**
     * Constructor.
     */
    /*package*/ ModelController(AsyncMutableModelStore modelStore) {
        this.modelStore = modelStore;
    }

    @Override
    public void onRequest(Map<String, Object> request, RequestManager.Responder responder) {
        final Object type = request.get("type");
        if ("create-service".equals(type)) {
            onCreateService(request, responder);
            return;
        }

        if ("list-services".equals(type)) {
            responder.respond(modelStore.get().getServices());
            return;
        }

        LOG.error("Unknown type. Ignoring message.");
        responder.respond(error("Unknown request type"));
    }

    private static Map<String, Object> error(String message) {
        final Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    @SuppressWarnings("unchecked")
    private void onCreateService(Map<String, Object> request, RequestManager.Responder responder) {
        final Object serviceObject = request.get("service");

        if (serviceObject == null || !(serviceObject instanceof Map)) {
            LOG.error("No or invalid service message component");
            responder.respond(error("No service provided"));
            return;
        }

        final Map<String, Object> service = (Map<String, Object>) serviceObject;
        final ServiceConfig serviceConfig = ServiceConfig
            .builder()
            .name((String) service.get("name"))
            .host((String) service.get("host"))
            .port((Integer) service.get("port"))
            .secure((Boolean) service.get("secure"))
            .endpoints(emptyList())
            .pollPeriod((Integer) service.get("pollPeriod"))
            .topicRoot((String) service.get("topicRoot"))
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
        responder.respond(emptyMap());
    }
}
