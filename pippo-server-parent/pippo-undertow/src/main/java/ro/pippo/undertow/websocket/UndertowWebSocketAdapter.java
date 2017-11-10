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

import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Pooled;
import ro.pippo.core.websocket.WebSocketConnection;
import ro.pippo.core.websocket.WebSocketContext;
import ro.pippo.core.websocket.WebSocketHandler;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bridge between two incompatible interfaces: {@link WebSocketHandler} from Pippo and
 * {@link WebSocketConnectionCallback}, {@link AbstractReceiveListener} from Undertow.
 *
 * @author Decebal Suiu
 */
public class UndertowWebSocketAdapter extends AbstractReceiveListener implements WebSocketConnectionCallback {

    private static final Logger log = LoggerFactory.getLogger(UndertowWebSocketAdapter.class);

    private static List<WebSocketConnection> connections = new CopyOnWriteArrayList<>();

    private final WebSocketHandler handler;
    private final Map<String, String> pathParameters;

    private WebSocketContext context;
    private WebSocketConnection connection;

    public UndertowWebSocketAdapter(WebSocketHandler handler, Map<String, String> pathParameters) {
        this.handler = handler;
        this.pathParameters = pathParameters;
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set(this);
        channel.resumeReceives();

        connection = new UndertowWebSocketConnection(exchange, channel);
        connections.add(connection);
        context = new WebSocketContext(connections, connection, pathParameters);

        handler.onOpen(context);
    }

    @Override
    protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
        handler.onClose(context, cm.getCode(), cm.getReason());
        connections.remove(connection);
    }

    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        super.onError(channel, error);

        if (error instanceof SocketTimeoutException) {
            handler.onTimeout(context);
        } else {
            log.error("An error occurred when using WebSocket", error);
            handler.onError(context, error);
        }
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        handler.onMessage(context, message.getData());
    }

    @Override
    protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
        Pooled<ByteBuffer[]> pulledData = message.getData();
        try {
            ByteBuffer[] resource = pulledData.getResource();
            ByteBuffer buffer = WebSockets.mergeBuffers(resource);
            handler.onMessage(context, buffer.array());
        } finally {
            pulledData.discard();
        }
    }

}
