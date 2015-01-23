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

/**
 * @author Decebal Suiu
 */
public class Route {

    private String uriPattern;
    private String requestMethod;
    private RouteHandler routeHandler;

    public Route(String uriPattern, String requestMethod, RouteHandler routeHandler) {
        this.uriPattern = uriPattern;
        this.requestMethod = requestMethod;
        this.routeHandler = routeHandler;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public RouteHandler getRouteHandler() {
        return routeHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Route route = (Route) o;

        if (!requestMethod.equals(route.requestMethod)) return false;
        if (!uriPattern.equals(route.uriPattern)) return false;

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
                ", uriPattern='" + uriPattern + '\'' +
                '}';
    }

}
