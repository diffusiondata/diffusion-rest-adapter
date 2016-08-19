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

package com.pushtechnology.adapters.rest.integrated.server;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.verification.VerificationWithTimeout;

/**
 * Unit tests for {@link RESTAdapterIntegratedServer}.
 *
 * @author Push Technology Limited
 */
public final class RESTAdapterIntegratedServerIT {

    @Mock
    private FutureCallback<HttpResponse> httpResponse;

    private CloseableHttpAsyncClient httpClient;

    @Before
    public void setUp() {
        initMocks(this);

        httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
    }

    @After
    public void postConditions() throws IOException {
        httpClient.close();

        verifyNoMoreInteractions(httpResponse);
    }

    @Test
    public void startClose() throws Exception {
        final RESTAdapterIntegratedServer restAdapterIntegratedServer = RESTAdapterIntegratedServer.create(8081);

        restAdapterIntegratedServer.start();

        httpClient.execute(new HttpGet("http://localhost:8081"), httpResponse);

        verify(httpResponse, timed()).completed(isA(HttpResponse.class));

        restAdapterIntegratedServer.close();
    }

    private VerificationWithTimeout timed() {
        return timeout(10000);
    }

}
