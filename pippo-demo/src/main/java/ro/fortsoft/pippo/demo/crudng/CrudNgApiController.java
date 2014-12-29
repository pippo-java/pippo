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
 *
 */
public class CrudNgApiController extends Controller {

    private final Logger log = LoggerFactory.getLogger(CrudNgApiController.class);

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
