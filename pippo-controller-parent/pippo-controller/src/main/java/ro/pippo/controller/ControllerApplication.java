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
import ro.pippo.controller.extractor.MethodParameterExtractor;
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

    @Inject
    private Optional<ControllerFactory> controllerFactory = Optional.empty();

    @Inject
    private Optional<List<MethodParameterExtractor>> extractors = Optional.empty();

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
        if (!controllerInvokeListeners.isPresent()) {
            controllerInvokeListeners = Optional.of(new ControllerInvokeListenerList());
        }

        return controllerInvokeListeners.get();
    }

    public ControllerFactory getControllerFactory() {
        if (!controllerFactory.isPresent()) {
            controllerFactory = Optional.of(new DefaultControllerFactory());
        }

        return controllerFactory.get();
    }

    public ControllerApplication setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = Optional.of(controllerFactory);
        log.debug("Controller factory is '{}'", controllerFactory.getClass().getName());

        return this;
    }

    public ControllerApplication addExtractors(MethodParameterExtractor... extractors) {
        getExtractors().addAll(Arrays.asList(extractors));

        return this;
    }

    public List<MethodParameterExtractor> getExtractors() {
        if (!extractors.isPresent()) {
            extractors = Optional.of(ServiceLocator.locateAll(MethodParameterExtractor.class));
        }

        return extractors.get();
    }

    public ControllerApplication addControllers(String... packageNames) {
        ControllerRegistry controllerRegistry = new ControllerRegistry(this);
        controllerRegistry.register(packageNames);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Package... packages) {
        ControllerRegistry controllerRegistry = new ControllerRegistry(this);
        controllerRegistry.register(packages);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Class<? extends Controller>... controllerClasses) {
        ControllerRegistry controllerRegistry = new ControllerRegistry(this);
        controllerRegistry.register(controllerClasses);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

    public ControllerApplication addControllers(Controller... controllers) {
        ControllerRegistry controllerRegistry = new ControllerRegistry(this);
        controllerRegistry.register(controllers);
        controllerRegistry.getRoutes().forEach(this::addRoute);

        return this;
    }

}
