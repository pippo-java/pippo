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

/**
 * @author Decebal Suiu
 */
public class ControllerApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(ControllerApplication.class);

    private ControllerRouteFactory controllerRouteFactory;

    public ControllerApplication() {
        super();
    }

    public ControllerApplication(PippoSettings settings) {
        super(settings);
    }

    public ControllerRouteFactory getControllerRouteFactory() {
        if (controllerRouteFactory == null) {
            controllerRouteFactory = new DefaultControllerRouteFactory().setContentTypeEngines(getContentTypeEngines());
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
