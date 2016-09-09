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

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
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

                        if (!endpointConfigs.equals(serviceConfig.getEndpoints())) {
                            LOG.info("Removing endpoint {} from {}", endpointObject, serviceObject);
                        }

                        return ServiceConfig
                            .builder()
                            .name(serviceConfig.getName())
                            .host(serviceConfig.getHost())
                            .port(serviceConfig.getPort())
                            .secure(serviceConfig.isSecure())
                            .endpoints(endpointConfigs)
                            .topicRoot(serviceConfig.getTopicRoot())
                            .pollPeriod(serviceConfig.getPollPeriod())
                            .security(serviceConfig.getSecurity())
                            .build();
                    }
                    else {
                        return serviceConfig;
                    }
                })
                .collect(toList());

            if (serviceConfigs.equals(model.getServices())) {
                LOG.info("Failed to find endpoint {} on {}", endpointObject, serviceObject);
            }

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

            if (serviceConfigs.equals(model.getServices())) {
                LOG.info("Failed to find service {}", serviceObject);
            }
            else {
                LOG.info("Removing service {}", serviceObject);
            }

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
            .topic((String) endpoint.get("topic"))
            .url((String) endpoint.get("url"))
            .produces((String) endpoint.get("produces"))
            .build();

        modelStore.apply(model -> {
            final List<ServiceConfig> serviceConfigs = model
                .getServices()
                .stream()
                .map(serviceConfig -> {
                    if (serviceConfig.getName().equals(serviceObject)) {
                        final List<EndpointConfig> endpointConfigs = serviceConfig
                            .getEndpoints()
                            .stream()
                            .collect(toList());
                        endpointConfigs.add(endpointConfig);

                        LOG.info("Adding {} to {}", endpointConfig, serviceObject);

                        return ServiceConfig
                            .builder()
                            .name(serviceConfig.getName())
                            .host(serviceConfig.getHost())
                            .port(serviceConfig.getPort())
                            .secure(serviceConfig.isSecure())
                            .endpoints(endpointConfigs)
                            .topicRoot(serviceConfig.getTopicRoot())
                            .pollPeriod(serviceConfig.getPollPeriod())
                            .security(serviceConfig.getSecurity())
                            .build();
                    }
                    else {
                        return serviceConfig;
                    }
                })
                .collect(toList());

            if (serviceConfigs.equals(model.getServices())) {
                LOG.info("Failed to find service {}", serviceObject);
            }

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

            LOG.info("Adding {}", serviceConfig);

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
