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
package ro.fortsoft.pippo.demo.spring;

import ro.fortsoft.pippo.core.Pippo;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;
import ro.fortsoft.pippo.spring.SpringControllerInjector;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This demo shows how to use Spring Framework to declare a bean and how to use
 * <code>{@link SpringControllerInjector}</code> to inject that bean in a Controller.
 *
 * @author Decebal Suiu
 */
public class SpringDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();

        // create spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // registering SpringControllerInjector
        pippo.getApplication().getControllerInstantiationListeners().add(new SpringControllerInjector(applicationContext));

        pippo.getApplication().GET(new WebjarsResourceHandler());
        pippo.getApplication().GET(new PublicResourceHandler());

        // add controller
        pippo.getApplication().GET("/", ContactsController.class, "index");

        pippo.start();
    }

}
