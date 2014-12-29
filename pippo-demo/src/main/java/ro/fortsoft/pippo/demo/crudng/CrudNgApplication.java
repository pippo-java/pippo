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
package ro.fortsoft.pippo.demo.crudng;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.demo.crud.Contact;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.demo.crud.InMemoryContactService;
import ro.fortsoft.pippo.metrics.Metered;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Moger
 */
public class CrudNgApplication extends Application {

    private final Logger log = LoggerFactory.getLogger(CrudNgApplication.class);

    private ContactService contactService;

    @Override
    public void init() {
        super.init();

        contactService = new InMemoryContactService();

        // audit filter
        GET("/.*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                log.info("request.getUri() = {}", request.getUri());
                chain.next();
            }

        });

        /*
         *  authentication filter
         */
        GET("/contact.*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                if (request.getSession().getAttribute("username") == null) {
                    request.getSession().setAttribute("originalDestination", request.getContextUriWithQuery());
                    response.redirectToContextPath("/login");
                } else {
                    chain.next();
                }
            }

        });


        GET("/login", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Map<String, Object> model = new HashMap<>();
                String error = (String) request.getSession().getAttribute("error");
                request.getSession().removeAttribute("error");
                if (error != null) {
                    model.put("error", error);
                }
                response.render("crudng/login", model);
            }

        });

        POST("/login", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                String username = request.getParameter("username").toString();
                String password = request.getParameter("password").toString();
                if (authenticate(username, password)) {
                    request.getSession().setAttribute("username", username);
                    String originalDestination = (String) request.getSession().getAttribute("originalDestination");
                    response.redirectToContextPath(originalDestination != null ? originalDestination : "/contacts");
                } else {
                    request.getSession().setAttribute("error", "Authentication failed");
                    response.redirectToContextPath("/login");
                }
            }

            private boolean authenticate(String username, String password) {
                return !username.isEmpty() && !password.isEmpty();
            }

        });

        /*
         * root redirect
         */
        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.redirectToContextPath("/contacts");
            }

        });

        /*
         * Server generated pages
         */
        GET("/contacts", new RouteHandler() {

            @Metered("getContactsList")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.render("crudng/contacts");
            }

        });

        GET("/contact/{id}", new RouteHandler() {

            @Metered("page.editContact")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.render("crudng/contact");
            }

        });


        /*
         * RESTful API methods
         */
        GET("/api/contacts", new RouteHandler() {

            @Metered("api.contacts.get")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.send(contactService.getContacts(), request.getAcceptType());
            }

        });

        GET("/api/contact/{id}", new RouteHandler() {

            @Metered("api.contact.get")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                int id = request.getParameter("id").toInt(0);
                Contact contact = (id > 0) ? contactService.getContact(id) : new Contact();
                response.send(contact, request.getAcceptType());
            }

        });

        DELETE("/api/contact/{id}", new RouteHandler() {

            @Metered("api.contact.delete")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                int id = request.getParameter("id").toInt(0);
                if (id <= 0) {
                    response.sendBadRequest();
                } else {
                    Contact contact = contactService.getContact(id);
                    if (contact == null) {
                        response.sendBadRequest();
                    } else {
                        contactService.delete(id);
                        log.info("Deleted contact #{} '{}'", contact.getId(), contact.getName());
                        response.sendOk();
                    }
                }
            }

        });

        POST("/api/contact", new RouteHandler() {

            @Metered("api.contact.post")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                // create the contact from the request body
                Contact contact = request.createEntityFromBody(Contact.class);
                contactService.save(contact);
                response.sendOk();
            }

        });

    }

}
