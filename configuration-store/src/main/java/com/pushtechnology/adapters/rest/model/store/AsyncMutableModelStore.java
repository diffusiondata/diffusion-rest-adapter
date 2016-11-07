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

package com.pushtechnology.adapters.rest.model.store;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * An mutable model store that notifies listeners asynchronously.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class AsyncMutableModelStore implements ModelStore {
    private static final Logger LOG = LoggerFactory.getLogger(AsyncMutableModelStore.class);

    private final Collection<Consumer<Model>> listeners = new CopyOnWriteArrayList<>();
    private final Object modelMutex = new Object();
    private final Object notificationMutex = new Object();
    private final Executor executor;
    @GuardedBy("modelMutex")
    private Model model;
    @GuardedBy("notificationMutex")
    private int notificationVersion;

    /**
     * Constructor.
     * @param executor the executor to notify on
     */
    public AsyncMutableModelStore(Executor executor) {
        this.executor = executor;
    }

    @Override
    public synchronized Model get() {
        synchronized (modelMutex) {
            return model;
        }
    }

    /**
     * Atomically update the model used by the store.
     * @param operation the operation to apply to the current model
     * @return the store
     */
    public AsyncMutableModelStore apply(Function<Model, Model> operation) {
        synchronized (modelMutex) {
            final Model newModel = operation.apply(model);

            updateModel(newModel);
        }
        return this;
    }

    @GuardedBy("modelMutex")
    private void updateModel(Model newModel) {
        if (!newModel.equals(model)) {
            model = newModel;

            synchronized (notificationMutex) {
                notificationVersion += 1;
                notifyListeners(notificationVersion, newModel);
            }
        }
    }

    /**
     * Atomically add a service to the model.
     * @param serviceConfig the service to add
     * @return the result
     */
    public CreateResult createService(ServiceConfig serviceConfig) {
        synchronized (modelMutex) {
            final Optional<ServiceConfig> currentService = model
                .getServices()
                .stream()
                .filter(service -> service.getName().equals(serviceConfig.getName()))
                .findFirst();

            if (currentService.isPresent()) {
                if (serviceConfig.equals(currentService.get())) {
                    LOG.info("Service {} present", serviceConfig);

                    return CreateResult.SUCCESS;
                }

                LOG.info("Service {} name used", serviceConfig);

                return CreateResult.NAME_CONFLICT;
            }

            final boolean isTopicRootInUse = model
                .getServices()
                .stream()
                .filter(service -> service.getTopicPathRoot().equals(serviceConfig.getTopicPathRoot()))
                .findFirst()
                .isPresent();

            if (isTopicRootInUse) {
                LOG.info("Service {} topic root used", serviceConfig);

                return CreateResult.UNIQUE_VALUE_USED;
            }

            final List<ServiceConfig> serviceConfigs = model.getServices().stream().collect(toList());
            serviceConfigs.add(serviceConfig);

            LOG.info("Service {} created", serviceConfig);

            updateModel(Model
                .builder()
                .active(model.isActive())
                .diffusion(model.getDiffusion())
                .services(serviceConfigs)
                .truststore(model.getTruststore())
                .build());

            return CreateResult.SUCCESS;
        }
    }

    /**
     * Atomically add an endpoint to the model.
     * @param serviceName the name of the service to add the endpoint to
     * @param endpointConfig the endpoint to add
     * @return the result
     */
    public CreateResult createEndpoint(String serviceName, EndpointConfig endpointConfig) {
        synchronized (modelMutex) {
            final Optional<ServiceConfig> currentService = model
                .getServices()
                .stream()
                .filter(service -> service.getName().equals(serviceName))
                .findFirst();

            if (!currentService.isPresent()) {
                LOG.info("Service {} not found", serviceName);
                return CreateResult.PARENT_MISSING;
            }
            final ServiceConfig serviceConfig = currentService.get();

            final Optional<EndpointConfig> currentEndpoint = serviceConfig
                .getEndpoints()
                .stream()
                .filter(endpoint -> endpoint.getName().equals(endpointConfig.getName()))
                .findFirst();

            if (currentEndpoint.isPresent()) {
                if (currentEndpoint.get().equals(endpointConfig)) {
                    LOG.info("Endpoint {} present under {}", endpointConfig, serviceName);

                    return CreateResult.SUCCESS;
                }

                LOG.info("Endpoint {} name used under {}", endpointConfig, serviceName);

                return CreateResult.NAME_CONFLICT;
            }

            final boolean isTopicInUse = serviceConfig
                .getEndpoints()
                .stream()
                .filter(endpoint -> endpoint.getTopicPath().equals(endpointConfig.getTopicPath()))
                .findFirst()
                .isPresent();

            if (isTopicInUse) {
                LOG.info("Service {} topic used", serviceConfig);

                return CreateResult.UNIQUE_VALUE_USED;
            }

            final List<EndpointConfig> endpointConfigs = serviceConfig
                .getEndpoints()
                .stream()
                .collect(toList());
            endpointConfigs.add(endpointConfig);

            LOG.info("Endpoint {} created under {}", endpointConfig, serviceName);

            final ServiceConfig updatedService = ServiceConfig
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

            final List<ServiceConfig> serviceConfigs = model
                .getServices()
                .stream()
                .map(service -> {
                    if (serviceConfig.getName().equals(serviceName)) {
                        return updatedService;
                    }
                    else {
                        return service;
                    }
                })
                .collect(toList());

            updateModel(Model
                .builder()
                .active(model.isActive())
                .diffusion(model.getDiffusion())
                .services(serviceConfigs)
                .truststore(model.getTruststore())
                .build());

            return CreateResult.SUCCESS;
        }
    }

    /**
     * Set the model used by the store.
     * @param newModel the new model
     * @return the store
     */
    public AsyncMutableModelStore setModel(Model newModel) {
        return apply(model -> newModel);
    }

    @Override
    public void onModelChange(Consumer<Model> listener) {
        listeners.add(listener);

        synchronized (modelMutex) {
            if (model == null) {
                return;
            }

            synchronized (notificationMutex) {
                listener.accept(model);
            }
        }
    }

    /**
     * Notify the listeners of a model change.
     */
    private void notifyListeners(int newNotificationVersion, Model newModel) {
        executor.execute(() -> {
            synchronized (notificationMutex) {
                if (notificationVersion != newNotificationVersion) {
                    return;
                }

                listeners.forEach(listeners1 -> listeners1.accept(newModel));
            }
        });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * The result of attempting to create an item in the model.
     */
    public enum CreateResult {
        /**
         * A different item is present with the same name.
         */
        NAME_CONFLICT,
        /**
         * A different item is present with the same unique value.
         */
        UNIQUE_VALUE_USED,
        /**
         * The parent of the item is missing.
         */
        PARENT_MISSING,
        /**
         * The item is present.
         */
        SUCCESS
    }
}
