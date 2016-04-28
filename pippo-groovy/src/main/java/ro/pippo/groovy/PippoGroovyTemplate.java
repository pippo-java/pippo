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
package ro.pippo.groovy;

import groovy.text.markup.BaseTemplate;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.ocpsoft.prettytime.PrettyTime;

import ro.pippo.core.Languages;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.PublicResourceHandler;
import ro.pippo.core.route.Router;
import ro.pippo.core.route.WebjarsResourceHandler;
import ro.pippo.core.util.StringUtils;

/**
 * Base class for Pippo Groovy Templates.
 *
 * @author James Moger
 */
public abstract class PippoGroovyTemplate extends BaseTemplate {

    private final Map<String, String> modelTypes;

    private final MarkupTemplateEngine engine;

    Router router;

    String language;

    Locale locale;

    Languages languages;

    Messages messages;

    PrettyTime prettyTime;

    AtomicReference<String> webjarsPatternRef;

    AtomicReference<String> publicPatternRef;

    public PippoGroovyTemplate(final MarkupTemplateEngine templateEngine, final Map model,
            final Map<String, String> modelTypes, final TemplateConfiguration configuration) {
        super(templateEngine, model, modelTypes, configuration);

        this.modelTypes = modelTypes;
        this.engine = templateEngine;
        this.webjarsPatternRef = new AtomicReference<>();
        this.publicPatternRef = new AtomicReference<>();
    }

    @SuppressWarnings("unchecked")
    public void setup(Languages languages, Messages messages, Router router) {
        this.languages = languages;
        this.messages = messages;
        this.router = router;

        // set global template variables
        getModel().put("contextPath",  router.getContextPath());
        getModel().put("appPath",  router.getApplicationPath());

        String language = (String) getModel().get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(language);
        }
        this.language = language;

        // prepare the locale
        Locale locale = (Locale) getModel().get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }
        this.locale = locale;
    }

    public void ln() throws IOException {
        newLine();
    }

    public String webjarsAt(String path) {
        return classpathResourceAt(path, webjarsPatternRef, WebjarsResourceHandler.class);
    }

    public String publicAt(String path) {
        return classpathResourceAt(path, publicPatternRef, PublicResourceHandler.class);
    }

    private String classpathResourceAt(String path, AtomicReference<String> patternRef,
                                       Class<? extends ClasspathResourceHandler> resourceHandlerClass) {

        if (patternRef.get() == null) {
            String pattern = router.uriPatternFor(resourceHandlerClass);
            if (pattern == null) {
                throw new PippoRuntimeException("You must register a route for {}",
                        resourceHandlerClass.getSimpleName());
            }

            patternRef.set(pattern);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ClasspathResourceHandler.PATH_PARAMETER, path);

        return router.uriFor(patternRef.get(), parameters);
    }

    public String i18n(String messageKey) throws IOException {
        return messages.get(messageKey, language);
    }

    public String i18n(String messageKey, Object... args) throws IOException {
        return messages.get(messageKey, language, args);
    }

    public String formatTime(Object input, String styleOrPattern) {
        Date date = getDateObject(input);

        DateFormat df;
        int type = parseStyle(styleOrPattern);
        if (type == -1) {
            df = new SimpleDateFormat(styleOrPattern, locale);
        } else {
            df = DateFormat.getDateTimeInstance(type, type, locale);
        }

        return df.format(date);
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

    public String prettyTime(Object input) {
        if (prettyTime == null) {
            this.prettyTime = new PrettyTime(locale);
        }
        Date date = getDateObject(input);

        return prettyTime.format(date);
    }

    public String ng(String content) throws IOException {
        return "{{ " + content + " }}";
    }

    /**
     * Imports a template and renders it using the specified model, allowing
     * fine grained composition of templates and layouting. This works
     * similarily to a template include but allows a distinct model to be used.
     * If the layout inherits from the parent model, a new model is created,
     * with the values from the parent model, eventually overriden with those
     * provided specifically for this layout.
     *
     * @param model
     *            model to be passed to the template
     * @param templateName
     *            the name of the template to be used as a layout
     * @param inheritModel
     *            a boolean indicating if we should inherit the parent model
     * @return this template instance
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Override
    public Object layout(Map model, String templateName, boolean inheritModel) throws IOException,
            ClassNotFoundException {
        Map submodel = inheritModel ? forkModel(model) : model;
        URL resource = engine.resolveTemplate(templateName);
        PippoGroovyTemplate template = (PippoGroovyTemplate) engine
                .createTypeCheckedModelTemplate(resource, modelTypes).make(submodel);
        template.setup(languages, messages, router);
        template.writeTo(getOut());

        return this;
    }

    @SuppressWarnings("unchecked")
    private Map forkModel(Map m) {
        Map result = new HashMap();
        result.putAll(getModel());
        result.putAll(m);

        return result;
    }

}
