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

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
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

        if ("list-services".equals(type)) {
            responder.respond(modelStore.get().getServices());
            return;
        }

        if ("create-service".equals(type)) {
            onCreateService(request, responder);
            return;
        }

        if ("create-endpoint".equals(type)) {
            onCreateEndpoint(request, responder);
            return;
        }

        if ("delete-service".equals(type)) {
            onDeleteService(request, responder);
            return;
        }

        if ("delete-endpoint".equals(type)) {
            onDeleteEndpoint(request, responder);
            return;
        }

        LOG.error("Unknown type {}. Ignoring message.", type);
        responder.respond(error("Unknown request type"));
    }

    private void onDeleteEndpoint(Map<String, Object> request, RequestManager.Responder responder) {
        final Object serviceObject = request.get("serviceName");
        if (serviceObject == null || !(serviceObject instanceof String)) {
            LOG.error("No or invalid service name message component");
            responder.error("No service name provided");
            return;
        }

        final Object endpointObject = request.get("endpointName");
        if (endpointObject == null || !(endpointObject instanceof String)) {
            LOG.error("No or invalid endpoint name message component");
            responder.error("No endpoint name provided");
            return;
        }

        modelStore.apply(model -> {
            final List<ServiceConfig> serviceConfigs = model
                .getServices()
                .stream()
                .map(serviceConfig -> {
                    if (serviceConfig.getName().equals(serviceObject)) {
                        final List<EndpointConfig> endpointConfigs = serviceConfig
                            .getEndpoints()
                            .stream()
                            .filter(endpointConfig -> !endpointConfig.getName().equals(endpointObject))
                            .collect(toList());

                        return ServiceConfig
                            .builder()
                            .name(serviceConfig.getName())
                            .host(serviceConfig.getHost())
                            .port(serviceConfig.getPort())
                            .secure(serviceConfig.isSecure())
                            .endpoints(endpointConfigs)
                            .topicPathRoot(serviceConfig.getTopicPathRoot())
                            .pollPeriod(serviceConfig.getPollPeriod())
                            .security(serviceConfig.getSecurity())
                            .build();
                    }
                    else {
                        return serviceConfig;
                    }
                })
                .collect(toList());

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

    private void onDeleteService(Map<String, Object> request, RequestManager.Responder responder) {
        final Object serviceObject = request.get("serviceName");
        if (serviceObject == null || !(serviceObject instanceof String)) {
            LOG.error("No or invalid service name message component");
            responder.error("No service name provided");
            return;
        }

        modelStore.apply(model -> {
            final List<ServiceConfig> serviceConfigs = model
                .getServices()
                .stream()
                .filter(serviceConfig -> !serviceConfig.getName().equals(serviceObject))
                .collect(toList());

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

    @SuppressWarnings("unchecked")
    private void onCreateEndpoint(Map<String, Object> request, RequestManager.Responder responder) {
        final Object serviceObject = request.get("serviceName");
        if (serviceObject == null || !(serviceObject instanceof String)) {
            LOG.error("No or invalid service name message component");
            responder.error("No service name provided");
            return;
        }

        final Object endpointObject = request.get("endpoint");
        if (endpointObject == null || !(endpointObject instanceof Map)) {
            LOG.error("No or invalid endpoint message component");
            responder.error("No endpoint provided");
            return;
        }

        final Map<String, Object> endpoint = (Map<String, Object>) endpointObject;
        final EndpointConfig endpointConfig = EndpointConfig
            .builder()
            .name((String) endpoint.get("name"))
            .topicPath((String) endpoint.get("topicPath"))
            .url((String) endpoint.get("url"))
            .produces((String) endpoint.get("produces"))
            .build();

        switch (modelStore.createEndpoint((String) serviceObject, endpointConfig)) {
            case SUCCESS:
                responder.respond(emptyMap());
                return;
            case PARENT_MISSING:
                responder.error("service missing");
                return;
            case UNIQUE_VALUE_USED:
                responder.error("endpoint topic conflict");
                return;
            default:
                responder.error("endpoint name conflict");
        }

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
            responder.error("no service provided");
            return;
        }

        final Map<String, Object> service = (Map<String, Object>) serviceObject;
        final ServiceConfig.ServiceConfigBuilder serviceConfigBuilder = ServiceConfig
            .builder()
            .name((String) service.get("name"))
            .host((String) service.get("host"))
            .port((Integer) service.get("port"))
            .secure((Boolean) service.get("secure"))
            .endpoints(emptyList())
            .pollPeriod((Integer) service.get("pollPeriod"))
            .topicPathRoot((String) service.get("topicPathRoot"));

        final Object securityObject = service.get("security");
        if (securityObject != null && securityObject instanceof Map) {
            final Map<String, Object> security = (Map<String, Object>) securityObject;
            final Object basicObject = security.get("basic");
            if (basicObject != null && basicObject instanceof Map) {
                final Map<String, String> basic = (Map<String, String>) basicObject;
                serviceConfigBuilder
                    .security(SecurityConfig
                        .builder()
                        .basic(BasicAuthenticationConfig
                            .builder()
                            .userid(basic.get("userid"))
                            .password(basic.get("password"))
                            .build())
                        .build());
            }
        }

        switch (modelStore.createService(serviceConfigBuilder.build())) {
            case SUCCESS:
                responder.respond(emptyMap());
                return;
            case UNIQUE_VALUE_USED:
                responder.error("service root topic conflict");
                return;
            default:
                responder.error("service name conflict");
        }
    }
}
