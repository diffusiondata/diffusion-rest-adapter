/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.polling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.SecurityConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Unit tests for {@link SimpleAuthenticator}.
 *
 * @author Push Technology Limited
 */
public final class SimpleAuthenticatorTest {
    @Test
    public void returnsCredentials() throws MalformedURLException {
        final Model model = Model
            .builder()
            .services(List.of(
                ServiceConfig
                    .builder()
                    .name("example")
                    .host("example.com")
                    .topicPathRoot("root")
                    .port(80)
                    .security(
                        SecurityConfig
                            .builder()
                            .basic(
                                BasicAuthenticationConfig
                                    .builder()
                                    .userid("user")
                                    .password("password")
                                    .build())
                            .build())
                    .build()))
            .build();

        final SimpleAuthenticator authenticator = new SimpleAuthenticator(model);

        authenticator.requestPasswordAuthenticationInstance("example.com", null, 80, "http", "", "", new URL("http://example.com"), Authenticator.RequestorType.SERVER);

        final PasswordAuthentication passwordAuthentication = authenticator.getPasswordAuthentication();

        assertEquals("user", passwordAuthentication.getUserName());
        assertArrayEquals("password".toCharArray(), passwordAuthentication.getPassword());
    }
}
