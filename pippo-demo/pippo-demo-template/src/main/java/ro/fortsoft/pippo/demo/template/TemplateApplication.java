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
package ro.fortsoft.pippo.demo.template;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.TemplateEngine;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;
import ro.fortsoft.pippo.metrics.Metered;
import ro.fortsoft.pippo.metrics.Timed;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    public void init() {
        super.init();

        // add classpath resource handlers
        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        // filter all requests and setup the language and locale
        GET("/.*", new LanguageFilter(getLanguages(), true));

        GET("/", new RouteHandler() {

            @Metered("getRoot")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
            	Calendar calendar = Calendar.getInstance();
            	calendar.add(Calendar.DATE, -5);
            	Date testDate = calendar.getTime();
                Map<String, Object> model = new HashMap<>();
                model.put("testDate", testDate);
                model.put("mode", getRuntimeMode());

                response.render(template, model);
            }

        });

        // throw an exception to demonstrate the template exception renderer
        GET("/exception", new RouteHandler() {

            int counter;

            @Timed("getException")
            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                throw new PippoRuntimeException("Exception \"&nbsp;\" <#{}>", ++counter);
            }

        });
    }

}
