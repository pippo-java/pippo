/*
 * Copyright (C) 2015 the original author or authors.
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

import ro.pippo.core.PippoRuntimeException;

/**
 * Basic implementation of a controller factory that uses Java reflection to
 * instantiate an object.
 * Create a new controller instance every time a request is done.
 *
 * @author Decebal Suiu
 */
public class DefaultControllerFactory implements ControllerFactory {

    @Override
    public <T extends Controller> T createController(Class<T> controllerClass) {
        try {
            return controllerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new PippoRuntimeException(e, "Could not instantiate '{}'", controllerClass.getName());
        }
    }

}
