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
import ro.pippo.core.route.RouteHandler;

/**
 * This factory constructs the default controller handler.
 *
 * @author James Moger
 */
public class DefaultControllerRouteHandlerFactory implements ControllerRouteHandlerFactory {

    private ControllerApplication application;

    @Override
    public RouteHandler createHandler(Class<? extends Controller> controllerClass, String methodName) {
        return new DefaultControllerRouteHandler(application, controllerClass, methodName);
    }

    @Override
    public void init(Application application) {
        this.application = (ControllerApplication) application;
    }

    @Override
    public void destroy(Application application) {
    }

}