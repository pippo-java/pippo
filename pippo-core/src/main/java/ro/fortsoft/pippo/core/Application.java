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
import ro.fortsoft.pippo.core.controller.ControllerInitializationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInstantiationListenerList;
import ro.fortsoft.pippo.core.controller.ControllerInvokeListenerList;

/**
 * @author Decebal Suiu
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private TemplateEngine templateEngine;
    private RouteMatcher routeMatcher;
    private ExceptionHandler exceptionHandler;
    private RouteNotFoundHandler routeNotFoundHandler;

    private String uploadLocation = System.getProperty("java.io.tmpdir");
    private long maximumUploadSize = -1L;

    private ControllerInstantiationListenerList controllerInstantiationListeners;
    private ControllerInitializationListenerList controllerInitializationListeners;
    private ControllerInvokeListenerList controllerInvokeListeners;

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

    public void init() {
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

}
