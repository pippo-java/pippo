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
package ro.pippo.undertow.websocket;

import io.undertow.servlet.websockets.ServletWebSocketHttpExchange;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version07.Hybi07Handshake;
import io.undertow.websockets.core.protocol.version08.Hybi08Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.websocket.AbstractWebSocketFilter;
import ro.pippo.core.websocket.WebSocketRouter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Decebal Suiu
 */
public class UndertowWebSocketFilter extends AbstractWebSocketFilter {

    private Set<WebSocketChannel> peerConnections;
    private Set<Handshake> handshakes;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);

        peerConnections = Collections.newSetFromMap(new ConcurrentHashMap<WebSocketChannel, Boolean>());

        handshakes = new HashSet<>();
        handshakes.add(new Hybi13Handshake());
        handshakes.add(new Hybi08Handshake());
        handshakes.add(new Hybi07Handshake());
    }

    @Override
    protected void processRequest(Request request, Response response) throws IOException, ServletException {
        if (!acceptWebSocket(request, response)) {
            super.processRequest(request, response);
        } else {
            processWebSocketRequest(request, response);
        }
    }

    private void processWebSocketRequest(Request request, Response response) {
        HttpServletRequest servletRequest = request.getHttpServletRequest();
        HttpServletResponse servletResponse = response.getHttpServletResponse();

        WebSocketHttpExchange exchange = new ServletWebSocketHttpExchange(servletRequest, servletResponse, peerConnections);
//        exchange.putAttachment(HandshakeUtil.PATH_PARAMS, Collections.<String, String>emptyMap());

        Handshake handshake = getHandshake(exchange);

        exchange.upgradeChannel((connection, serverExchange) -> {
            WebSocketChannel channel = handshake.createChannel(exchange, connection, exchange.getBufferPool());
            peerConnections.add(channel);
            createWebSocketAdapter(request).onConnect(exchange, channel);
        });

        handshake.handshake(exchange);
    }

    private Handshake getHandshake(WebSocketHttpExchange exchange) {
        for (Handshake handshake : handshakes) {
            if (handshake.matches(exchange)) {
                return handshake;
            }
        }

        throw new PippoRuntimeException("No matching Undertow Handshake found: {}", exchange.getRequestHeaders());
    }

    protected UndertowWebSocketAdapter createWebSocketAdapter(Request request) {
        WebSocketRouter.WebSocketMatch match = findWebSocketRoute(request.getUri());

        return new UndertowWebSocketAdapter(match.getHandler(), match.getPathParameters());
    }

}
