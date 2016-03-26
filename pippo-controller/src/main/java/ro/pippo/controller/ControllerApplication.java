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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.route.Route;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.ServiceLocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class ControllerApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(ControllerApplication.class);

    private ControllerHandlerFactory controllerHandlerFactory;
    private ControllerInstantiationListenerList controllerInstantiationListeners;
    private ControllerInitializationListenerList controllerInitializationListeners;
    private ControllerInvokeListenerList controllerInvokeListeners;

    private ControllerFactory controllerFactory;
    private List<RouteRegistration> routeRegistrations;

    public ControllerApplication() {
        super();

        routeRegistrations = new ArrayList<>();
    }

    public ControllerApplication(PippoSettings settings) {
        super(settings);

        routeRegistrations = new ArrayList<>();
    }

    public Route GET(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return GET(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route POST(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return POST(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route DELETE(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return DELETE(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route HEAD(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return HEAD(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route PUT(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return PUT(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route PATCH(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return PATCH(uriPattern, createRouteHandler(controllerClass, methodName));
    }

    public Route ALL(String uriPattern, Class<? extends Controller> controllerClass, String methodName) {
        return ALL(uriPattern, createRouteHandler(controllerClass, methodName));
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

    @Override
    public ControllerRouter getRouter() {
        if (router == null) {
            router = new DefaultControllerRouter();
        }

        return (ControllerRouter) router;
    }

    @Override
    public void setRouter(Router router) {
        if (!(router instanceof ControllerRouter)) {
            throw new PippoRuntimeException("'router' must be an instance of '{}'", ControllerRouter.class.getName());
        }

        super.setRouter(router);
    }

    public ControllerFactory getControllerFactory() {
        if (controllerFactory == null) {
            controllerFactory = new DefaultControllerFactory();
        }

        return controllerFactory;
    }

    public void setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;
        log.debug("Controller factory is '{}'", controllerFactory.getClass().getName());
    }

    public RouteHandler createRouteHandler(Class<? extends Controller> controllerClass, String methodName) {
        return getControllerHandlerFactory().createHandler(controllerClass, methodName);
    }

    public void addControllers(String... packageNames) {
        ControllerRegistrar registrar = new ControllerRegistrar(this);
        registrar.init(packageNames);
        routeRegistrations.addAll(registrar.getRouteRegistrations());
    }

    public void addControllers(Package... packages) {
        ControllerRegistrar registrar = new ControllerRegistrar(this);
        registrar.init(packages);
        routeRegistrations.addAll(registrar.getRouteRegistrations());
    }

    public void addControllers(Class<? extends Controller>... controllers) {
        ControllerRegistrar registrar = new ControllerRegistrar(this);
        registrar.init(controllers);
        routeRegistrations.addAll(registrar.getRouteRegistrations());
    }

    // TODO
    /**
     * Adds Routes to the Router respecting exclusion rules.
     *
     * Also wraps RouteHandlers with Metrics handlers and sets Route names.
     */
    private void compileRoutes() {
        Iterator<RouteRegistration> iterator = routeRegistrations.iterator();
        while (iterator.hasNext()) {
            RouteRegistration routeRegistration = iterator.next();
            iterator.remove();

            // make some validations/checks

            // add route to router
            RouteHandler routeHandler = routeRegistration.getRouteHandler();
            Route route = new Route(routeRegistration.getRequestMethod(), routeRegistration.getUriPattern(), routeHandler);
            route.setName(routeRegistration.getName());
            if (routeRegistration.isRunAsFinally()) {
                route.runAsFinally();
            }

            router.addRoute(route);
        }
    }

}
