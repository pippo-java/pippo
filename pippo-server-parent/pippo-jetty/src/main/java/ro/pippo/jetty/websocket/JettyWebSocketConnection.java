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
import ro.pippo.core.websocket.WebSocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author Decebal Suiu
 */
public class JettyWebSocketConnection implements WebSocketConnection {

    private final Session session;

    public JettyWebSocketConnection(Session session) {
        this.session = session;
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }

    @Override
    public void close(int code, String reason) {
        if (isOpen()) {
            session.close(code, reason);
        }
    }

    @Override
    public WebSocketConnection sendMessage(String message) throws IOException {
        checkClosed();
        session.getRemote().sendString(message);

        return this;
    }

    @Override
    public WebSocketConnection sendMessage(byte[] message) throws IOException {
        checkClosed();
        ByteBuffer buffer = ByteBuffer.wrap(message, 0, message.length);
        session.getRemote().sendBytes(buffer);

        return this;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return session.getRemoteAddress();
    }

    public Session getSession() {
        return session;
    }

    private void checkClosed() {
        if (!isOpen()) {
            throw new IllegalStateException("The connection is closed");
        }
    }

}
