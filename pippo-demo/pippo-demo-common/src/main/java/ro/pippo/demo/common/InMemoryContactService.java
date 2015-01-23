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
package ro.pippo.demo.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class InMemoryContactService implements ContactService {

    private static int nextId = 1;

    private List<Contact> contacts;

    public InMemoryContactService() {
        addDefaultContacts();
    }

    @Override
    public List<Contact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    @Override
    public Contact getContact(int id) {
        if (id > 0) {
            for (Contact contact : contacts) {
                if (id == contact.getId()) {
                    return contact;
                }
            }
        }

        return null;
    }

    @Override
    public void delete(int id) {
        if (id > 0) {
            Iterator<Contact> iterator = contacts.iterator();
            while (iterator.hasNext()) {
                if (id == iterator.next().getId()) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public Contact save(Contact contact) {
        if (contact.getId() > 0) {
            // update
            for (int i = 0; i < contacts.size(); i++) {
                if (contact.getId() == contacts.get(i).getId()) {
                    contacts.set(i, contact);
                    break;
                }
            }
        } else {
            // new
            contact.setId(nextId++);
            contacts.add(contact);
        }

        return contact;
    }

    private void addDefaultContacts() {
        contacts = new ArrayList<>();
        contacts.add(new Contact(nextId++).setName("John").setPhone("0733434435").setAddress("Sunflower Street, No. 6"));
        contacts.add(new Contact(nextId++).setName("Peter").setPhone("0742333331").setAddress("Sunflower Street, No. 2"));
        contacts.add(new Contact(nextId++).setName("Maria").setPhone("0741200000").setAddress("Sunflower Street, No. 3"));
    }

}
