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
package ro.fortsoft.pippo.demo.controller;

import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.demo.crud.InMemoryContactService;

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

    public void index() {
        getResponse().bind("contacts", contactService.getContacts()).render("crud/contacts.ftl");
    }

    public void urlFor() {
        int id = getRequest().getParameter("id").toInt();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", id);
        parameters.put("action", "new");
        String url = getApplication().urlFor(ContactsController.class, "urlFor", parameters);

        getResponse().send("id = " + id + "; url = " + url);
    }

}
