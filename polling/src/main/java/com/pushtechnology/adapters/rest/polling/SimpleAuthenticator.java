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

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

import com.pushtechnology.adapters.rest.model.latest.BasicAuthenticationConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * A simple authenticator.
 *
 * @author Push Technology Limited
 */
public final class SimpleAuthenticator extends Authenticator {
    private final Map<AuthScope, PasswordAuthentication> authDetails;

    /**
     * Constructor.
     */
    public SimpleAuthenticator(Model model) {
        authDetails = new HashMap<>();

        // Configure client with Basic authentication credentials
        model
            .getServices()
            .stream()
            .filter(ServiceConfig::isSecure)
            .filter(serviceConfig -> serviceConfig.getSecurity() != null)
            .filter(serviceConfig -> serviceConfig.getSecurity().getBasic() != null)
            .forEach(serviceConfig -> {
                final BasicAuthenticationConfig basic = serviceConfig.getSecurity().getBasic();
                authDetails.put(
                    new AuthScope(serviceConfig.getHost(), serviceConfig.getPort()),
                    new PasswordAuthentication(basic.getUserid(), basic.getPassword().toCharArray()));
            });
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
            return authDetails.get(new AuthScope(getRequestingHost(), getRequestingPort()));
    }

    private static final class AuthScope {
        private final String host;
        private final int port;

        AuthScope(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final AuthScope authScope = (AuthScope) o;
            if (port != authScope.port) {
                return false;
            }
            return host.equals(authScope.host);
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + port;
            return result;
        }
    }
}
