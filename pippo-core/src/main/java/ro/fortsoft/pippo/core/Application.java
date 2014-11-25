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
package ro.fortsoft.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.controller.ControllerInitializationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInstantiationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInvokeListenerList;
import ro.fortsoft.pippo.core.route.DefaultRouteMatcher;
import ro.fortsoft.pippo.core.route.DefaultRouteNotFoundHandler;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteMatcher;
import ro.fortsoft.pippo.core.route.RouteNotFoundHandler;
import ro.fortsoft.pippo.core.util.ServiceLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private PippoSettings pippoSettings;
    private TemplateEngine templateEngine;
    private RouteMatcher routeMatcher;
    private ExceptionHandler exceptionHandler;
    private RouteNotFoundHandler routeNotFoundHandler;

    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private long maximumUploadSize = -1L;

    private ControllerInstantiationListenerList controllerInstantiationListeners;
    private ControllerInitializationListenerList controllerInitializationListeners;
    private ControllerInvokeListenerList controllerInvokeListeners;

    private Map<String, Object> locals;

    /**
     * Get Application for current thread.
     *
     * @return The current thread's Application
     */
    public static Application get() {
        Application application = ThreadContext.getApplication();
        if (application == null) {
            throw new PippoRuntimeException("There is no application attached to current thread " +
                    Thread.currentThread().getName());
        }

        return application;
    }

    public Application() {
        this(new PippoSettings());
    }

    public Application(PippoSettings settings) {
        this.pippoSettings = new PippoSettings();
    }

    public void init() {
    }

    public void destroy() {
    }

    /**
     * The runtime mode. Must currently be either DEV, TEST, or PROD.
     */
    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.getCurrent();
    }

    public PippoSettings getPippoSettings() {
        return pippoSettings;
    }

    public String getApplicationName() {
        return pippoSettings.getString(PippoConstant.SETTING_APPLICATION_NAME, "");
    }

    public String getApplicationVersion() {
        return pippoSettings.getString(PippoConstant.SETTING_APPLICATION_VERSION, "");
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
        if (routeMatcher == null) {
            routeMatcher = new DefaultRouteMatcher();
        }

        return routeMatcher;
    }

    public void setRouteMatcher(RouteMatcher routeMatcher) {
        this.routeMatcher = routeMatcher;
    }

    public void GET(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.GET, routeHandler);
    }

    public void GET(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.GET, new ControllerHandler(controllerClass, methodName));
    }

    public void POST(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.POST, routeHandler);
    }

    public void POST(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.POST, new ControllerHandler(controllerClass, methodName));
    }

    public void DELETE(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, routeHandler);
    }

    public void DELETE(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.DELETE, new ControllerHandler(controllerClass, methodName));
    }

    public void HEAD(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, routeHandler);
    }

    public void HEAD(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.HEAD, new ControllerHandler(controllerClass, methodName));
    }

    public void PUT(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PUT, routeHandler);
    }

    public void PUT(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PUT, new ControllerHandler(controllerClass, methodName));
    }

    public void PATCH(String urlPattern, RouteHandler routeHandler) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, routeHandler);
    }

    public void PATCH(String urlPattern, Class<? extends Controller> controllerClass, String methodName) {
        addRoute(urlPattern, HttpConstants.Method.PATCH, new ControllerHandler(controllerClass, methodName));
    }

    public void addRoute(String urlPattern, String requestMethod, RouteHandler routeHandler) {
        Route route = new Route(urlPattern, requestMethod, routeHandler);
        try {
            getRouteMatcher().addRoute(route);
        } catch (Exception e) {
            log.error("Cannot add route '{}'", route, e);
        }
    }

    public ExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new DefaultExceptionHandler();
        }

        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public RouteNotFoundHandler getRouteNotFoundHandler() {
        if (routeNotFoundHandler == null) {
            routeNotFoundHandler = new DefaultRouteNotFoundHandler(this);
        }

        return routeNotFoundHandler;
    }

    public void setRouteNotFoundHandler(RouteNotFoundHandler routeNotFoundHandler) {
        this.routeNotFoundHandler = routeNotFoundHandler;
    }

    /**
     * The directory location where files will be stored.
     *
     * @return
     */
    public String getUploadLocation() {
        return uploadLocation;
    }

    public void setUploadLocation(String uploadLocation) {
        this.uploadLocation = uploadLocation;
    }

    /**
     * Gets the maximum size allowed for uploaded files.
     *
     * @return
     */
    public long getMaximumUploadSize() {
        return maximumUploadSize;
    }

    public void setMaximumUploadSize(long maximumUploadSize) {
        this.maximumUploadSize = maximumUploadSize;
    }

    public ControllerInstantiationListenerList getControllerInstantiationListeners() {
        if (controllerInstantiationListeners == null) {
            controllerInstantiationListeners = new ControllerInstantiationListenerList();
        }

        return controllerInstantiationListeners;
    }

    public ControllerInitializationListenerList getControllerInitializationListeners() {
        if (controllerInitializationListeners == null) {
            controllerInitializationListeners = new ControllerInitializationListenerList();
        }

        return controllerInitializationListeners;
    }

    public ControllerInvokeListenerList getControllerInvokeListeners() {
        if (controllerInvokeListeners == null) {
            controllerInvokeListeners = new ControllerInvokeListenerList();
        }

        return controllerInvokeListeners;
    }

    public Map<String, Object> getLocals() {
        if (locals == null) {
            locals = new HashMap<>();
        }

        return locals;
    }

    public String urlFor(Class<? extends Controller> controllerClass, String methodName, Map<String, Object> parameters) {
        Route route = getRoute(controllerClass, methodName);

        return (route != null) ? urlFor(route.getUrlPattern(), parameters) : null;
    }

    public String urlFor(String urlPattern, Map<String, Object> parameters) {
        return getRouteMatcher().urlFor(urlPattern, parameters);
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

    @Override
    public String toString() {
        String toString = (getApplicationName() + " " + getApplicationVersion()).trim();
        if (toString.isEmpty()) {
            super.toString();
        }
        return toString;
    }

}
