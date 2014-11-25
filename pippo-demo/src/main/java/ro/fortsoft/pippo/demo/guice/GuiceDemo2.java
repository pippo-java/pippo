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
package ro.fortsoft.pippo.demo.guice;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Pippo;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.guice.GuiceControllerInjector;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This demo shows how to use Guice with Pippo.
 *
 * @author James Moger
 */
public class GuiceDemo2 {

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new GuiceModule());
        Application application = injector.getInstance(GuiceApplication2.class);
        application.getControllerInstantiationListeners().add(new GuiceControllerInjector(injector));

        Pippo pippo = new Pippo(application);
        pippo.getServer().getSettings().staticFilesLocation("/public");

        pippo.start();
    }

    public static class GuiceApplication2 extends Application {

        @Inject
        private ContactService contactService;

        @Override
        public void init() {
            super.init();

            GET("/", ContactsController2.class, "index");
        }

        public ContactService getContactService() {
            return contactService;
        }

    }

    public static class ContactsController2 extends Controller {

        @Inject
        private ContactService contactService;

        public void index() {
            getResponse().getLocals().put("contacts", contactService.getContacts());
            getResponse().render("crud/contacts.ftl");
        }

    }

}