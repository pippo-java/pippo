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
package ro.pippo.demo.crud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.route.CSRFHandler;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.demo.common.Contact;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;
import ro.pippo.metrics.Metered;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class CrudApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(CrudApplication.class);

    private ContactService contactService;

    @Override
    protected void onInit() {
        contactService = new InMemoryContactService();

        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        // audit filter
        ALL("/.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                log.info("Request for {} '{}'", routeContext.getRequestMethod(), routeContext.getRequestUri());
                routeContext.next();
            }

        });

        /*
         * Register a CSRF token generator and validator.
         * This creates a session for all matching requests.
         */
        ALL("/contact.*", new CSRFHandler()).named("CSRF handler");

        // authentication filter
        GET("/contact.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                if (routeContext.getSession("username") == null) {
                    routeContext.setSession("originalDestination", routeContext.getRequest().getApplicationUriWithQuery());
                    routeContext.redirect("/login");
                } else {
                    routeContext.next();
                }
            }

        });

        GET("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.render("login");
            }

        });

        POST("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String username = routeContext.getParameter("username").toString();
                String password = routeContext.getParameter("password").toString();
                if (authenticate(username, password)) {
                    String originalDestination = routeContext.removeSession("originalDestination");
                    routeContext.resetSession();

                    routeContext.setSession("username", username);
                    routeContext.redirect(originalDestination != null ? originalDestination : "/contacts");
                } else {
                    routeContext.flashError("Authentication failed");
                    routeContext.redirect("/login");
                }
            }

            private boolean authenticate(String username, String password) {
                return !username.isEmpty() && !password.isEmpty();
            }

        });

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.redirect("/contacts");
            }

        });

        GET("/contacts", new RouteHandler() {

            @Metered("getContactsList")
            @Override
            public void handle(RouteContext routeContext) {
                /*
                // variant 1
                Map<String, Object> model = new HashMap<>();
                model.put("contacts", contactService.getContacts());
                response.render("contacts", model);
                */

                // variant 2
                routeContext.setLocal("contacts", contactService.getContacts());
                routeContext.render("contacts");
            }

        });

        GET("/contact/{id}", new RouteHandler() {

            @Metered("getContact")
            @Override
            public void handle(RouteContext routeContext) {
                int id = routeContext.getParameter("id").toInt(0);
                String action = routeContext.getParameter("action").toString("new");
                if ("delete".equals(action)) {
                    contactService.delete(id);
                    routeContext.redirect("/contacts");

                    return;
                }

                Contact contact = (id > 0) ? contactService.getContact(id) : new Contact();
                routeContext.setLocal("contact", contact);
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("action", "save");
                if (id > 0) {
                    parameters.put("id", id);
                }
                routeContext.setLocal("saveUrl", getRouter().uriFor("/contact", parameters));
                routeContext.setLocal("backUrl", getRouter().uriFor("/contacts"));
                routeContext.render("contact");
            }

        });

        POST("/contact", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String action = routeContext.getParameter("action").toString();
                if ("save".equals(action)) {
                    Contact contact = routeContext.createEntityFromParameters(Contact.class);
                    contactService.save(contact);
                    routeContext.redirect("/contacts");
                }
            }

        });
    }

}
