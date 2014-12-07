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
package ro.fortsoft.pippo.groovy;

import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pippo.core.Languages;
import ro.fortsoft.pippo.core.Messages;
import ro.fortsoft.pippo.core.PippoConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.PippoSettings;
import ro.fortsoft.pippo.core.TemplateEngine;
import ro.fortsoft.pippo.core.route.UrlBuilder;
import ro.fortsoft.pippo.core.util.StringUtils;

/**
 * Groovy template engine for Pippo.
 *
 * @author James Moger
 *
 */
public class GroovyTemplateEngine implements TemplateEngine {

    public static final String GROOVY = "groovy";

    public static final String FILE_SUFFIX = "." + GROOVY;

    private final Logger logger = LoggerFactory.getLogger(GroovyTemplateEngine.class);

    private Languages languages;

    private Messages messages;

    private UrlBuilder urlBuilder;

    private MarkupTemplateEngine engine;

    @Override
    public void init(PippoSettings pippoSettings, Languages languages, Messages messages, UrlBuilder urlBuilder) {
        this.languages = languages;
        this.messages = messages;
        this.urlBuilder = urlBuilder;

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

        String pathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, DEFAULT_PATH_PREFIX);
        pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        pathPrefix = StringUtils.removeEnd(pathPrefix, "/");

        GroovyTemplateResolver cachingResolver = new GroovyTemplateResolver(pathPrefix);

        ClassLoader classLoader = getClass().getClassLoader();
        engine = new MarkupTemplateEngine(classLoader, configuration, cachingResolver);
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {

        if (templateName.indexOf('.') == -1) {
            templateName += FILE_SUFFIX;
        }
        Template groovyTemplate = null;

        try {
            groovyTemplate = engine.createTemplateByPath(templateName);
        } catch (ClassNotFoundException | IOException | RuntimeException e) {
            logger.error("Error reading Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }

        try {

            PippoGroovyTemplate gt = ((PippoGroovyTemplate) groovyTemplate.make(model));
            gt.setup(languages, messages, urlBuilder);
            gt.writeTo(writer);

        } catch (Exception e) {
            logger.error("Error processing Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }

    }
}
