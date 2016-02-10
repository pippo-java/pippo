/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.core;

import org.junit.Test;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteGroup;
import ro.pippo.core.route.RouteMatch;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ScienJus
 * @date 16/2/9.
 */
public class RouteGroupTest {

    @Test
    public void testOnInit() {
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

    @Test
    public void testAddGroup() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                group.GET("{id}", routeContext -> routeContext.send("all posts"));
                addGroup(group);
            }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users/1");
        assertEquals(1, matches.size());
    }

    @Test
    public void testAddRoute() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                Route route = group.GET("{id}", routeContext -> routeContext.send("all posts"));
                addRoute(route);
            }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users/1");
        assertEquals(1, matches.size());
    }

    @Test
    public void testGroupAddRoute() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                Route route = new Route("GET", "{id}", routeContext -> {routeContext.send("all users");});
                group.addRoute(route);
                addGroup(group);
             }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users/1");
        assertEquals(1, matches.size());
    }

    @Test
    public void testInGroup() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                Route route = new Route("GET", "{id}", routeContext -> {routeContext.send("all users");});
                route.inGroup(group);
                addRoute(route);
            }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.GET, "/users/1");
        assertEquals(1, matches.size());
    }

    @Test
    public void testGroupAddGroup() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                RouteGroup child = new RouteGroup().withNamespace("{id}");
                Route route = new Route("POST", "like", routeContext -> {routeContext.send("i like you");});
                group.addGroup(child);
                child.addRoute(route);
                addGroup(group);
            }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.POST, "/users/1/like");
        assertEquals(1, matches.size());
    }

    @Test
    public void testGroupInGroup() {
        Application application = new Application(){

            @Override
            protected void onInit() {
                RouteGroup group = new RouteGroup().withNamespace("/users");
                RouteGroup child = new RouteGroup().withNamespace("{id}");
                Route route = new Route("POST", "like", routeContext -> {routeContext.send("i like you");});
                child.inGroup(group);
                route.inGroup(child);
                addRoute(route);
            }

        };
        application.init();
        List<RouteMatch> matches = application.getRouter().findRoutes(HttpConstants.Method.POST, "/users/1/like");
        assertEquals(1, matches.size());
    }

    // how to test this? can not use Router.addRoute
    private class TestApplication extends Application {

        // create group with onInit
        @Override
        protected void onInit() {

            GROUP(new RouteGroup() {

                @Override
                public void onInit() {

                    GET(routeContext -> routeContext.send("all users"));

                    POST(routeContext -> routeContext.send("create a new user"));

                    GROUP(new RouteGroup() {

                        @Override
                        public void onInit() {

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
                    }.withNamespace("{id}"));
                }
            }.withNamespace("/users"));
        }
    }
}

