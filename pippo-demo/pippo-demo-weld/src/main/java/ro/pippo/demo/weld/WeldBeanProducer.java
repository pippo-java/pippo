package ro.pippo.demo.weld;

import javax.enterprise.inject.Produces;

import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;

public class WeldBeanProducer {
	
	@Produces
	public ContactService contactService() {
		return new InMemoryContactService();
	}
}
