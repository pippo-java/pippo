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
package ro.pippo.demo.template;

import ro.pippo.core.Application;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.metrics.Metered;
import ro.pippo.metrics.Timed;

import java.util.Calendar;
import java.util.Date;

/**
 * @author James Moger
 */
public class TemplateApplication extends Application {

    private final String template;

    public TemplateApplication(TemplateEngine engine, String template) {
        this.template = template;

        // set the template engine
        setTemplateEngine(engine);
    }

    @Override
    protected void onInit() {
        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        // filter all requests and setup the language and locale
        GET("/.*", new LanguageFilter(getLanguages(), true, true));

        GET("/", new RouteHandler() {

            @Metered("getRoot")
            @Override
            public void handle(RouteContext routeContext) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -5);
                Date testDate = calendar.getTime();

                routeContext.setLocal("testDate", testDate);
                routeContext.setLocal("mode", getRuntimeMode());

                routeContext.render(template);
            }

        });

        // throw an exception to demonstrate the template exception renderer
        GET("/exception", new RouteHandler() {

            int counter;

            @Timed("getException")
            @Override
            public void handle(RouteContext routeContext) {
                throw new PippoRuntimeException("Exception \"&nbsp;\" <#{}>", ++counter);
            }

        });
    }

}
