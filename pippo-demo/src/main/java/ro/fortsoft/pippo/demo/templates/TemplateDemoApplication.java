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
package ro.fortsoft.pippo.demo.templates;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.TemplateEngine;
import ro.fortsoft.pippo.core.route.PublicResourceHandler;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.WebjarsResourceHandler;
import ro.fortsoft.pippo.demo.DemoRequestLanguageFilter;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author James Moger
 */
public class TemplateDemoApplication extends Application {

    private final TemplateEngine engine;
    private final String template;

    public TemplateDemoApplication(TemplateEngine engine, String template) {
        this.engine = engine;
        this.template = template;
    }

    @Override
    public void init() {
        super.init();

        // set the template engine
        setTemplateEngine(engine);

        // add classpath resource handlers
        GET(new WebjarsResourceHandler());
        GET(new PublicResourceHandler());

        // filter all requests and setup the language and locale
        GET("/.*", new DemoRequestLanguageFilter(getLanguages(), true));

        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
            	Calendar c = Calendar.getInstance();
            	c.add(Calendar.DATE, -5);
            	Date testDate = c.getTime();
                Map<String, Object> model = new HashMap<>();
                model.put("testDate", testDate);
                model.put("mode", getRuntimeMode());

                // .mustache is the default file extension
                response.render(template, model);
            }

        });

        // throw an exception to demonstrate the template exception renderer
        GET("/exception", new RouteHandler() {

            int counter;

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                throw new PippoRuntimeException("Exception \"&nbsp;\" <#{}>", ++counter);
            }

        });
    }

}
