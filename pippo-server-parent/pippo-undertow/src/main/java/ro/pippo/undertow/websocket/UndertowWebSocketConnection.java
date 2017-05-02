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

import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.websocket.WebSocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Decebal Suiu
 */
public class UndertowWebSocketConnection implements WebSocketConnection {

    private final WebSocketHttpExchange exchange;
    private final WebSocketChannel channel;

    public UndertowWebSocketConnection(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        this.exchange = exchange;
        this.channel = channel;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close(int code, String reason) {
        channel.setCloseCode(code);
        channel.setCloseReason(reason);
        try {
            channel.sendClose();
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public WebSocketConnection sendMessage(String message) throws IOException {
        checkClosed();
        WebSockets.sendText(message, channel, null);

        return this;
    }

    @Override
    public WebSocketConnection sendMessage(byte[] message) throws IOException {
        checkClosed();
        ByteBuffer buffer = ByteBuffer.wrap(message, 0, message.length);
        WebSockets.sendBinary(buffer, channel, null);

        return this;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.getSourceAddress();
    }

    public WebSocketHttpExchange getExchange() {
        return exchange;
    }

    public WebSocketChannel getChannel() {
        return channel;
    }

    private void checkClosed() {
        if (!isOpen()) {
            throw new IllegalStateException("The connection is closed");
        }
    }

}
