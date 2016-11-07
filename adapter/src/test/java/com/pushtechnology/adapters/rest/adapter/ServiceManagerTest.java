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

package com.pushtechnology.adapters.rest.adapter;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Unit tests for {@link ServiceManager}.
 *
 * @author Push Technology Limited
 */
public final class ServiceManagerTest {

    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceSessionFactory serviceSessionFactory;
    @Mock
    private ServiceSessionStarter serviceSessionStarter;
    @Mock
    private ServiceSession serviceSession;

    private ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service-0")
        .host("localhost")
        .endpoints(emptyList())
        .topicPathRoot("root")
        .build();
    private Model model = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("localhost")
            .build())
        .services(singletonList(serviceConfig))
        .build();

    private ServiceManagerContext context;

    @Before
    public void setUp() {
        initMocks(this);

        context = new ServiceManagerContext(publishingClient, serviceSessionFactory, serviceSessionStarter);

        when(serviceSessionFactory.create(serviceConfig)).thenReturn(serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, serviceSessionFactory, serviceSessionStarter);
    }

    @Test
    public void configure() {
        final ServiceManager manager = new ServiceManager();

        manager.reconfigure(context, model);

        verify(serviceSessionFactory).create(serviceConfig);
        verify(serviceSessionStarter).start(serviceConfig, serviceSession);
    }

    @Test
    public void reconfigure() {
        final ServiceManager manager = new ServiceManager();

        manager.reconfigure(context, model);

        verify(serviceSessionFactory).create(serviceConfig);
        verify(serviceSessionStarter).start(serviceConfig, serviceSession);

        manager.reconfigure(context, model);

        verify(publishingClient).removeService(serviceConfig);

        verify(serviceSessionFactory, times(2)).create(serviceConfig);
        verify(serviceSessionStarter, times(2)).start(serviceConfig, serviceSession);
    }

    @Test
    public void close() {
        final ServiceManager manager = new ServiceManager();

        manager.reconfigure(context, model);

        verify(serviceSessionFactory).create(serviceConfig);
        verify(serviceSessionStarter).start(serviceConfig, serviceSession);

        manager.close();

        verify(publishingClient).removeService(serviceConfig);
    }
}
