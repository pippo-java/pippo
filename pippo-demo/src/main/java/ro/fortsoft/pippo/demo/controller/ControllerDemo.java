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
package ro.fortsoft.pippo.demo.controller;

import ro.fortsoft.pippo.core.Pippo;

/**
 * @author Decebal Suiu
 */
public class ControllerDemo {

    public static void main(String[] args) {
        Pippo pippo = new Pippo();
        pippo.getServer().getSettings().staticFilesLocation("/public");
        pippo.getApplication().GET("/", ContactsController.class, "index");
        pippo.getApplication().GET("/contacts/:id", ContactsController.class, "urlFor");
        pippo.start();
    }

}
