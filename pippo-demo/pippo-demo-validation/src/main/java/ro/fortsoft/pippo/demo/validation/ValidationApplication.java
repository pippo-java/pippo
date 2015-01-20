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
package ro.fortsoft.pippo.demo.validation;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
//                model.put("errors", Collections.emptyList());
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
                    List<String> errors = new ArrayList<>();
                    for (ConstraintViolation<Contact> violation : violations) {
                        errors.add(violation.getPropertyPath() + " " + violation.getMessage());
                    }
                    Map<String, Object> model = new HashMap<>();
                    model.put("contact", contact);
                    model.put("errors", errors);
                    response.render("contact", model);
                }
            }

        });
    }

}
