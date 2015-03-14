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
package ro.pippo.demo.validation;

import ro.pippo.core.Application;
import ro.pippo.core.Flash;
import ro.pippo.core.RequestResponseFactory;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.core.session.SessionManager;
import ro.pippo.core.session.SessionRequestResponseFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class ValidationApplication extends Application {

    @Override
    protected void onInit() {
        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
                System.out.println(routeContext.getRequest().getHttpServletRequest());
                System.out.println(routeContext.getRequest().getSession(false));
                System.out.println("<<<<<<<<<<<<<<<<<<<<<<<");

                Contact contact = new Contact();
                routeContext.setLocal("contact", contact);
                routeContext.render("contact");
            }

        });

        POST("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                Contact contact = routeContext.createEntityFromParameters(Contact.class);

                // check for validation
                Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
                if (violations.isEmpty()) {
                    routeContext.send(contact.toString());
                } else {
                    // makes violations available to template via flash (response.getLocals().get("flash"))
                    Flash flash = routeContext.getResponse().getFlash();
                    for (ConstraintViolation<Contact> violation : violations) {
                        flash.error(violation.getPropertyPath() + " " + violation.getMessage());
                    }

                    routeContext.setLocal("contact", contact);
                    routeContext.render("contact");
                }
            }

        });

        GET("/session", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
//                routeContext.flashError("Test 123");
                routeContext.setSession("test", "aaaaa");
                routeContext.redirect("/");
                System.out.println("_______________________");
            }

        });
    }

    @Override
    protected RequestResponseFactory createRequestResponseFactory() {
        return new SessionRequestResponseFactory(this, new SessionManager());
    }

}
