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
import ro.pippo.core.RouteContext;
import ro.pippo.core.TemplateHandler;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteHandlerChain;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;

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
            public void handle(RouteContext routeContext, RouteHandlerChain chain) {
                log.info("Request for {} '{}'", routeContext.getRequest().getMethod(), routeContext.getRequest().getUri());
                chain.next();
            }

        });

        /*
         *  authentication filter
         */
        GET("/contact.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext, RouteHandlerChain chain) {
                if (routeContext.getRequest().getSession().get("username") == null) {
                    routeContext.getRequest().getSession().put("originalDestination", routeContext.getRequest().getContextUriWithQuery());
                    routeContext.getResponse().redirectToContextPath("/login");
                } else {
                    chain.next();
                }
            }

        });

        GET("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext, RouteHandlerChain chain) {
                routeContext.getResponse().render("login");
            }

        });

        POST("/login", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext, RouteHandlerChain chain) {
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
