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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Route groups allow you to prefix <code>uriPattern</code>,
 * across a large number of routes without needing to define this attribute
 * on each individual route.
 * Also you can add (route) filters for all routes of the group.
 *
 * @author ScienJus
 * @author Decebal Suiu
 */
public class RouteGroup implements Routing {

    private String uriPattern;
    private List<Route> routes;
    private RouteGroup parent;
    private List<RouteGroup> children;

    private String name;

    private Map<String, Object> attributes;

    public RouteGroup(String uriPattern) {
        this(null, uriPattern);
    }

    public RouteGroup(RouteGroup parent, String uriPattern) {
        this.uriPattern = uriPattern;
        this.parent = parent;

        if (parent != null) {
            parent.children.add(this);
        }

        routes = new ArrayList<>();
        children = new ArrayList<>();
        attributes = new HashMap<>();
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

    public List<Route> getRoutes() {
        return routes;
    }

    @Override
    public void addRoute(Route route) {
        routes.add(route);
    }

    @Override
    public void addRouteGroup(RouteGroup routeGroup) {
        routeGroup.parent = this;
        children.add(routeGroup);
    }

    public String getName() {
        return name;
    }

    public RouteGroup named(String name) {
        this.name = name;

        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RouteGroup bind(String name, Object value) {
        attributes.put(name, value);

        return this;
    }

    /**
     * Copies all of the attributes from the specified map to this route group.
     *
     * @param attributes
     * @return
     */
    public RouteGroup bindAll(Map<String, Object> attributes) {
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

}
