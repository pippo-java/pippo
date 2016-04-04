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

    void addRoute(Route route);

    void removeRoute(Route route);

    void addRouteGroup(RouteGroup routeGroup);

    void removeRouteGroup(RouteGroup routeGroup);

    List<RouteMatch> findRoutes(String requestMethod, String requestUri);

    List<Route> getRoutes();

    void compileRoutes();

    /**
     * Get uri with context path.
     *
     * @param relativeUri
     * @return
     */
    String uriFor(String relativeUri);

    String uriFor(String nameOrUriPattern, Map<String, Object> parameters);

    String uriPatternFor(Class<? extends ResourceHandler> resourceHandlerClass);

    /**
     * Returns the base path for the Pippo application.
     */
    String getApplicationPath();

    void setApplicationPath(String pippoPath);

    void addCompiledRouteTransformer(CompiledRouteTransformer transformer);

}
