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
package ro.pippo.demo.crudng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.RedirectHandler;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.TemplateHandler;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteHandlerChain;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author James Moger
 */
public class CrudNgApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(CrudNgApplication.class);

    private ContactService contactService;

    public ContactService getContactService() {
        return contactService;
    }

    @Override
    public void init() {
        super.init();

        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        contactService = new InMemoryContactService();

        /*
         *  audit filter
         */
        ALL("/.*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                log.info("Request for {} '{}'", request.getMethod(), request.getUri());
                chain.next();
            }

        });

        /*
         *  authentication filter
         */
        GET("/contact.*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                if (request.getSession().get("username") == null) {
                    request.getSession().put("originalDestination", request.getContextUriWithQuery());
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
                String error = request.getSession().get("error");
                request.getSession().remove("error");
                if (error != null) {
                    model.put("error", error);
                }
                response.render("login", model);
            }

        });

        POST("/login", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                String username = request.getParameter("username").toString();
                String password = request.getParameter("password").toString();
                if (authenticate(username, password)) {
                    request.getSession().put("username", username);
                    String originalDestination = (String) request.getSession().get("originalDestination");
                    response.redirectToContextPath(originalDestination != null ? originalDestination : "/contacts");
                } else {
                    request.getSession().put("error", "Authentication failed");
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
        GET("/", new RedirectHandler("/contacts"));

        /*
         * Server-generated HTML pages
         */
        GET("/contacts", new TemplateHandler("contacts"));
        GET("/contact/.*", new TemplateHandler("contact"));

        /*
         * RESTful API
         */
        GET("/api/contacts", CrudNgApiController.class, "getContacts");
        GET("/api/contact/{id}", CrudNgApiController.class, "getContact");
        DELETE("/api/contact/{id}", CrudNgApiController.class, "deleteContact");
        POST("/api/contact", CrudNgApiController.class, "saveContact");
    }

}
