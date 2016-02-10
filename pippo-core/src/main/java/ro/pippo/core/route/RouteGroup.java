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
 * @author ScienJus
 * @date 16/2/9.
 */
public class RouteGroup {

    private String namespace;

    private List<Route> routes = new ArrayList<>();

    private RouteGroup parent;

    private List<RouteGroup> children = new ArrayList<>();

    public RouteGroup withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public RouteGroup getParent() {
        return parent;
    }

    public List<RouteGroup> getChildren() {
        return children;
    }

    public void onInit(){}

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

    public void GROUP(RouteGroup routeGroup) {
        this.addGroup(routeGroup);
    }

    public List<Route> initRoutes() {
        onInit();
        return routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        route.inGroup(this);
    }

    public void addGroup(RouteGroup routeGroup) {
        routeGroup.inGroup(this);
    }

    public void inGroup(RouteGroup routeGroup) {
        this.parent = routeGroup;
        routeGroup.getChildren().add(this);
    }
}
