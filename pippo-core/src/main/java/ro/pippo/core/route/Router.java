/*
 * Copyright (C) 2014 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class allows you to do route requests based on the HTTP verb (request method) and the request URI,
 * in a manner similar to Sinatra or Express.
 * Routes are matched in the order they are added/defined.
 * Routing is the process of selecting the best matching candidate from a collection of routes for an incoming request.
 *
 * @author Decebal Suiu
 */
public interface Router {

    /**
     * Gets the current context path.
     *
     * @return the context path
     */
    String getContextPath();

    /**
     * Sets the context path for url generation.
     *
     * @param contextPath
     */
    void setContextPath(String contextPath);

    Set<String> getIgnorePaths();

    void ignorePaths(String... paths);

    /**
     * Add an uncompiled route.
     *
     * @param route
     */
    void addRoute(Route route);

    /**
     * Remove a route (compiled or uncompiled).
     *
     * @param route
     */
    void removeRoute(Route route);

    void addRouteGroup(RouteGroup routeGroup);

    void removeRouteGroup(RouteGroup routeGroup);

    /**
     * Find compiled routes for a request method and uri.
     *
     * @param requestMethod
     * @param requestUri
     * @return
     */
    List<RouteMatch> findRoutes(String requestMethod, String requestUri);

    /**
     * Returns compiled and uncompiled routes.
     *
     * @return
     */
    List<Route> getRoutes();

    /**
     * Compile each added route via {@code addRoute, addRouteGroup} and apply transformers.
     */
    void compileRoutes();

    /**
     * Get uri with application path.
     *
     * @param relativeUri
     * @return
     */
    String uriFor(String relativeUri);

    /**
     * Generate an URI string for a route (referenced by an uriPattern) with some parameters.
     * For example:
     * <pre>
     * // add a route
     * GET("/user", routeContext -> {...});
     *
     * // get an uri string for the above route
     * Map<String, Object> parameters = new HashMap<>();
     * parameters.put("admin", true);
     * parameters.put("company", "Home Office")
     * String uri = uriFor("/user", parameters);
     * // the result is "/user?admin=true&company=Home+Office"
     * </pre>
     * The parameters values are automatically encoded by this method.
     *
     * The returned URI contains the application path.
     *
     * @param nameOrUriPattern
     * @param parameters
     * @return
     */
    String uriFor(String nameOrUriPattern, Map<String, Object> parameters);

    String uriPatternFor(Class<? extends ResourceHandler> resourceHandlerClass);

    /**
     * Returns the base path for the Pippo application.
     * application path = servlet context path + filter path
     */
    String getApplicationPath();

    void setApplicationPath(String pippoPath);

    void addRouteTransformer(RouteTransformer transformer);

    List<RouteTransformer> getRouteTransformers();

}
