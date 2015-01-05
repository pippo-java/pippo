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
package ro.fortsoft.pippo.demo.crudng;

import ro.fortsoft.pippo.core.Param;
import ro.fortsoft.pippo.core.controller.Body;
import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.demo.crud.Contact;
import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.metrics.Metered;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTful API controller.
 * <p>
 * This controller demonstrates route registration, metrics collection,
 * accept-type negotiation, parameter annotations, and body annotations.
 * </p>
 *
 * @author James Moger
 */
public class CrudNgApiController extends Controller {

    private static final Logger log = LoggerFactory.getLogger(CrudNgApiController.class);

    ContactService getContactService() {
        return ((CrudNgApplication) getApplication()).getContactService();
    }

    @Metered("api.contacts.get")
    public void getContacts() {
        getResponse().send(getContactService().getContacts(), getRequest().getAcceptType());
        log.info("Retrieved all contacts");
    }

    @Metered("api.contact.get")
    public void getContact(@Param("id") int id) {
        Contact contact = (id > 0) ? getContactService().getContact(id) : new Contact();
        getResponse().send(contact, getRequest().getAcceptType());
        log.info("Retrieved contact #{} '{}'", contact.getId(), contact.getName());
    }

    @Metered("api.contact.delete")
    public void deleteContact(@Param("id") int id) {
        if (id <= 0) {
            getResponse().sendBadRequest();
        } else {
            Contact contact = getContactService().getContact(id);
            if (contact == null) {
                getResponse().sendBadRequest();
            } else {
                getContactService().delete(id);
                log.info("Deleted contact #{} '{}'", contact.getId(), contact.getName());
                getResponse().sendOk();
            }
        }
    }

    @Metered("api.contact.post")
    public void saveContact(@Body Contact contact) {
        getContactService().save(contact);
        getResponse().sendOk();
        log.info("Saved contact #{} '{}'", contact.getId(), contact.getName());
    }
}
