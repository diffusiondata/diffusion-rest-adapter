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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.services.ServiceSessionFactory;

/**
 * Unit tests for {@link ServiceManagerContext}.
 *
 * @author Push Technology Limited
 */
public final class ServiceManagerContextTest {

    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceSessionFactory serviceSessionFactory;

    private ServiceManagerContext context;

    @Before
    public void setUp() {
        initMocks(this);

        context = new ServiceManagerContext(publishingClient, serviceSessionFactory);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, serviceSessionFactory);
    }

    @Test
    public void getPublishingClient() {
        assertEquals(publishingClient, context.getPublishingClient());
    }

    @Test
    public void getServiceSessionFactory() {
        assertEquals(serviceSessionFactory, context.getServiceSessionFactory());
    }
}
