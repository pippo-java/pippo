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
import ro.pippo.core.session.CookieSessionFactory;
import ro.pippo.core.Flash;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.session.SessionFactory;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteHandlerChain;
import ro.pippo.core.route.WebjarsResourceHandler;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public class ValidationApplication extends Application {

    @Override
    public void init() {
        super.init();

        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = new Contact();
                Map<String, Object> model = new HashMap<>();
                model.put("contact", contact);
                response.render("contact", model);
            }

        });

        POST("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = request.createEntityFromParameters(Contact.class);

                // check for validation
                Set<ConstraintViolation<Contact>> violations = validator.validate(contact);
                if (violations.isEmpty()) {
                    response.send(contact.toString());
                } else {
                    // makes violations available to template via flash (response.getLocals().get("flash"))
                    Flash flash = response.getFlash();
                    for (ConstraintViolation<Contact> violation : violations) {
                        flash.error(violation.getPropertyPath() + " " + violation.getMessage());
                    }
                    Map<String, Object> model = new HashMap<>();
                    model.put("contact", contact);
                    response.render("contact", model);
                }
            }

        });
    }

    /*
    @Override
    public SessionFactory getSessionFactory() {
        return new CookieSessionFactory(getPippoSettings());
    }
    */

}
