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

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Pippo;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.demo.crud.InMemoryContactService;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This demo shows how to use Spring Framework to declare your Application as bean together with other beans and how
 * to retrieve a declared bean in a Controller.
 *
 * @author Decebal Suiu
 */
public class SpringDemo2 {

    public static void main(String[] args) {

        // create spring application context
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SpringConfiguration2.class);
        Application application = (Application) applicationContext.getBean("application");

        Pippo pippo = new Pippo(application);
        pippo.start();
    }

    @Configuration
    public static class SpringConfiguration2 {

        @Bean
        public ContactService contactService() {
            return new InMemoryContactService();
        }

        @Bean
        public Application application() {
            return new SpringApplication2();
        }

    }

    public static class SpringApplication2 extends Application {

        @Inject
        private ContactService contactService;

        @Override
        public void init() {
            super.init();

            GET(new WebjarsResourceHandler());
            GET(new PublicResourceHandler());
            GET("/", ContactsController2.class, "index");
        }

        public ContactService getContactService() {
            return contactService;
        }

        public static SpringApplication2 get() {
            return (SpringApplication2) Application.get();
        }

    }

    public static class ContactsController2 extends Controller {

        public void index() {
            ContactService contactService = SpringApplication2.get().getContactService();
            getResponse().bind("contacts", contactService.getContacts());
            getResponse().render("crud/contacts");
        }

    }

}