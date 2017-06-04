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

package com.pushtechnology.adapters.rest.session.management;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;

/**
 * Unit tests for {@link SSLContextFactory}.
 *
 * @author Push Technology Limited
 */
public final class SSLContextFactoryTest {

    private final Model model = Model
        .builder()
        .diffusion(DiffusionConfig
            .builder()
            .host("localhost")
            .build())
        .services(emptyList())
        .metrics(MetricsConfig
            .builder()
            .build())
        .build();

    private final SSLContextFactory contextFactory = new SSLContextFactory();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void provideNoTruststore() {
        assertNull(contextFactory.provide(model));
    }
}
