/*
 * Copyright (C) 2014-present the original author or authors.
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
import ro.pippo.core.PippoSettings;
import ro.pippo.core.util.ServiceLocator;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Decebal Suiu
 */
public class ControllerApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(ControllerApplication.class);

    @Inject
    private Optional<ControllerInstantiationListenerList> controllerInstantiationListeners = Optional.empty();

    @Inject
    private Optional<ControllerInitializationListenerList> controllerInitializationListeners = Optional.empty();

    @Inject
    private Optional<ControllerInvokeListenerList> controllerInvokeListeners = Optional.empty();

    private ControllerRouteFactory controllerRouteFactory;

    public ControllerApplication() {
        super();
    }

    @Inject
    public ControllerApplication(PippoSettings settings) {
        super(settings);
    }

    public ControllerInstantiationListenerList getControllerInstantiationListeners() {
        if (!controllerInstantiationListeners.isPresent()) {
            controllerInstantiationListeners = Optional.of(new ControllerInstantiationListenerList());
        }

        return controllerInstantiationListeners.get();
    }

    public ControllerInitializationListenerList getControllerInitializationListeners() {
        if (!controllerInitializationListeners.isPresent()) {
            controllerInitializationListeners = Optional.of(new ControllerInitializationListenerList());
        }

        return controllerInitializationListeners.get();
    }

    public ControllerInvokeListenerList getControllerInvokeListeners() {
        if (controllerInvokeListeners == null) {
            controllerInvokeListeners = new ControllerInvokeListenerList();
        }

        return controllerInvokeListeners;
    }

    public ControllerRouteFactory getControllerRouteFactory() {
        if (controllerRouteFactory == null) {
            ControllerHandlerFactory controllerHandlerFactory = new DefaultControllerHandlerFactory()
                .setContentTypeEngines(getContentTypeEngines());
            controllerRouteFactory = new DefaultControllerRouteFactory()
                .setControllerHandlerFactory(controllerHandlerFactory);
        }

        return controllerRouteFactory;
    }

    public ControllerApplication setControllerRouteFactory(ControllerRouteFactory controllerRouteFactory) {
        this.controllerRouteFactory = controllerRouteFactory;
        log.debug("Controller route factory is '{}'", controllerRouteFactory.getClass().getName());

        return this;
    }

    public ControllerApplication addControllers(String... packageNames) {
        ControllerRegistry controllerRegistry = new ControllerRegistry().setControllerRouteFactory(getControllerRouteFactory());
        controllerRegistry.register(packageNames);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Package... packages) {
        ControllerRegistry controllerRegistry = new ControllerRegistry().setControllerRouteFactory(getControllerRouteFactory());
        controllerRegistry.register(packages);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Class<? extends Controller>... controllerClasses) {
        ControllerRegistry controllerRegistry = new ControllerRegistry().setControllerRouteFactory(getControllerRouteFactory());
        controllerRegistry.register(controllerClasses);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Controller... controllers) {
        ControllerRegistry controllerRegistry = new ControllerRegistry().setControllerRouteFactory(getControllerRouteFactory());
        controllerRegistry.register(controllers);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    /**
     * See {@link Application#get()}.
     */
    public static ControllerApplication get() {
        return (ControllerApplication) Application.get();
    }

}
