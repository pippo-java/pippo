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
import ro.pippo.core.ContentTypeEngines;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;

import java.lang.reflect.Method;

/**
 * Default {@link ControllerRouteFactory} implementation.
 * {@link DefaultControllerFactory} is used if a custom {@link ControllerFactory} is not supplied
 * via {@link DefaultControllerRouteFactory::setControllerFactory}.
 *
 * @author Decebal Suiu
 */
public class DefaultControllerRouteFactory implements ControllerRouteFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultControllerRouteFactory.class);

    private ContentTypeEngines contentTypeEngines;
    private ControllerFactory controllerFactory;

    @Override
    public Route createRoute(String requestMethod, String uriPattern, Method controllerMethod) {
        // create the route handler
        RouteHandler<?> handler = createRouteHandler(controllerMethod);

        // create the route
        Route route = new Route(requestMethod, uriPattern, handler)
            .bind("__controllerClass", controllerMethod.getDeclaringClass())
            .bind("__controllerMethod", controllerMethod);

        log.debug("Created route '{}' for '{}'", route, controllerMethod);

        return route;
    }

    protected RouteHandler<?> createRouteHandler(Method controllerMethod) {
        return new ControllerHandler(getContentTypeEngines(), controllerMethod).setControllerFactory(getControllerFactory());
    }

    public ContentTypeEngines getContentTypeEngines() {
        if (contentTypeEngines == null) {
            return new ContentTypeEngines();
        }

        return contentTypeEngines;
    }

    public DefaultControllerRouteFactory setContentTypeEngines(ContentTypeEngines contentTypeEngines) {
        this.contentTypeEngines = contentTypeEngines;

        return this;
    }

    public ControllerFactory getControllerFactory() {
        if (controllerFactory == null) {
            controllerFactory = new DefaultControllerFactory();
        }

        return controllerFactory;
    }

    public DefaultControllerRouteFactory setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;

        return this;
    }

}
