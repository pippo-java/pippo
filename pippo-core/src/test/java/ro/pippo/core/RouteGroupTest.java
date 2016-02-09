package ro.pippo.core;

import org.junit.Test;
import ro.pippo.core.route.RouteGroupHandler;
import ro.pippo.core.route.RouteMatch;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ScienJus
 * @date 16/2/9.
 */
public class RouteGroupTest {

    @Test
    public void testGroupRoute() {
        Application application = new TestApplication();
        application.init();

        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users");
        assertEquals(1, matches.size());

        matches = application.getRouter().findRoutes(HttpConstants.Method.POST, "/users");
        assertEquals(1, matches.size());

        matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users/1");
        assertEquals(1, matches.size());

        matches = application.getRouter().findRoutes(HttpConstants.Method.PUT, "/users/1");
        assertEquals(1, matches.size());

        matches = application.getRouter().findRoutes(HttpConstants.Method.DELETE, "/users/1");
        assertEquals(1, matches.size());
    }

    // how to test this? can not use Router.addRoute
    private class TestApplication extends Application {

        @Override
        protected void onInit() {

            GROUP("/users", new RouteGroupHandler() {

                @Override
                public void handle() {

                    GET(routeContext -> routeContext.send("all users"));

                    POST(routeContext -> routeContext.send("create a new user"));

                    GROUP("{id}", new RouteGroupHandler() {

                        @Override
                        public void handle() {

                            GET(routeContext -> {
                                routeContext.send("find user, id: " + routeContext.getParameter("id").toString());
                            });

                            PUT(routeContext -> {
                                routeContext.send("modify user, id: " + routeContext.getParameter("id").toString());
                            });

                            DELETE(routeContext -> {
                                routeContext.send("delete user, id: " + routeContext.getParameter("id").toString());
                            });
                        }
                    });
                }
            });
        }
    }
}

