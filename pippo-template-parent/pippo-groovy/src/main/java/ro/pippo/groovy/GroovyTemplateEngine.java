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
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractTemplateEngine;
import ro.pippo.core.Application;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Groovy template engine for Pippo.
 *
 * @author James Moger
 */
@MetaInfServices(TemplateEngine.class)
public class GroovyTemplateEngine extends AbstractTemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(GroovyTemplateEngine.class);

    public static final String GROOVY = "groovy";

    private MarkupTemplateEngine engine;

    @Override
    public void init(Application application) {
        super.init(application);

        PippoSettings pippoSettings = getPippoSettings();

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

        String pathPrefix = getTemplatePathPrefix();
        pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        pathPrefix = StringUtils.removeEnd(pathPrefix, "/");

        GroovyTemplateResolver cachingResolver = new GroovyTemplateResolver(pathPrefix);

        ClassLoader classLoader = getClass().getClassLoader();

        // allow custom initialization
        init(application, configuration);

        engine = new MarkupTemplateEngine(classLoader, configuration, cachingResolver);
    }

    @Override
    protected String getDefaultFileExtension() {
        return GROOVY;
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        try {
            Template groovyTemplate = engine.createTemplate(templateContent);
            PippoGroovyTemplate gt = (PippoGroovyTemplate) groovyTemplate.make(model);
            gt.setup(getLanguages(), getMessages(), getRouter());
            gt.writeTo(writer);
        } catch (Exception e) {
            log.error("Error processing Groovy template {} ", templateContent, e);
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        if (templateName.indexOf('.') == -1) {
            templateName += "." + getFileExtension();
        }

        Template groovyTemplate;
        try {
            groovyTemplate = engine.createTemplateByPath(templateName);
        } catch (ClassNotFoundException | IOException | RuntimeException e) {
            log.error("Error reading Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }

        try {
            PippoGroovyTemplate gt = (PippoGroovyTemplate) groovyTemplate.make(model);
            gt.setup(getLanguages(), getMessages(), getRouter());
            gt.writeTo(writer);
        } catch (Exception e) {
            log.error("Error processing Groovy template {} ", templateName, e);
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Override this method if you want to modify the template configuration.
     *
     * @param application
     * @param configuration
     */
    protected void init(Application application, TemplateConfiguration configuration) {
    }

}
