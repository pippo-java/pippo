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
    private RouteGroup parent;
    private List<RouteGroup> children;

    public RouteGroup(String uriPattern) {
        this.uriPattern = uriPattern;
        this.routes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public String getUriPattern() {
        return this.uriPattern;
    }

    public RouteGroup getParent() {
        return parent;
    }

    public List<RouteGroup> getChildren() {
        return children;
    }

    public Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        return Route.GET(uriPattern, routeHandler).inGroup(this);
    }

    public Route GET(RouteHandler routeHandler) {
        return GET("", routeHandler);
    }

    public Route POST(String uriPattern, RouteHandler routeHandler) {
        return Route.POST(uriPattern, routeHandler).inGroup(this);
    }

    public Route POST(RouteHandler routeHandler) {
        return POST("", routeHandler);
    }

    public Route DELETE(String uriPattern, RouteHandler routeHandler) {
        return Route.DELETE(uriPattern, routeHandler).inGroup(this);
    }

    public Route DELETE(RouteHandler routeHandler) {
        return DELETE("", routeHandler);
    }

    public Route HEAD(String uriPattern, RouteHandler routeHandler) {
        return Route.HEAD(uriPattern, routeHandler).inGroup(this);
    }

    public Route HEAD(RouteHandler routeHandler) {
        return HEAD("", routeHandler);
    }

    public Route PUT(String uriPattern, RouteHandler routeHandler) {
        return Route.PUT(uriPattern, routeHandler).inGroup(this);
    }

    public Route PUT(RouteHandler routeHandler) {
        return PUT("", routeHandler);
    }

    public Route PATCH(String uriPattern, RouteHandler routeHandler) {
        return Route.PATCH(uriPattern, routeHandler).inGroup(this);
    }

    public Route PATCH(RouteHandler routeHandler) {
        return PATCH("", routeHandler);
    }

    public Route ALL(String uriPattern, RouteHandler routeHandler) {
        return Route.ALL(uriPattern, routeHandler).inGroup(this);
    }

    public Route ALL(RouteHandler routeHandler) {
        return ALL("", routeHandler);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public RouteGroup addRoute(Route route) {
        route.inGroup(this);

        return this;
    }

    public RouteGroup addRouteGroup(RouteGroup routeGroup) {
        routeGroup.inGroup(this);

        return this;
    }

    public RouteGroup inGroup(RouteGroup routeGroup) {
        this.parent = routeGroup;
        routeGroup.getChildren().add(this);

        return this;
    }

}
