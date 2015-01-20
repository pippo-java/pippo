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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.RedirectHandler;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.TemplateHandler;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;
import ro.fortsoft.pippo.demo.common.ContactService;
import ro.fortsoft.pippo.demo.common.InMemoryContactService;

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
                response.render("login", model);
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
