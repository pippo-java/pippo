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
package ro.pippo.freemarker;

import java.util.ArrayList;
import java.util.List;

import ro.pippo.core.Messages;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Internationalization (i18n) method for accessing localized messages in a Freemarker template.
 *
 * @author James Moger
 */
public class I18nMethod implements TemplateMethodModelEx {

    final Messages messages;
    final String language;

    public I18nMethod(Messages messages, String language) {
        this.messages = messages;
        this.language = language;
    }

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        if (args.size() == 1) {
            String messageKey = ((SimpleScalar) args.get(0)).getAsString();
            String messageValue = messages.get(messageKey, language);

            return new SimpleScalar(messageValue);
        } else if (args.size() > 1) {
            List<Object> objects = new ArrayList<>();

            for (Object o : args) {
                if (o instanceof SimpleScalar) {
                    objects.add(((SimpleScalar) o).getAsString());
                } else if (o instanceof SimpleNumber) {
                    objects.add(o.toString());
                } else if (o instanceof StringModel) {
                    objects.add(((StringModel) o).getWrappedObject());
                }
            }

            String messageKey = objects.get(0).toString();
            String messageValue = messages.get(messageKey, language, objects.subList(1, objects.size()).toArray());
            return new SimpleScalar(messageValue);
        } else {
            throw new TemplateModelException("Please specify a message key for the i18n() method!");
        }
    }

}
