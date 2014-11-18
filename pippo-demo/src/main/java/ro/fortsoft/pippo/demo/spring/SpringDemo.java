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
package ro.fortsoft.pippo.demo.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ro.fortsoft.pippo.core.Pippo;
import ro.fortsoft.pippo.spring.SpringControllerInjector;

/**
 * This demo shows how to use Spring Framework to declare a bean and how to use
 * <code>{@link SpringControllerInjector}</code> to inject that bean in a Controller.
 *
 * @author Decebal Suiu
 */
public class SpringDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
        pippo.getServer().getSettings().staticFilesLocation("/public");

        // create spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // registering SpringControllerInjector
        pippo.getApplication().getControllerInstantiationListeners().add(new SpringControllerInjector(applicationContext));

        // add controller
        pippo.getApplication().GET("/", ContactsController.class, "index");

        pippo.start();
    }

}
