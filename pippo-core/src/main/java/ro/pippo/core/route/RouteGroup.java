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
package ro.pippo.core.route;

import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Route groups allow you to prefix <code>uriPattern</code>,
 * across a large number of routes without needing to define this attribute
 * on each individual route.
 * Also you can add (route) filters for all routes of the group.
 *
 * @author ScienJus
 */
public class RouteGroup {

    private String uriPattern;
    private List<Route> routes;
//    private RouteGroup parent;
    private List<RouteGroup> children;

    public RouteGroup(String uriPattern) {
        this(null, uriPattern);
    }

    public RouteGroup(RouteGroup parent, String uriPattern) {
        if (parent != null) {
            this.uriPattern = StringUtils.concatUriPattern(parent.getUriPattern(), uriPattern);
            parent.getChildren().add(this);
        } else {
            this.uriPattern = uriPattern;
        }

//        this.parent = parent;
        this.routes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public String getUriPattern() {
        return this.uriPattern;
    }

    /*
    public RouteGroup getParent() {
        return parent;
    }
    */

    public List<RouteGroup> getChildren() {
        return children;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        Route route = Route.GET(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route GET(RouteHandler routeHandler) {
        return GET("", routeHandler);
    }

    public Route POST(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.POST(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route POST(RouteHandler routeHandler) {
        return POST("", routeHandler);
    }

    public Route DELETE(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.DELETE(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route DELETE(RouteHandler routeHandler) {
        return DELETE("", routeHandler);
    }

    public Route HEAD(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.HEAD(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route HEAD(RouteHandler routeHandler) {
        return HEAD("", routeHandler);
    }

    public Route PUT(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PUT(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route PUT(RouteHandler routeHandler) {
        return PUT("", routeHandler);
    }

    public Route PATCH(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PATCH(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route PATCH(RouteHandler routeHandler) {
        return PATCH("", routeHandler);
    }

    public Route ALL(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.ALL(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    public Route ALL(RouteHandler routeHandler) {
        return ALL("", routeHandler);
    }

    public void addRoute(Route route) {
        route.setGroupUriPattern(this.uriPattern);
        routes.add(route);
    }

}
