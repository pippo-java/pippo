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
package ro.pippo.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ro.pippo.core.Application;
import ro.pippo.core.websocket.WebSocketContext;
import ro.pippo.core.websocket.WebSocketHandler;
import ro.pippo.test.PippoTest;
import ro.pippo.test.PippoWebSocketExtension;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class JettyServerTest extends PippoTest {

    @RegisterExtension
    public static PippoWebSocketExtension pippoExtension = new PippoWebSocketExtension(new Application() {{

        GET("/foo", context -> context.send("foo"));

        addWebSocket("/ws/echo", new WebSocketHandler() {
            @Override
            public void onMessage(WebSocketContext context, String message) {
                try {
                    context.getConnection().sendMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to echo message", e);
                }
            }
            @Override
            public void onMessage(WebSocketContext context, byte[] message) {
                try {
                    context.getConnection().sendMessage(message);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to echo message", e);
                }
            }
        });

    }});

    @Test
    public void testPlainTextGet() {
        when()
            .get("/foo")
        .then()
            .statusCode(200)
            .body(Matchers.containsString("foo"))
        ;
    }

    @Test
    public void testWsText() throws IOException, ExecutionException, InterruptedException {
        BlockingQueue<String> incoming = new LinkedBlockingQueue<>();

        WsEchoEndpoint clientEndPoint = new WsEchoEndpoint(incoming);

        CompletableFuture<Session> clientSessionPromise = pippoExtension.wsConnect(clientEndPoint, "/ws/echo");

        try (Session session = clientSessionPromise.get()) {
            String message = "ping";

            session.getRemote().sendString(message);

            String response = incoming.take();
            Assertions.assertEquals(response, message);
        }
    }

    @WebSocket
    @SuppressWarnings("InnerClassMayBeStatic")
    public class WsEchoEndpoint {

        private final BlockingQueue<String> queue;

        public WsEchoEndpoint(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        @OnWebSocketMessage
        public void onText(@SuppressWarnings("unused") Session session, String message) throws InterruptedException {
            queue.put(message);
        }

    }

}
