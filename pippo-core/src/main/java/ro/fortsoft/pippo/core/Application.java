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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private TemplateEngine templateEngine;
    private RouteMatcher routeMatcher;
    private ExceptionHandler exceptionHandler;
    private RouteNotFoundHandler routeNotFoundHandler;

    /*
     * Cache value for the runtime mode. No need to re-read it because it wont change at runtime.
     */
    private RuntimeMode runtimeMode;

    public void init() {
        routeMatcher = new DefaultRouteMatcher();
        exceptionHandler = new DefaultExceptionHandler();
        routeNotFoundHandler = new DefaultRouteNotFoundHandler(this);
    }

    public void destroy() {
    }

    /**
     * The runtime mode. Must currently be either DEV or PROD.
     */
    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.getCurrent();
    }

    public TemplateEngine getTemplateEngine() {
        if (templateEngine == null) {
            templateEngine = ServiceLocator.locate(TemplateEngine.class);
        }

        return templateEngine;
    }

    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public RouteMatcher getRouteMatcher() {
        return routeMatcher;
    }

    public void setRouteMatcher(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    public void GET(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.GET, routeHandler);
    }

    public void POST(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.POST, routeHandler);
    }

    public void DELETE(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, routeHandler);
    }

    public void HEAD(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, routeHandler);
    }

    public void PUT(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PUT, routeHandler);
    }

    public void addRoute(String urlPattern, String requestMethod, RouteHandler routeHandler) {
        Route route = new Route(urlPattern, requestMethod, routeHandler);
        try {
            routeMatcher.addRoute(route);
        } catch (Exception e) {
            log.error("Cannot add route '{}'", route, e);
        }
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public RouteNotFoundHandler getRouteNotFoundHandler() {
        return routeNotFoundHandler;
    }

    public void setRouteNotFoundHandler(RouteNotFoundHandler routeNotFoundHandler) {
        this.routeNotFoundHandler = routeNotFoundHandler;
    }

}
