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

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Decebal Suiu
 */
public interface WebSocketConnection {

    /**
     * @return {@code true} when the underlying native web socket connection is still open.
     */
    boolean isOpen();

    /**
     * Closes the underlying web socket connection.
     */
    void close(int code, String reason);

    /**
     * Sends a text message to the client.
     *
     * @return {@code this} object, for chaining methods
     * @throws IOException when an IO error occurs during the write to the client
     */
    WebSocketConnection sendMessage(String message) throws IOException;

    /**
     * Sends a binary message to the client.
     *
     * @return {@code this} object, for chaining methods
     * @throws IOException when an IO error occurs during the write to the client
     */
    WebSocketConnection sendMessage(byte[] message) throws IOException;

    InetSocketAddress getRemoteAddress();

}
