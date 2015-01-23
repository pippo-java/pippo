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
package ro.pippo.spring;

import org.springframework.context.ApplicationContext;
import ro.pippo.core.controller.Controller;
import ro.pippo.core.controller.ControllerInstantiationListener;
import ro.pippo.ioc.FieldValueProvider;
import ro.pippo.ioc.Injector;

/**
 * @author Decebal Suiu
 */
public class SpringControllerInjector extends Injector implements ControllerInstantiationListener {

    private FieldValueProvider fieldValueProvider;

    public SpringControllerInjector(ApplicationContext applicationContext) {
        fieldValueProvider = new AnnotationFieldValueProvider(applicationContext);
    }

    @Override
    public void inject(Object object) {
        inject(object, fieldValueProvider);
    }

    @Override
    public void onInstantiation(Controller controller) {
        inject(controller);
    }

}
