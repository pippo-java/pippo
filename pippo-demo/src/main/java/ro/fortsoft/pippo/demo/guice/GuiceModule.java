package ro.fortsoft.pippo.demo.guice;

import ro.fortsoft.pippo.demo.crud.ContactService;
import ro.fortsoft.pippo.demo.crud.InMemoryContactService;

import com.google.inject.AbstractModule;

public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ContactService.class).to(InMemoryContactService.class).asEagerSingleton();
	}

}