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
package ro.pippo.demo.crudng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Param;
import ro.pippo.controller.Body;
import ro.pippo.controller.Controller;
import ro.pippo.demo.common.Contact;
import ro.pippo.demo.common.ContactService;
import ro.pippo.metrics.Metered;

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
        getResponse().xml().contentType(getRequest()).send(getContactService().getContacts());
        log.info("Retrieved all contacts");
    }

    @Metered("api.contact.get")
    public void getContact(@Param("id") int id) {
        Contact contact = (id > 0) ? getContactService().getContact(id) : new Contact();
        getResponse().xml().contentType(getRequest()).send(contact);
        log.info("Retrieved contact #{} '{}'", contact.getId(), contact.getName());
    }

    @Metered("api.contact.delete")
    public void deleteContact(@Param("id") int id) {
        if (id <= 0) {
            getResponse().badRequest();
        } else {
            Contact contact = getContactService().getContact(id);
            if (contact == null) {
                getResponse().badRequest();
            } else {
                getContactService().delete(id);
                log.info("Deleted contact #{} '{}'", contact.getId(), contact.getName());
                getResponse().ok();
            }
        }
    }

    @Metered("api.contact.post")
    public void saveContact(@Body Contact contact) {
        getContactService().save(contact);
        getResponse().ok();
        log.info("Saved contact #{} '{}'", contact.getId(), contact.getName());
    }

}
