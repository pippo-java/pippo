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
package ro.pippo.demo.ajax;

import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

/**
 * @author Decebal Suiu
 */
public class AjaxApplication extends Application {

    private long pageAccessTime;
    private long increment;

    @Override
    protected void onInit() {
        getRouter().ignorePaths("/favicon.ico");

        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.redirect("/ajax");
            }

        });

        GET("/ajax", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                pageAccessTime = System.currentTimeMillis();

                routeContext.render("index");
            }

        });

        GET("/seconds", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                long seconds = (System.currentTimeMillis() - pageAccessTime) / 1000;
                routeContext.getResponse().send("You have been on this page for {} seconds...", seconds);
            }

        });

        POST("/increment", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                increment++;
                routeContext.getResponse().send("Click Me! ({})", increment);
            }

        });

    }

}
