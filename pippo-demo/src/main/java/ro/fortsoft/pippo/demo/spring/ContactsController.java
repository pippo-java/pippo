package ro.fortsoft.pippo.demo.spring;

import ro.fortsoft.pippo.core.controller.Controller;
import ro.fortsoft.pippo.demo.crud.ContactService;

import javax.inject.Inject;

/**
 * @author Decebal Suiu
 */
public class ContactsController extends Controller {

    @Inject
    private ContactService contactService;

    public void index() {
        getResponse().getLocals().put("contacts", contactService.getContacts());
        getResponse().render("crud/contacts.ftl");
    }

}
