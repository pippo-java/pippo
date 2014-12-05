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
package ro.fortsoft.pippo.core.route;

import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * UrlBuilder generates context-aware URLs for Pippo resources and controllers.
 *
 * @author James Moger
 */
public class UrlBuilder {

    private final RouteMatcher routeMatcher;

    private String contextPath;

    public UrlBuilder(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
        this.contextPath = "/";
    }

    /**
     * Gets the current context path.
     *
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path for url generation.
     *
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = StringUtils.addStart(contextPath, "/");
    }

    /**
     * Prefix the given path with the context path.
     *
     * @param path
     * @return an absolute path
     */
    private String prefixContextPath(String path) {
        if ("/".equals(contextPath)) {
            // context path is the root
            return StringUtils.addStart(path, "/");
        }
        return contextPath + StringUtils.addStart(path, "/");
    }

    public RouteMatcher getRouteMatcher() {
        return routeMatcher;
    }

    public String urlFor(String urlPattern, Map<String, Object> parameters) {
        return prefixContextPath(routeMatcher.urlFor(urlPattern, parameters));
    }

    public String urlPatternFor(Class<? extends Controller> controllerClass, String methodName) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? route.getUrlPattern() : null;
    }

    public String urlFor(Class<? extends Controller> controllerClass, String methodName, Map<String, Object> parameters) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? prefixContextPath(urlFor(route.getUrlPattern(), parameters)) : null;
    }

    private Route getRoute(Class<? extends Controller> controllerClass, String methodName) {
        List<Route> routes = routeMatcher.getRoutes();
        for (Route route : routes) {
            RouteHandler routeHandler = route.getRouteHandler();
            if (routeHandler instanceof ControllerHandler) {
                ControllerHandler controllerHandler = (ControllerHandler) routeHandler;
                if (controllerClass == controllerHandler.getControllerClass()
                        && methodName.equals(controllerHandler.getMethodName())) {
                    return route;
                }
            }
        }

        return null;
    }

    public String urlPatternFor(Class<? extends ClasspathResourceHandler> resourceHandlerClass) {
        Route route = getRoute(resourceHandlerClass);

        return (route != null) ? route.getUrlPattern() : null;
    }

    private Route getRoute(Class<? extends ClasspathResourceHandler> resourceHandlerClass) {
        List<Route> routes = routeMatcher.getRoutes();
        for (Route route : routes) {
            RouteHandler routeHandler = route.getRouteHandler();
            if (resourceHandlerClass.isAssignableFrom(routeHandler.getClass())) {
                ClasspathResourceHandler resourceHandler = (ClasspathResourceHandler) routeHandler;
                if (resourceHandlerClass == resourceHandler.getClass()) {
                    return route;
                }
            }
        }
        return null;
    }
}
