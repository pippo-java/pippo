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
package ro.pippo.trimou;

import java.util.Locale;

import org.trimou.engine.config.AbstractConfigurationAware;
import org.trimou.engine.locale.LocaleSupport;

/**
 * Sets a thread local variable to reference the request locale.
 *
 * @author James Moger
 */
class ThreadLocalLocaleSupport extends AbstractConfigurationAware implements LocaleSupport {

    ThreadLocal<Locale> threadLocalLocale;

    ThreadLocalLocaleSupport() {
        this.threadLocalLocale = new ThreadLocal<>();
    }

    public void setCurrentLocale(Locale locale) {
        threadLocalLocale.set(locale);
    }

    public void remove() {
        threadLocalLocale.remove();
    }

    @Override
    public Locale getCurrentLocale() {
        return threadLocalLocale.get();
    }

}