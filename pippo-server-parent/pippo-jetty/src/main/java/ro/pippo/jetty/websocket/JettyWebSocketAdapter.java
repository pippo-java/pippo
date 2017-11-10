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

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bridge between two incompatible interfaces: {@link WebSocketHandler} from Pippo and
 * {@link WebSocketListener} from Jetty.
 *
 * @author Decebal Suiu
 */
public class JettyWebSocketAdapter implements WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(JettyWebSocketAdapter.class);

    private static List<WebSocketConnection> connections = new CopyOnWriteArrayList<>();

    private final WebSocketHandler handler;
    private final Map<String, String> pathParameters;

    private WebSocketContext context;
    private WebSocketConnection connection;

    public JettyWebSocketAdapter(WebSocketHandler handler, Map<String, String> pathParameters) {
        this.handler = handler;
        this.pathParameters = pathParameters;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        connection = new JettyWebSocketConnection(session);
        connections.add(connection);
        context = new WebSocketContext(connections, connection, pathParameters);

        handler.onOpen(context);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        handler.onClose(context, statusCode, reason);
        connections.remove(connection);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        if (cause instanceof SocketTimeoutException) {
            handler.onTimeout(context);
        } else {
            log.error("An error occurred when using WebSocket", cause);
            handler.onError(context, cause);
        }
    }

    @Override
    public void onWebSocketText(String message) {
        handler.onMessage(context, message);
    }

    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        ByteBuffer buffer = ByteBuffer.wrap(payload, offset, len);
        handler.onMessage(context, buffer.array());
    }

}
