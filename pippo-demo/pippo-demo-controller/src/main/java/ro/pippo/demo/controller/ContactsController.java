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

import ro.pippo.controller.Controller;
import ro.pippo.controller.ControllerRouter;
import ro.pippo.core.Param;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;
import ro.pippo.metrics.Metered;
import ro.pippo.metrics.Timed;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class ContactsController extends Controller {

    private ContactService contactService;

    public ContactsController() {
        contactService = new InMemoryContactService();
    }

    @Metered
    public void index() {
        getResponse().bind("contacts", contactService.getContacts()).render("contacts");
    }

    @Timed
    public void uriFor(@Param("id") int id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("action", "new");
        ControllerRouter router = (ControllerRouter) getApplication().getRouter();
        String uri = router.uriFor(ContactsController.class, "uriFor", parameters);

        getResponse().send("id = " + id + "; uri = " + uri);
    }

}
