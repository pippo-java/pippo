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

import ro.pippo.core.ContentTypeEngines;
import ro.pippo.core.route.RouteHandler;

import java.lang.reflect.Method;

/**
 * Default {@link ControllerHandlerFactory} implementation.
 * {@link DefaultControllerFactory} is used if a custom {@link ControllerFactory} is not supplied
 * via {@link DefaultControllerHandlerFactory::setControllerFactory}.
 *
 * @author Decebal Suiu
 */
public class DefaultControllerHandlerFactory implements ControllerHandlerFactory {

    private ContentTypeEngines contentTypeEngines;
    private ControllerFactory controllerFactory;

    @Override
    public RouteHandler<?> createRouteHandler(Method controllerMethod) {
        return new ControllerHandler(getContentTypeEngines(), controllerMethod).setControllerFactory(getControllerFactory());
    }

    public ContentTypeEngines getContentTypeEngines() {
        if (contentTypeEngines == null) {
            return new ContentTypeEngines();
        }

        return contentTypeEngines;
    }

    public DefaultControllerHandlerFactory setContentTypeEngines(ContentTypeEngines contentTypeEngines) {
        this.contentTypeEngines = contentTypeEngines;

        return this;
    }

    public ControllerFactory getControllerFactory() {
        if (controllerFactory == null) {
            controllerFactory = new DefaultControllerFactory();
        }

        return controllerFactory;
    }

    public DefaultControllerHandlerFactory setControllerFactory(ControllerFactory controllerFactory) {
        this.controllerFactory = controllerFactory;

        return this;
    }

}
