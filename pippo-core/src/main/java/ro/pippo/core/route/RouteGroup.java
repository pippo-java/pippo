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
public abstract class RouteGroup {

    private String namespace;

    private List<Route> routes = new ArrayList<>();

    public abstract void onInit();

    public Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        Route route = Route.GET(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route GET(RouteHandler routeHandler) {
        return GET("", routeHandler);
    }

    public Route POST(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.POST(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route POST(RouteHandler routeHandler) {
        return POST("", routeHandler);
    }

    public Route DELETE(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.DELETE(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route DELETE(RouteHandler routeHandler) {
        return DELETE("", routeHandler);
    }

    public Route HEAD(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.HEAD(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route HEAD(RouteHandler routeHandler) {
        return HEAD("", routeHandler);
    }

    public Route PUT(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PUT(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route PUT(RouteHandler routeHandler) {
        return PUT("", routeHandler);
    }

    public Route PATCH(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PATCH(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }

    public Route PATCH(RouteHandler routeHandler) {
        return PATCH("", routeHandler);
    }

    public Route ALL(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.ALL(buildPath(uriPattern), routeHandler);
        routes.add(route);

        return route;
    }
    public Route ALL(RouteHandler routeHandler) {
        return ALL("", routeHandler);
    }

    public void GROUP(String namespace, RouteGroup routeGroup) {
        if (StringUtils.isNullOrEmpty(namespace)) {
            throw new PippoRuntimeException("The group namespace cannot be null or empty");
        }
        routes.addAll(routeGroup.routes(buildPath(namespace)));
    }

    public List<Route> routes(String namespace) {
        this.namespace = namespace;
        onInit();
        return routes;
    }

    private String buildPath(String uriPattern) {
        String routePath = StringUtils.addEnd(StringUtils.addEnd(namespace, "/"), uriPattern);
        return routePath.endsWith("/") ? routePath.substring(0, routePath.length() - 1) : routePath;
    }
}
