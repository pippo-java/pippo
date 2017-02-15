/*
 * Copyright (C) 2016 the original author or authors.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link ControllerFactory} that always returns a specific instance.
 * This class can use other {@code ControllerFactory} to create the controller's instance if
 * this is not already in cache.
 * Optional you can specify the controller classes for which you want singletons.
 *
 * @author Decebal Suiu
 */
public class SingletonControllerFactory implements ControllerFactory {

    private static final Logger log = LoggerFactory.getLogger(SingletonControllerFactory.class);

    private final ControllerFactory decoratedFactory;
    private final List<String> controllerClassNames;

    private final Map<String, Controller> cache;

    public SingletonControllerFactory() {
        this (new DefaultControllerFactory());
    }

    public SingletonControllerFactory(String... controllerClassNames) {
        this(new DefaultControllerFactory(), controllerClassNames);
    }

    public SingletonControllerFactory(ControllerFactory decoratedFactory, String... controllerClassNames) {
        this.decoratedFactory = decoratedFactory;
        this.controllerClassNames = Arrays.asList(controllerClassNames);

        cache = new ConcurrentHashMap<>(); // simple cache implementation
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Controller> T createController(Class<T> controllerClass) {
        String controllerClassName = controllerClass.getName();
        T controller = (T) cache.get(controllerClassName);
        if (controller == null) {
            log.debug("Create instance of '{}'", controllerClassName);
            controller = decoratedFactory.createController(controllerClass);
            if (controllerClassNames.isEmpty() || controllerClassNames.contains(controllerClassName)) {
                cache.put(controllerClassName, controller);
            }
        }

        return controller;
    }

}
