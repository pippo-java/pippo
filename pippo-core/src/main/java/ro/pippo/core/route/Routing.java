/*
 * Copyright (C) 2016 the original author or authors.
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

/**
 * @author Decebal Suiu
 */
public interface Routing {

    void addRoute(Route route);

    void addRouteGroup(RouteGroup routeGroup);

    default Route GET(String uriPattern, RouteHandler routeHandler) {
        if (routeHandler instanceof ResourceHandler) {
            throw new PippoRuntimeException("Please use 'addResourceRoute()'");
        }

        Route route = Route.GET(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route POST(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.POST(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route DELETE(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.DELETE(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route HEAD(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.HEAD(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route PUT(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PUT(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route PATCH(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.PATCH(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route CONNECT(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.CONNECT(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    default Route OPTIONS(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.OPTIONS(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    /**
     * @deprecated Replaced by {@link #ANY(String, RouteHandler)}.
     */
    @Deprecated
    default Route ALL(String uriPattern, RouteHandler routeHandler) {
        return ANY(uriPattern, routeHandler);
    }

    default Route ANY(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.ANY(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

}
