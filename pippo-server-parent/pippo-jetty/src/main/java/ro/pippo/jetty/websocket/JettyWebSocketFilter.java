/*
 * Copyright (C) 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.jetty.websocket;

import org.eclipse.jetty.websocket.core.WebSocketComponents;
import org.eclipse.jetty.websocket.core.server.WebSocketServerComponents;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.websocket.AbstractWebSocketFilter;
import ro.pippo.core.websocket.WebSocketRouter;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import ro.pippo.jetty.JettyServer;

import java.io.IOException;
import java.time.Duration;

/**
 * @author Decebal Suiu
 */
public class JettyWebSocketFilter extends AbstractWebSocketFilter {

    private static final Logger log = LoggerFactory.getLogger(JettyWebSocketFilter.class);

    private JettyWebSocketServerContainer serverContainer;
    private JettyWebSocketCreator creator;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        try {

            // TODO

            /*
             * Alternative is JettyWebSocketServletContainerInitializer.configure
             */
            WebSocketComponents components = WebSocketServerComponents.ensureWebSocketComponents(JettyServer._get_DONT_USE(), filterConfig.getServletContext());
            serverContainer = JettyWebSocketServerContainer.ensureContainer(filterConfig.getServletContext());

            String inputBufferSize = filterConfig.getInitParameter("inputBufferSize");
            if (inputBufferSize != null) {
                serverContainer.setInputBufferSize(Integer.parseInt(inputBufferSize));
            }

            String idleTimeout = filterConfig.getInitParameter("idleTimeout");
            if (idleTimeout != null) {
                serverContainer.setIdleTimeout(Duration.ofMillis(Long.parseLong(idleTimeout)));
            }

            String maxTextMessageSize = filterConfig.getInitParameter("maxTextMessageSize");
            if (maxTextMessageSize != null) {
                serverContainer.setMaxTextMessageSize(Integer.parseInt(maxTextMessageSize));
            }

            creator = (request, response) -> createWebSocketAdapter(request);

            serverContainer.start();

        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        if (serverContainer != null) {
            try {
                serverContainer.stop();
            } catch (Exception e) {
                log.warn("A problem occurred while stopping the web socket factory", e);
            }
        }

        super.destroy();
    }

    @Override
    protected boolean acceptWebSocket(Request request, Response response) throws IOException, ServletException {
        return super.acceptWebSocket(request, response) && serverContainer.upgrade(creator, request.getHttpServletRequest(), response.getHttpServletResponse());
    }

    protected JettyWebSocketAdapter createWebSocketAdapter(JettyServerUpgradeRequest request) {
        WebSocketRouter.WebSocketMatch match = findWebSocketRoute(request.getRequestPath());
        return new JettyWebSocketAdapter(match.getHandler(), match.getPathParameters());
    }

}
