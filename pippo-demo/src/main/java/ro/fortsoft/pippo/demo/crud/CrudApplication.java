/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.demo.crud;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class CrudApplication extends Application {

    private ContactService contactService;

    @Override
    public void init() {
        super.init();

        /*
        FreemarkerTemplateEngine templateEngine = new FreemarkerTemplateEngine();
        try {
            templateEngine.getConfiguration().setDirectoryForTemplateLoading(new File("src/main/resources/templates/"));
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }
        setTemplateEngine(templateEngine);
        */

        contactService = new InMemoryContactService();

        // audit filter
        GET("/*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                System.out.println("request.getUri() = " + request.getUri());
                chain.next();
            }

        });

        // authentication filter
        GET("/contact*", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                if (request.getSession().getAttribute("username") == null) {
                    request.getSession().setAttribute("originalDestination", request.getUri());
                    response.redirect("/login");
                } else {
                    chain.next();
                }
            }

        });

        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.redirect("/contacts");
            }

        });

        GET("/contacts", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                /*
                // variant 1
                Map<String, Object> model = new HashMap<>();
                model.put("contacts", contactService.getContacts());
                response.render("crud/contacts.ftl", model);
                */

                // variant 2
                response.getLocals().put("contacts", contactService.getContacts());
                response.render("crud/contacts.ftl");
            }

        });

        GET("/contact/:id", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                int id = request.getParameter("id").toInt(0);
                String action = request.getParameter("action").toString("new");
                if ("delete".equals(action)) {
                    contactService.delete(id);
                    response.redirect("/contacts");

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
                model.put("editAction", editAction);
                model.put("backAction", "/contacts");
                response.render("crud/contact.ftl", model);
            }

        });

        POST("/contact", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                String action = request.getParameter("action").toString();
                if ("save".equals(action)) {
                    Contact contact = request.getEntityFromParameters(Contact.class);
                    contactService.save(contact);
                    response.redirect("/contacts");
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
                response.render("crud/login.ftl", model);
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
                    response.redirect(originalDestination != null ? originalDestination : "/contacts");
                } else {
                    request.getSession().setAttribute("error", "Authentication failed");
                    response.redirect("/login");
                }
            }

            private boolean authenticate(String username, String password) {
                return !username.isEmpty() && !password.isEmpty();
            }

        });

    }

}
