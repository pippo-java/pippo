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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.trimou.handlebars.Options;
import org.trimou.handlebars.i18n.LocaleAwareValueHelper;

import ro.pippo.core.Messages;

/**
 * <p>
 * Using the i18n helper:
 * </p>
 * <code>
 * {{i18n "my.key"}}
 * </code>
 * <p>
 * Passing arguments to the MessageFormat string formatter:
 * </p>
 * <code>
 * {{i18n "hello.world" "Frank"}}
 * </code>
 *
 * @author James Moger
 */
public class I18nHelper extends LocaleAwareValueHelper {

    private final Messages messages;

    public I18nHelper(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void execute(Options options) {

        Locale locale = getCurrentLocale();
        String requestLang = locale.toLanguageTag();

        String messageKey = options.getParameters().get(0).toString();
        List<Object> args = new ArrayList<>();
        for (int i = 1; i < options.getParameters().size(); i++) {
            args.add(options.getParameters().get(i));
        }

        String messageValue = messages.get(messageKey, requestLang, args.toArray());
        append(options, messageValue);

    }

}
