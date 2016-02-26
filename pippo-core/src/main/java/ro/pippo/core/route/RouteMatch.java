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

import java.util.Map;

/**
 * Represent an entry/item from the result of routing.
 * Routing is the process of selecting the best matching candidate from a collection of routes for an incoming request.
 *
 * @author Decebal Suiu
 */
public class RouteMatch {

    private final Route route;
    private final Map<String, String> pathParameters;

    public RouteMatch(Route route, Map<String, String> pathParameters) {
        this.route = route;
        this.pathParameters = pathParameters;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    @Override
    public String toString() {
        return "RouteMatch{" +
            "requestMethod='" + route.getRequestMethod() + '\'' +
            ", uriPattern='" + route.getUriPattern() + '\'' +
            '}';
    }

}
