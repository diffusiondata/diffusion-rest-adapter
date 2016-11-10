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

package com.pushtechnology.adapters.rest.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet for providing Diffusion client configuration.
 *
 * @author Push Technology Limited
 */
public final class ConfigServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ServletContext context = getServletContext();
        response.setContentType("application/javascript");
        response.getWriter().println("window.diffusionConfig = {\n" +
            "host: \"" + context.getInitParameter("host") + "\",\n" +
            "port: " + context.getInitParameter("port") + ",\n" +
            "secure: " + context.getInitParameter("secure") + "\n" +
            "};");
    }

    @Override
    public void destroy() {
    }
}
