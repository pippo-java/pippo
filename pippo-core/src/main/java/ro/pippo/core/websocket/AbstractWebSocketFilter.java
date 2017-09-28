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
package ro.pippo.core.websocket;

import ro.pippo.core.PippoFilter;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractWebSocketFilter extends PippoFilter {

    @Override
    protected void processRequest(Request request, Response response) throws IOException, ServletException {
        if (!acceptWebSocket(request, response)) {
            super.processRequest(request, response);
        }
    }

    protected boolean acceptWebSocket(Request request, Response response)
        throws IOException, ServletException {
        // information required to send the server handshake message
        if (!headerContainsToken(request, "Upgrade", "websocket")) {
            return false;
        }

        if (!headerContainsToken(request, "Connection", "upgrade")) {
            response.badRequest().commit();
            return false;
        }

        if (!headerContainsToken(request, "Sec-websocket-version", "13")) {
            response.badRequest().header("Sec-WebSocket-Version", "13"); // http://tools.ietf.org/html/rfc6455#section-4.4
            return false;
        }

        String key = request.getHeader("Sec-WebSocket-Key");
        if (key == null) {
            response.badRequest().commit();
            return false;
        }

        String origin = request.getHeader("Origin");
        if (!verifyOrigin(origin)) {
            response.forbidden().commit();
            return false;
        }

        String subProtocol = null;
        List<String> subProtocols = getTokensFromHeader(request, "Sec-WebSocket-Protocol-Client");
        if (!subProtocols.isEmpty()) {
            subProtocol = selectSubProtocol(subProtocols);
        }

        if (subProtocol != null) {
            response.header("Sec-WebSocket-Protocol", subProtocol);
        }

        return true;
    }

    /*
     * This only works for tokens. Quoted strings need more sophisticated parsing.
     */
    private boolean headerContainsToken(Request request, String headerName, String target) {
        Enumeration<String> headers = request.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] tokens = header.split(",");
            for (String token : tokens) {
                if (target.equalsIgnoreCase(token.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * This only works for tokens. Quoted strings need more sophisticated parsing.
     */
    protected List<String> getTokensFromHeader(Request request, String headerName) {
        List<String> result = new ArrayList<>();

        Enumeration<String> headers = request.getHeaders(headerName);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] tokens = header.split(",");
            for (String token : tokens) {
                result.add(token.trim());
            }
        }

        return result;
    }

    /**
     * Intended to be overridden by sub-classes that wish to verify the origin
     * of a WebSocket request before processing it.
     */
    protected boolean verifyOrigin(String origin) {
        return true;
    }

    /**
     * Intended to be overridden by sub-classes that wish to select a
     * sub-protocol if the client provides a list of supported protocols.
     */
    protected String selectSubProtocol(List<String> subProtocols) {
        return null;
    }

    protected WebSocketRouter.WebSocketMatch findWebSocketRoute(String requestUri) {
        String applicationPath = getApplication().getRouter().getContextPath();
        String path = applicationPath.isEmpty() ? requestUri : requestUri.substring(applicationPath.length());
        if (StringUtils.isNullOrEmpty(path)) {
            path = "/";
        }

        return getApplication().getWebSocketRouter().match(path);
    }

}
