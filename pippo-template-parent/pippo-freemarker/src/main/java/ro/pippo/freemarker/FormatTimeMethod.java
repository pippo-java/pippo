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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ro.pippo.core.PippoRuntimeException;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;

/**
 * FormatTime method for producing locale-aware dates in a Freemarker template.
 *
 * @author James Moger
 *
 */
public class FormatTimeMethod implements TemplateMethodModelEx {

    private final Locale locale;

    public FormatTimeMethod(Locale locale) {
        this.locale = locale;
    }

    @Override
    public TemplateModel exec(List args) {
        Date date = getFormattableObject(args.get(0));
        int type = parseStyle(args.get(1).toString());
        DateFormat df;
        if (type == -1) {
            df = new SimpleDateFormat(args.get(1).toString(), locale);
        } else {
            df = DateFormat.getDateTimeInstance(type, type, locale);
        }

        String result = df.format(date);

        return new SimpleScalar(result);
    }

    private Date getFormattableObject(Object value) {
        if (value instanceof SimpleDate) {
            return ((SimpleDate) value).getAsDate();
        } else {
            throw new PippoRuntimeException("Formattable object for FormatTime not found!");
        }
    }

    protected Integer parseStyle(String style) {
        if ("full".equals(style)) {
            return DateFormat.FULL;
        } else if ("long".equals(style)) {
            return DateFormat.LONG;
        } else if ("short".equals(style)) {
            return DateFormat.SHORT;
        } else if ("medium".equals(style)) {
            return DateFormat.MEDIUM;
        } else {
            return -1;
        }
    }

}
