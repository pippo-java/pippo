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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public interface WebSocketHandler {

    Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    /**
     * Called when a text message arrives from the client.
     *
     * @param message
     *      the text message from the client
     */
    void onMessage(WebSocketContext webSocketContext, String message);

    /**
     * Called when a binary message arrives from the client.
     * @param message
     *      the binary message from the client
     *
     */
    default void onMessage(WebSocketContext webSocketContext, byte[] message) {
        // do nothing
    }

    /**
     * A client successfully has made a web socket connection.
     */
    default void onOpen(WebSocketContext webSocketContext) {
        log.debug("Open websocket connection");
    }

    /**
     * A notification after the close of the web socket connection.
     * The connection could be closed by either the client or the server
     *
     * @param closeCode
     * @param message
     */
    default void onClose(WebSocketContext webSocketContext, int closeCode, String message) {
        log.debug("Close websocket connection");
    }

    /**
     * A notification that the web socket does not receive any message for the specified timeout period.
     */
    default void onTimeout(WebSocketContext webSocketContext) {
        log.debug("Timeout websocket connection");
    }

    /**
     * A notification after a communication error.
     *
     * @param t
     *      The throwable for the communication problem
     */
    default void onError(WebSocketContext webSocketContext, Throwable t) {
        log.error("Error websocket", t);
    }

}
