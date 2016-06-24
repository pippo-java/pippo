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
package ro.pippo.guice;

import com.google.inject.Injector;
import ro.pippo.controller.Controller;
import ro.pippo.controller.ControllerFactory;

/**
 * @author Decebal Suiu
 */
public class GuiceControllerFactory implements ControllerFactory {

    private Injector injector;

    public GuiceControllerFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    public <T extends Controller> T createController(Class<T> controllerClass) {
        return injector.getInstance(controllerClass);
    }

}
