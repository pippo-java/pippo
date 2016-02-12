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
import ro.pippo.core.util.StringUtils;

/**
 * @author Decebal Suiu
 */
public class Route {

    private String requestMethod;
    private String uriPattern;
    private RouteHandler routeHandler;

    private boolean runAsFinally;
    private String name;
    private RouteGroup group;
    private String absoluteUriPattern;

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
        return getAbsoluteUriPattern();
    }

    public String getAbsoluteUriPattern() {
        if (absoluteUriPattern == null) {
            RouteGroup group = this.group;
            String path = this.uriPattern;
            while (group != null) {
                path = StringUtils.addStart(StringUtils.addStart(path, "/"), group.getUriPattern());
                group = group.getParent();
            }
            absoluteUriPattern = "/".equals(path) ? path : StringUtils.removeEnd(path, "/");
        }

        return absoluteUriPattern;
    }

    public String getRelativeUriPattern() {
        return uriPattern;
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

    public Route inGroup(RouteGroup group) {
        this.group = group;
        group.getRoutes().add(this);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        if (!requestMethod.equals(route.requestMethod)) return false;
        if (!getUriPattern().equals(route.getUriPattern())) return false;

        return true;
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
