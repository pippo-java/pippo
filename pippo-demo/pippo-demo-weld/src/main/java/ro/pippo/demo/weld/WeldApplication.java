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
package ro.pippo.demo.weld;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import ro.pippo.controller.ControllerApplication;
import ro.pippo.weld.WeldControllerFactory;

/**
 * @author Daniel Jipa
 */
public class WeldApplication extends ControllerApplication {

    @Override
    protected void onInit() {
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
        setControllerFactory(new WeldControllerFactory(container));

        // add routes for static content
        addPublicResourceRoute();
        addWebjarsResourceRoute();

        // add controller
        GET("/", ContactsController.class, "index");
    }

}
