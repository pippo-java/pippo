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
package ro.fortsoft.pippo.jade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pippo.core.Messages;
import ro.fortsoft.pippo.core.PippoRuntimeException;

/**
 * Pippo helper for accessing localized messages in a Jade template and
 * formatting relative locale-aware dates.
 *
 * @author James Moger
 */
public class PippoHelper {

    public final static Logger logger = LoggerFactory.getLogger(PippoHelper.class);

    final Messages messages;
    final String language;
    final Locale locale;
    final PrettyTime prettyTime;

    public PippoHelper(Messages messages, String language, Locale locale) {
        this.messages = messages;
        this.language = language;
        this.locale = locale;
        this.prettyTime = new PrettyTime(locale);

    }

    public String i18n(String messageKey) {
        String messageValue = messages.get(messageKey, language);
        return messageValue;
    }

    public String i18n(String messageKey, Object... parameters) {
        String messageValue = messages.get(messageKey, language, parameters);
        return messageValue;
    }

    public String formatDate(Object input, String styleOrPattern) {
        int type = parseStyle(styleOrPattern);
        DateFormat df;
        if (type == -1) {
            df = new SimpleDateFormat(styleOrPattern, locale);
        } else {
            df = DateFormat.getDateTimeInstance(type, type, locale);
        }
        Date date = getDateObject(input);
        return df.format(date);
    }

    public String prettyTime(Object input) {
        Date date = getDateObject(input);
        return prettyTime.format(date);
    }

    private Date getDateObject(Object value) {

        if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        } else if (value instanceof Long) {
            return new Date((Long) value);
        } else {
            throw new PippoRuntimeException("Failed to get a date object from {}!", value);
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
