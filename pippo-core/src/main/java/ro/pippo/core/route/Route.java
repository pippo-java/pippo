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

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;

/**
 * @author Decebal Suiu
 */
public class Route {

    private String requestMethod;
    private String uriPattern;
    private RouteHandler routeHandler;
    private String absoluteUriPattern;

    private boolean runAsFinally;
    private String name;

    public Route(String requestMethod, String uriPattern, RouteHandler routeHandler) {
        this.requestMethod = requestMethod;
        this.uriPattern = uriPattern;
        this.routeHandler = routeHandler;
    }

    /**
     * Create a GET route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route GET(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.GET, uriPattern, routeHandler);
    }

    /**
     * Create a POST route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route POST(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.POST, uriPattern, routeHandler);
    }

    /**
     * Create a DELETE route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route DELETE(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.DELETE, uriPattern, routeHandler);
    }

    /**
     * Create a HEAD route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route HEAD(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.HEAD, uriPattern, routeHandler);
    }

    /**
     * Create a PUT route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route PUT(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.PUT, uriPattern, routeHandler);
    }

    /**
     * Create a PATCH route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route PATCH(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.PATCH, uriPattern, routeHandler);
    }

    /**
     * Create a ALL route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route ALL(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.ALL, uriPattern, routeHandler);
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getUriPattern() {
        return (absoluteUriPattern != null) ? absoluteUriPattern : uriPattern;
    }

    public RouteHandler getRouteHandler() {
        return routeHandler;
    }

    public boolean isRunAsFinally() {
        return runAsFinally;
    }

    /**
     * Mark this route to be invoked even when exceptions were raised in previous routes.
     * This flag make sense only for an after filter.
     */
    public void runAsFinally() {
        runAsFinally = true;
    }

    public String getName() {
        return name;
    }

    public Route named(String name) {
        this.name = name;

        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAbsoluteUriPattern(String absoluteUriPattern) {
        if (this.absoluteUriPattern != null) {
            // when group1.addRoute(route); group2.addRoute(route);
            throw new PippoRuntimeException("This route is already in a group");
        }

        this.absoluteUriPattern = absoluteUriPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Route route = (Route) o;

        return requestMethod.equals(route.requestMethod) && getUriPattern().equals(route.getUriPattern());

    }

    @Override
    public int hashCode() {
        int result = uriPattern.hashCode();
        result = 31 * result + requestMethod.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Route{" +
            "requestMethod='" + requestMethod + '\'' +
            ", uriPattern='" + getUriPattern() + '\'' +
            '}';
    }

}
