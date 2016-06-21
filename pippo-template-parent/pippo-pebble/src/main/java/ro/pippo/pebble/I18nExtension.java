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
package ro.pippo.pebble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.escaper.SafeString;
import com.mitchellbosecke.pebble.template.EvaluationContext;

import ro.pippo.core.Messages;

import com.google.common.collect.Lists;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

public class I18nExtension extends AbstractExtension {

    private final Messages messages;

    public I18nExtension(Messages messages) {
        this.messages = messages;
    }

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("i18n", new I18nFunction());
        return functions;
    }

    class I18nFunction implements Function {

        @Override
        public List<String> getArgumentNames() {
            List<String> names = new ArrayList<>();
            names.add("key");

            // we simulate up to 5 arguments for message parameters
            names.add("arg1");
            names.add("arg2");
            names.add("arg3");
            names.add("arg4");
            names.add("arg5");
            return names;
        }

        @Override
        public Object execute(Map<String, Object> args) {
            String messageKey = (String) args.get("key");

            EvaluationContext context = (EvaluationContext) args.get("_context");
            Locale locale = context.getLocale();
            String requestLang = locale.toLanguageTag();

            List<Object> messageArgs = Lists.newArrayList();
            for (int i = 1; i <= 5; i++) {
                if (args.containsKey("arg" + i)) {
                    Object object = args.get("arg" + i);
                    messageArgs.add(object);
                }
            }

            String messageValue = messages.get(messageKey, requestLang, messageArgs.toArray());
            return new SafeString(messageValue);
        }

    }

}
