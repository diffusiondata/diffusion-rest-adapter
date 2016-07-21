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

package com.pushtechnology.adapters.rest.client;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.publication.PublishingClient;

/**
 * Unit tests for {@link PollingComponentImpl}.
 *
 * @author Push Technology Limited
 */
public final class PollingComponentImplTest {

    @Mock
    private PublishingClient publishingClient;

    @Mock
    private ServiceSession serviceSession;

    private ServiceConfig serviceConfig = ServiceConfig.builder().build();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void close() {
        final PollingComponent component = new PollingComponentImpl(
            publishingClient,
            singletonList(serviceConfig),
            singletonList(serviceSession));

        component.close();

        verify(publishingClient).removeService(serviceConfig);
    }

    @Test
    public void closeInactive() {
        PollingComponent.INACTIVE.close();
    }
}
