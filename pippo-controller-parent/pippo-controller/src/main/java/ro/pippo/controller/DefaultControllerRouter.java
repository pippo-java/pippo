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
package ro.pippo.controller;

import ro.pippo.core.route.DefaultRouter;
import ro.pippo.core.route.Route;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * The routes are matched in the order they are defined.
 *
 * @author Decebal Suiu
 */
public class DefaultControllerRouter extends DefaultRouter implements ControllerRouter {

    public DefaultControllerRouter() {
        super();
    }

    @Override
    public String uriFor(Class<? extends Controller> controllerClass, String methodName) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? route.getUriPattern() : null;
    }

    @Override
    public String uriFor(Class<? extends Controller> controllerClass, String methodName, Map<String, Object> parameters) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? prefixApplicationPath(uriFor(route.getUriPattern(), parameters)) : null;
    }

    private Route getRoute(Class<? extends Controller> controllerClass, String methodName) {
        List<Route> routes = getRoutes();
        for (Route route : routes) {
            // TODO
            Method controllerMethod = route.getAttribute("__controllerMethod");
            if (controllerMethod != null) {
                if (controllerClass == route.getAttribute("__controllerClass")
                    && methodName.equals(controllerMethod.getName())) {
                    return route;
                }
            }
        }

        return null;
    }

}
