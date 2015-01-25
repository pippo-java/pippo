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

import ro.pippo.core.Application;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.FileResourceHandler;
import ro.pippo.core.route.RouteHandler;
import ro.pippo.core.route.RouteHandlerChain;
import ro.pippo.demo.common.Contact;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class BasicApplication extends Application {

    @Override
    public void init() {
        super.init();

        // send 'Hello World' as response
        GET("/", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.send("Hello World");
            }

        });

        // send a file as response
        GET("/file", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                response.file(new File("pom.xml"));
            }

        });

        // send a json as response
        GET("/json", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = new Contact()
                        .setId(12345)
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_JSON); // 1
//                response.send(new Gson().toJson(contact)); // 1
                response.json(contact); // 2
            }

        });

        // send xml as response
        GET("/xml", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = new Contact()
                        .setId(12345)
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_XML); // 1
//                response.send(new Xstream().toXML(contact)); // 1
                response.xml(contact); // 2
            }

        });

        // send text as response
        GET("/text", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = new Contact()
                        .setId(12345)
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                response.text(contact); // 2
            }

        });

        // send an object and negotiate the Response content-type, default to XML
        GET("/negotiate", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Contact contact = new Contact()
                        .setId(12345)
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                response.xml().contentType(request).send(contact);
            }

        });

        // send a template as response
        GET("/template", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
            	String message;

            	String lang = request.getParameter("lang").toString();
            	if (lang == null) {
            		message = getMessages().get("pippo.greeting", request, response);
            	} else {
            		message = getMessages().get("pippo.greeting", lang);
            	}

                Map<String, Object> model = new HashMap<>();
                model.put("greeting", message);
                response.render("hello", model);
            }

        });

        // send an error as response
        GET("/error", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                int statusCode = request.getParameter("code").toInt(HttpConstants.StatusCode.INTERNAL_ERROR);
                // do not commit the response
                // this delegates response representation to PippoFilter
                response.status(statusCode);
            }

        });

         // send files from a local folder (try a request like 'src/main/java/ro/pippo/demo/basic/BasicApplication.java')
        GET(new FileResourceHandler("/src", "src"));
    }

}
