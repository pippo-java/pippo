/*
 * Copyright (C) 2021-present the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;

import java.lang.reflect.Method;

/**
 * Default {@link ControllerRouteFactory} implementation.
 *
 * @author Decebal Suiu
 */
public class DefaultControllerRouteFactory implements ControllerRouteFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultControllerRouteFactory.class);

    private final ControllerApplication application;

    public DefaultControllerRouteFactory(ControllerApplication application) {
        this.application = application;
    }

    @Override
    public Route createRoute(String requestMethod, String uriPattern, Method controllerMethod) {
        // create the route handler
        RouteHandler<?> handler = new ControllerHandler(application, controllerMethod);

        // create the route
        Route route = new Route(requestMethod, uriPattern, handler)
            .bind("__controllerClass", controllerMethod.getDeclaringClass())
            .bind("__controllerMethod", controllerMethod);

        log.debug("Created route '{}' for '{}'", route, controllerMethod);

        return route;
    }

}
