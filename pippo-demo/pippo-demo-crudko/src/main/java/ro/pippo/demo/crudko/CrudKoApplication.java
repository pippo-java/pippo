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
package ro.pippo.demo.crudko;

import ro.pippo.controller.ControllerApplication;
import ro.pippo.core.RedirectHandler;
import ro.pippo.core.TemplateHandler;
import ro.pippo.core.route.CSRFHandler;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;

/**
 * @author Decebal Suiu
 */
public class CrudKoApplication extends ControllerApplication {

    private ContactService contactService;

    public ContactService getContactService() {
        return contactService;
    }

    @Override
    protected void onInit() {
        contactService = new InMemoryContactService();

        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        /*
         * Register a CSRF token generator and validator.
         * This creates a session for all matching requests.
         */
        ALL("/contacts", new CSRFHandler()).named("CSRF handler");

        /*
         * Redirect "/" to "/contacts"
         */
        GET("/", new RedirectHandler("/contacts"));

        /*
         * Server-generated HTML pages
         */
        GET("/contacts", new TemplateHandler("contacts"));
        GET("/contact", new TemplateHandler("contact"));

        /*
         * RESTful API
         */
        GET("/api/contacts", CrudKoApiController.class, "getContacts");
        GET("/api/contact/{id}", CrudKoApiController.class, "getContact");
        DELETE("/api/contact/{id}", CrudKoApiController.class, "deleteContact");
        POST("/api/contact", CrudKoApiController.class, "saveContact");
    }

}
