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
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.WebjarsResourceHandler;
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
    public void init() {
        super.init();

        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        contactService = new InMemoryContactService();

        // audit filter
        ALL("/.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                log.info("Request for {} '{}'", routeContext.getRequest().getMethod(), routeContext.getRequest().getUri());
                routeContext.next();
            }

        });

        // authentication filter
        GET("/contact.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                if (routeContext.getRequest().getSession().get("username") == null) {
                    routeContext.getRequest().getSession().put("originalDestination", routeContext.getRequest().getContextUriWithQuery());
                    routeContext.getResponse().redirectToContextPath("/login");
                } else {
                    routeContext.next();
                }
            }

        });

        GET("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.getResponse().render("login");
            }

        });

        POST("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String username = routeContext.getRequest().getParameter("username").toString();
                String password = routeContext.getRequest().getParameter("password").toString();
                if (authenticate(username, password)) {
                    routeContext.getRequest().getSession().put("username", username);
                    String originalDestination = routeContext.getRequest().getSession().remove("originalDestination");
                    routeContext.getResponse().redirectToContextPath(originalDestination != null ? originalDestination : "/contacts");
                } else {
                    routeContext.getRequest().getSession().getFlash().error("Authentication failed");
                    routeContext.getResponse().redirectToContextPath("/login");
                }
            }

            private boolean authenticate(String username, String password) {
                return !username.isEmpty() && !password.isEmpty();
            }

        });

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.getResponse().redirectToContextPath("/contacts");
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
                routeContext.getResponse().bind("contacts", contactService.getContacts()).render("contacts");
            }

        });

        GET("/contact/{id}", new RouteHandler() {

            @Metered("getContact")
            @Override
            public void handle(RouteContext routeContext) {
                int id = routeContext.getRequest().getParameter("id").toInt(0);
                String action = routeContext.getRequest().getParameter("action").toString("new");
                if ("delete".equals(action)) {
                    contactService.delete(id);
                    routeContext.getResponse().redirectToContextPath("/contacts");

                    return;
                }

                Contact contact = (id > 0) ? contactService.getContact(id) : new Contact();
                Map<String, Object> model = new HashMap<>();
                model.put("contact", contact);
                StringBuilder editAction = new StringBuilder();
                editAction.append("/contact?action=save");
                if (id > 0) {
                    editAction.append("&id=");
                    editAction.append(id);
                }
                model.put("editAction", getRouter().uriFor(editAction.toString()));
                model.put("backAction", getRouter().uriFor("/contacts"));
                routeContext.getResponse().render("contact", model);
            }

        });

        POST("/contact", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String action = routeContext.getRequest().getParameter("action").toString();
                if ("save".equals(action)) {
                    Contact contact = routeContext.getRequest().createEntityFromParameters(Contact.class);
                    contactService.save(contact);
                    routeContext.getResponse().redirectToContextPath("/contacts");
                }
            }

        });
    }

}
