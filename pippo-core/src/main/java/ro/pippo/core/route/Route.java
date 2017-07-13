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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, Object> attributes;

    public Route(String requestMethod, String uriPattern, RouteHandler routeHandler) {
        this.requestMethod = requestMethod;
        this.uriPattern = uriPattern;
        setRouteHandler(routeHandler);

        attributes = new HashMap<>();
    }

    /**
     * Copy constructor.
     *
     * @param route
     */
    public Route(Route route) {
        this.requestMethod = route.requestMethod;
        this.uriPattern = route.uriPattern;
        this.routeHandler = route.routeHandler;
        this.absoluteUriPattern = route.absoluteUriPattern;
        this.attributes = new HashMap<>(route.attributes);
        this.name = route.name;
        this.runAsFinally = route.runAsFinally;
    }

    /**
     * Create a {@code GET} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route GET(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.GET, uriPattern, routeHandler);
    }

    /**
     * Create a {@code POST} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route POST(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.POST, uriPattern, routeHandler);
    }

    /**
     * Create a {@code DELETE} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route DELETE(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.DELETE, uriPattern, routeHandler);
    }

    /**
     * Create a {@code HEAD} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route HEAD(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.HEAD, uriPattern, routeHandler);
    }

    /**
     * Create a {@code PUT} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route PUT(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.PUT, uriPattern, routeHandler);
    }

    /**
     * Create a {@code PATCH} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route PATCH(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.PATCH, uriPattern, routeHandler);
    }

    /**
     * Create an {@code OPTIONS} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route OPTIONS(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.OPTIONS, uriPattern, routeHandler);
    }

    /**
     * Create an {@code CONNECT} route.
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route CONNECT(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.CONNECT, uriPattern, routeHandler);
    }

    /**
     * @deprecated Replaced by {@link #ANY(String, RouteHandler)}.
     */
    @Deprecated
    public static Route ALL(String uriPattern, RouteHandler routeHandler) {
        return ANY(uriPattern, routeHandler);
    }

    /**
     * Create a route responding to any HTTP Verb (GET, POST, PUT, ...).
     *
     * @param uriPattern
     * @param routeHandler
     * @return
     */
    public static Route ANY(String uriPattern, RouteHandler routeHandler) {
        return new Route(HttpConstants.Method.ANY, uriPattern, routeHandler);
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

    public void setRouteHandler(RouteHandler routeHandler) {
        if (routeHandler == null) {
            throw new IllegalArgumentException("Route handler cannot be null");
        }

        this.routeHandler = routeHandler;
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

    /**
     * Bind a value to a name. The idea is to extends the standard metadata (uriPattern,
     * requestPattern, name, runAsFinally) and to make the route definition more dynamic.
     *
     * @param name
     * @param value
     * @return
     */
    public Route bind(String name, Object value) {
        attributes.put(name, value);

        return this;
    }

    /**
     * Copies all of the attributes from the specified map to this route.
     *
     * @param attributes
     * @return
     */
    public Route bindAll(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);

        return this;
    }

    /**
     * Returns an unmodifiable view of attributes.
     *
     * @return
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
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
