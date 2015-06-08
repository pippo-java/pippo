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
package ro.pippo.demo.controller;

/**
 * @author Decebal Suiu
 */
public class ControllerApplication extends ro.pippo.controller.ControllerApplication {

    @Override
    protected void onInit() {
        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        GET("/", ContactsController.class, "index");
        GET("/contact/{id}", ContactsController.class, "uriFor");

        GET("/collections", CollectionsController.class, "index");
        PUT("/collections", CollectionsController.class, "update");
        POST("/collections", CollectionsController.class, "post");
    }

}
