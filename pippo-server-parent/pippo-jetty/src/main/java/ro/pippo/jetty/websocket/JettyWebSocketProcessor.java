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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.websocket.WebSocketConnection;
import ro.pippo.core.websocket.WebSocketContext;
import ro.pippo.core.websocket.WebSocketHandler;
import ro.pippo.core.websocket.WebSocketProcessor;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Decebal Suiu
 */
public class JettyWebSocketProcessor implements WebSocketProcessor, WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(JettyWebSocketProcessor.class);

    private static List<WebSocketConnection> connections = new CopyOnWriteArrayList<>();

    private final WebSocketHandler handler;

    private WebSocketContext context;
    private WebSocketConnection connection;

    public JettyWebSocketProcessor(WebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(String message) {
        handler.onMessage(context, message);
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        handler.onMessage(context, data, offset, length);
    }

    @Override
    public void onOpen(Object connection) {
        if (!(connection instanceof Session)) {
            throw new IllegalArgumentException(getClass().getName() + " can work only with " + Session.class.getName());
        }

        onWebSocketConnect((Session) connection);
    }

    @Override
    public void onClose(int closeCode, String message) {
        handler.onClose(context, closeCode, message);
        connections.remove(connection);
    }

    @Override
    public void onError(Throwable t) {
        handler.onError(context, t);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        onMessage(payload, offset, len);
    }

    @Override
    public void onWebSocketText(String message) {
        onMessage(message);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        onClose(statusCode, reason);
    }

    @Override
    public void onWebSocketConnect(Session session) {
        connection = createWebSocketConnection(session);
        connections.add(connection);
        context = createWebSocketContext(connections, connection);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        if (cause instanceof SocketTimeoutException) {
            handler.onTimeout(context);
        } else {
            log.error("An error occurred when using WebSocket.", cause);
            onError(cause);
        }
    }

    protected WebSocketConnection createWebSocketConnection(Session session) {
        return new JettyWebSocketConnection(session);
    }

    protected WebSocketContext createWebSocketContext(List<WebSocketConnection> connections, WebSocketConnection connection) {
        return new WebSocketContext(connections, connection);
    }

}
