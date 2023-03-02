/*
 * Copyright (C) 2015-present the original author or authors.
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
package ro.pippo.test;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.Pippo;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class PippoWebSocketExtension extends PippoExtension {

    private static final Logger log = LoggerFactory.getLogger(PippoWebSocketExtension.class);

    private final WebSocketClient webSocketClient;

    public PippoWebSocketExtension() {
        this(new Application());
    }

    public PippoWebSocketExtension(Application application) {
        this(application, AvailablePortFinder.findAvailablePort());
    }

    public PippoWebSocketExtension(Application application, Integer port) {
        this(new Pippo(application), port);
    }

    public PippoWebSocketExtension(Pippo pippo) {
        this(pippo, AvailablePortFinder.findAvailablePort());
    }

    public PippoWebSocketExtension(Pippo pippo, Integer port) {
        super(pippo, port);

        webSocketClient = new WebSocketClient();
        webSocketClient.setMaxTextMessageSize(8 * 1024);
    }

    @Override
    public void startPippo() {
        super.startPippo();

        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new RuntimeException("Error starting WebSocket client", e);
        }
    }

    @Override
    public void stopPippo() {
        try {
            webSocketClient.stop();
        } catch (Exception e) {
            log.error("Error stopping WebSocket client", e);
        }

        super.stopPippo();
    }

    public CompletableFuture<Session> wsConnect(Object clientEndPoint, String path) throws IOException {
        return webSocketClient.connect(clientEndPoint, URI.create(String.format("ws://%s:%d%s", "localhost", pippo.getServer().getPort(), path)));
    }
}
