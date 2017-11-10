/*
 * Copyright (C) 2017 the original author or authors.
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

import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.server.WebSocketServerFactory;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.websocket.AbstractWebSocketFilter;
import ro.pippo.core.websocket.WebSocketRouter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Decebal Suiu
 */
public class JettyWebSocketFilter extends AbstractWebSocketFilter {

    private static final Logger log = LoggerFactory.getLogger(JettyWebSocketFilter.class);

    private WebSocketServerFactory webSocketFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        try {
            WebSocketPolicy serverPolicy = WebSocketPolicy.newServerPolicy();

            String inputBufferSize = filterConfig.getInitParameter("inputBufferSize");
            if (inputBufferSize != null) {
                serverPolicy.setInputBufferSize(Integer.parseInt(inputBufferSize));
            }

            String idleTimeout = filterConfig.getInitParameter("idleTimeout");
            if (idleTimeout != null) {
                serverPolicy.setIdleTimeout(Integer.parseInt(idleTimeout));
            }

            String maxTextMessageSize = filterConfig.getInitParameter("maxTextMessageSize");
            if (maxTextMessageSize != null) {
                serverPolicy.setMaxTextMessageSize(Integer.parseInt(maxTextMessageSize));
            }

            webSocketFactory = new WebSocketServerFactory(filterConfig.getServletContext(), serverPolicy);
            webSocketFactory.setCreator((request, response) -> createWebSocketAdapter(request));
            webSocketFactory.start();
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        if (webSocketFactory != null) {
            try {
                webSocketFactory.stop();
            } catch (Exception e) {
                log.warn("A problem occurred while stopping the web socket factory", e);
            }
        }

        super.destroy();
    }

    @Override
    protected boolean acceptWebSocket(Request request, Response response) throws IOException, ServletException {
        return super.acceptWebSocket(request, response) && webSocketFactory
            .acceptWebSocket(request.getHttpServletRequest(), response.getHttpServletResponse());
    }

    protected JettyWebSocketAdapter createWebSocketAdapter(ServletUpgradeRequest request) {
        WebSocketRouter.WebSocketMatch match = findWebSocketRoute(request.getRequestPath());

        return new JettyWebSocketAdapter(match.getHandler(), match.getPathParameters());
    }

}
