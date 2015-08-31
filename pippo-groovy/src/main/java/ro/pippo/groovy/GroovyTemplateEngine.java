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

import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.pippo.core.Application;
import ro.pippo.core.Languages;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

/**
 * Groovy template engine for Pippo.
 *
 * @author James Moger
 */
public class GroovyTemplateEngine implements TemplateEngine<MarkupTemplateEngine> {

    private static final Logger log = LoggerFactory.getLogger(GroovyTemplateEngine.class);

    public static final String GROOVY = "groovy";
    public static final String FILE_SUFFIX = "." + GROOVY;

    private Languages languages;
    private Messages messages;
    private Router router;

    private MarkupTemplateEngine engine;

    @Override
    public void init(Application application) {
        this.languages = application.getLanguages();
        this.messages = application.getMessages();
        this.router = application.getRouter();

        PippoSettings pippoSettings = application.getPippoSettings();

        TemplateConfiguration configuration = new TemplateConfiguration();
        configuration.setBaseTemplateClass(PippoGroovyTemplate.class);
        configuration.setAutoEscape(true);

        if (pippoSettings.isDev()) {
            // Do not cache templates in dev mode
            configuration.setCacheTemplates(false);
        } else {
            configuration.setAutoIndent(true);
            configuration.setAutoNewLine(true);
            configuration.setAutoIndentString("  ");
        }

        String pathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, null);
        if (StringUtils.isNullOrEmpty(pathPrefix)) {
            pathPrefix = TemplateEngine.DEFAULT_PATH_PREFIX;
        }
        pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        pathPrefix = StringUtils.removeEnd(pathPrefix, "/");

        GroovyTemplateResolver cachingResolver = new GroovyTemplateResolver(pathPrefix);

        ClassLoader classLoader = getClass().getClassLoader();
        engine = new MarkupTemplateEngine(classLoader, configuration, cachingResolver);
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        try {
            Template groovyTemplate = engine.createTemplate(templateContent);
            PippoGroovyTemplate gt = ((PippoGroovyTemplate) groovyTemplate.make(model));
            gt.setup(languages, messages, router);
            gt.writeTo(writer);
        } catch (Exception e) {
            log.error("Error processing Groovy template {} ", templateContent, e);
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        if (templateName.indexOf('.') == -1) {
            templateName += FILE_SUFFIX;
        }

        Template groovyTemplate;
        try {
            groovyTemplate = engine.createTemplateByPath(templateName);
        } catch (ClassNotFoundException | IOException | RuntimeException e) {
            log.error("Error reading Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }

        try {
            PippoGroovyTemplate gt = ((PippoGroovyTemplate) groovyTemplate.make(model));
            gt.setup(languages, messages, router);
            gt.writeTo(writer);
        } catch (Exception e) {
            log.error("Error processing Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public MarkupTemplateEngine getEngine() {
        return engine;
    }

}
