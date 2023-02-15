package ro.pippo.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import ro.pippo.core.Application;
import ro.pippo.core.websocket.WebSocketContext;
import ro.pippo.core.websocket.WebSocketHandler;
import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class JettyServerTest extends PippoTest {

    @ClassRule
    public static PippoRule pippoRule = new PippoRule(new Application() {{

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

        CompletableFuture<Session> clientSessionPromise = pippoRule.wsConnect(clientEndPoint, "/ws/echo");

        try (Session session = clientSessionPromise.get()) {

            String message = "ping";

            session.getRemote().sendString(message);

            String response = incoming.take();
            Assert.assertEquals(response, message);

        }

    }

    @WebSocket
    @SuppressWarnings("InnerClassMayBeStatic")
    public class WsEchoEndpoint {

        public WsEchoEndpoint(BlockingQueue<String> queue) {
            this.queue = queue;
        }

        @OnWebSocketMessage
        public void onText(@SuppressWarnings("unused") Session session, String message) throws InterruptedException {
            queue.put(message);
        }

        private final BlockingQueue<String> queue;

    }

}
