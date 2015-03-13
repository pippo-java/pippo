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

import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.util.ServiceLocator;

/**
 * @author Decebal Suiu
 */
public class ControllerApplication extends Application {

    private ControllerHandlerFactory controllerHandlerFactory;
    private ControllerInstantiationListenerList controllerInstantiationListeners;
    private ControllerInitializationListenerList controllerInitializationListeners;
    private ControllerInvokeListenerList controllerInvokeListeners;

    public ControllerApplication() {
        super();
    }

    public ControllerApplication(PippoSettings settings) {
        super(settings);
    }

    public Route GET(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.GET, controllerClass, methodName);
    }

    public Route POST(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.POST, controllerClass, methodName);
    }

    public Route DELETE(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.DELETE, controllerClass, methodName);
    }

    public Route HEAD(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.HEAD, controllerClass, methodName);
    }

    public Route PUT(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.PUT, controllerClass, methodName);
    }

    public Route PATCH(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.PATCH, controllerClass, methodName);
    }

    public Route ALL(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return addRoute(uriPattern, HttpConstants.Method.ALL, controllerClass, methodName);
    }

    public Route addRoute(String uriPattern, String requestMethod, Class<? extends Controller> controllerClass, String methodName) {
        RouteHandler routeHandler = getControllerHandlerFactory().createHandler(controllerClass, methodName);
        Route route = addRoute(uriPattern, requestMethod, routeHandler);

        return route;
    }

    public ControllerHandlerFactory getControllerHandlerFactory() {
        if (controllerHandlerFactory == null) {
            ControllerHandlerFactory factory = ServiceLocator.locate(ControllerHandlerFactory.class);
            if (factory == null) {
                factory = new DefaultControllerHandlerFactory();
            }
            factory.init(this);
            controllerHandlerFactory = factory;
        }

        return controllerHandlerFactory;
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
