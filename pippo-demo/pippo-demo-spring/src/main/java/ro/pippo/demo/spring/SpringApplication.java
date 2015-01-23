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
package ro.pippo.demo.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ro.pippo.core.Application;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.spring.SpringControllerInjector;

/**
 * @author Decebal Suiu
 */
public class SpringApplication extends Application {

    @Override
    public void init() {
        super.init();

        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        // create spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        // registering SpringControllerInjector
        getControllerInstantiationListeners().add(new SpringControllerInjector(applicationContext));

        // add controller
        GET("/", ContactsController.class, "index");
    }

}
