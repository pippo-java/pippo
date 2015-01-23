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
package ro.pippo.demo.guice;

import com.google.inject.AbstractModule;
import ro.pippo.core.Application;
import ro.pippo.demo.common.ContactService;
import ro.pippo.demo.common.InMemoryContactService;

/**
 * @author James Moger
 */
public class GuiceModule2 extends AbstractModule {

	@Override
	protected void configure() {
		bind(ContactService.class).to(InMemoryContactService.class).asEagerSingleton();
		bind(Application.class).to(GuiceApplication2.class).asEagerSingleton();
	}

}