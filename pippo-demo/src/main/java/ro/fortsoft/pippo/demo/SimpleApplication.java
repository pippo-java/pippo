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
package ro.fortsoft.pippo.demo;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.RouteHandler;
import ro.fortsoft.pippo.core.RouteHandlerChain;
import ro.fortsoft.pippo.demo.crud.Contact;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class SimpleApplication extends Application {

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
                        .setName("John")
                        .setPhone("0733434435")
                        .setAddress("Sunflower Street, No. 6");
                // you can use variant 1 or 2
//                response.contentType(HttpConstants.ContentType.APPLICATION_JSON); // 1
//                response.send(new Gson().toJson(contact)); // 1
                response.json(contact); // 2
            }

        });

        // send a template as response
        GET("/template", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                Map<String, Object> model = new HashMap<String, Object>();
                model.put("greeting", "Hello my friend");
                response.render("hello.ftl", model);
            }

        });

        // send an error as response
        GET("/error", new RouteHandler() {

            @Override
            public void handle(Request request, Response response, RouteHandlerChain chain) {
                throw new RuntimeException("Error");
            }

        });

    }

}
