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

import java.io.File;

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

    default Route ALL(String uriPattern, RouteHandler routeHandler) {
        Route route = Route.ALL(uriPattern, routeHandler);
        addRoute(route);

        return route;
    }

    /**
     * It's a shortcut for {@link #addPublicResourceRoute(String)} with parameter <code>"/public"</code>.
     */
    default Route addPublicResourceRoute() {
        return addPublicResourceRoute("/public");
    }

    /**
     * Add a route that serves resources from the "public" directory within your classpath.
     */
    default Route addPublicResourceRoute(String urlPath) {
        return addResourceRoute(new PublicResourceHandler(urlPath));
    }

    /**
     * Add a route that serves resources from a directory(file system).
     */
    default Route addFileResourceRoute(String urlPath, File directory) {
        return addResourceRoute(new FileResourceHandler(urlPath, directory));
    }

    default Route addFileResourceRoute(String urlPath, String directory) {
        return addResourceRoute(new FileResourceHandler(urlPath, directory));
    }

    default Route addClasspathResourceRoute(String urlPath, Class<?> resourceClass) {
        return addResourceRoute(new ClasspathResourceHandler(urlPath, resourceClass.getName().replace(".", "/")));
    }

    /**
     * Add a route that serves resources from classpath.
     */
    default Route addClasspathResourceRoute(String urlPath, String resourceBasePath) {
        return addResourceRoute(new ClasspathResourceHandler(urlPath, resourceBasePath));
    }

    /**
     * It's a shortcut for {@link #addWebjarsResourceRoute(String)} with parameter <code>"/webjars"</code>.
     */
    default Route addWebjarsResourceRoute() {
        return addWebjarsResourceRoute("/webjars");
    }

    /**
     * Add a route that serves webjars (http://www.webjars.org/) resources.
     */
    default Route addWebjarsResourceRoute(String urlPath) {
        return addResourceRoute(new WebjarsResourceHandler(urlPath));
    }

    default Route addResourceRoute(ResourceHandler resourceHandler) {
        Route route = Route.GET(resourceHandler.getUriPattern(), resourceHandler);
        addRoute(route);

        return route;
    }

}
