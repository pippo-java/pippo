/*
 * Copyright (C) 2016 the original author or authors.
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
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.util.StringUtils;
import ro.pippo.core.websocket.WebSocketConnection;
import ro.pippo.core.websocket.WebSocketHandler;
import ro.pippo.core.websocket.WebSocketProcessor;

import java.net.SocketTimeoutException;

/**
 * @author Decebal Suiu
 */
public class JettyWebSocketProcessor implements WebSocketProcessor, WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(JettyWebSocketProcessor.class);

//    private final List<WebSocketConnection> connections; // used by WebSocketConnection.broadcastMessage(String message)
    private final WebSocketHandler handler;

    private WebSocketConnection connection;

    public JettyWebSocketProcessor(ServletUpgradeRequest req, ServletUpgradeResponse resp, Application application) {
//        this.connections = new CopyOnWriteArrayList<>();

        // TODO: make this outside ?!
        // init path
        String applicationPath = application.getRouter().getContextPath();
        String requestUri = req.getRequestPath();
        String path = applicationPath.isEmpty() ? requestUri : requestUri.substring(applicationPath.length());
        if (StringUtils.isNullOrEmpty(path)) {
            path = "/";
        }

        handler = application.getWebSocketHandler(path);
    }

    @Override
    public void onMessage(String message) {
        handler.onMessage(connection, message);
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        handler.onMessage(connection, data, offset, length);
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
        // TODO
//        connections.remove();
        handler.onClose(connection, closeCode, message);
    }

    @Override
    public void onError(Throwable t) {
        // TODO
        handler.onError(connection, t);
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
        connection = new JettyWebSocketConnection(session);
        onConnect(connection);
    }

    @Override
    public void onWebSocketError(Throwable cause) {

        if (cause instanceof SocketTimeoutException) {
            handler.onTimeout(connection);
        } else {
            log.error("An error occurred when using WebSocket.", cause);
            onError(cause);
        }
    }

    protected final void onConnect(WebSocketConnection connection) {
//        connections.add(connection);
    }

}
