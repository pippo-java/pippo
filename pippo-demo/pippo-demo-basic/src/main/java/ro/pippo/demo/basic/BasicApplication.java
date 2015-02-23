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
package ro.pippo.demo.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.route.FileResourceHandler;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.demo.common.Contact;

import java.io.File;

/**
 * @author Decebal Suiu
 */
public class BasicApplication extends Application {

    private final static Logger log = LoggerFactory.getLogger(BasicApplication.class);

    @Override
    public void init() {
        super.init();

        // send 'Hello World' as response
        GET("/", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.send("Hello World");
            }

        });

        // send a file as response
        GET("/file", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                routeContext.send(new File("pom.xml"));
            }

        });

        // send a json as response
        GET("/json", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                Contact contact = new Contact()
                    .setId(12345)
                    .setName("John")
                    .setPhone("0733434435")
                    .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_JSON); // 1
//                response.send(new Gson().toJson(contact)); // 1
                routeContext.json().send(contact); // 2
            }

        });

        // send xml as response
        GET("/xml", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                Contact contact = new Contact()
                    .setId(12345)
                    .setName("John")
                    .setPhone("0733434435")
                    .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_XML); // 1
//                response.send(new Xstream().toXML(contact)); // 1
                routeContext.xml().send(contact); // 2
            }

        });

        // send text as response
        GET("/text", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                Contact contact = new Contact()
                    .setId(12345)
                    .setName("John")
                    .setPhone("0733434435")
                    .setAddress("Sunflower Street, No. 6");
                routeContext.text().send(contact); // 2
            }

        });

        // send an object and negotiate the Response content-type, default to XML
        GET("/negotiate", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                Contact contact = new Contact()
                    .setId(12345)
                    .setName("John")
                    .setPhone("0733434435")
                    .setAddress("Sunflower Street, No. 6");
                routeContext.xml().negotiateContentType().send(contact);
            }

        });

        // send a template as response
        GET("/template", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                String message;

                String lang = routeContext.fromRequest("lang").toString();
                if (lang == null) {
                    message = getMessages().get("pippo.greeting", routeContext);
                } else {
                    message = getMessages().get("pippo.greeting", lang);
                }

                routeContext.putLocal("greeting", message);
                routeContext.render("hello");
            }

        });

        // send an error as response
        GET("/error", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                int statusCode = routeContext.fromRequest("code").toInt(HttpConstants.StatusCode.INTERNAL_ERROR);
                // do not commit the response
                // this delegates response representation to PippoFilter
                routeContext.status(statusCode);
            }

        });

        // send files from a local folder (try a request like 'src/main/java/ro/pippo/demo/basic/BasicApplication.java')
        GET(new FileResourceHandler("/src", "src"));

        // throw a programatically exception
        GET("/exception", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                throw new RuntimeException("My programatically error");
            }

        });

        // use a finally filter (invoked even when exceptions were raised in previous routes)
        // test with route "/" and "/exception"
        ALL("/.*", new RouteHandler() {

            @Override
            public void handle(RouteContext routeContext) {
                log.info(">>> Cleanup here");
            }

        }).runAsFinally();
    }

}
