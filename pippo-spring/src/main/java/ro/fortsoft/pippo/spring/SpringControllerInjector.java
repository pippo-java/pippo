/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.spring;

import org.springframework.context.ApplicationContext;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.controller.ControllerInstantiationListener;
import ro.fortsoft.pippo.ioc.FieldValueProvider;
import ro.fortsoft.pippo.ioc.Injector;

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
