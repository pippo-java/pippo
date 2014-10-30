/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

import java.io.Serializable;

/**
 * @author Decebal Suiu
 */
public class Route implements Serializable {

    private String urlPattern;
    private String requestMethod;
    private RouteHandler routeHandler;

    public Route(String urlPattern, String requestMethod, RouteHandler routeHandler) {
        this.urlPattern = urlPattern;
        this.requestMethod = requestMethod;
        this.routeHandler = routeHandler;
    }

    public String getUrlPattern() {
        return urlPattern;
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
        if (!urlPattern.equals(route.urlPattern)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = urlPattern.hashCode();
        result = 31 * result + requestMethod.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Route{" +
                "requestMethod='" + requestMethod + '\'' +
                ", urlPattern='" + urlPattern + '\'' +
                '}';
    }

}
