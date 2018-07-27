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
package ro.pippo.jade;

import de.neuland.jade4j.Jade4J.Mode;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.ReaderTemplateLoader;
import de.neuland.jade4j.template.TemplateLoader;
import org.kohsuke.MetaInfServices;
import ro.pippo.core.AbstractTemplateEngine;
import ro.pippo.core.Application;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
@MetaInfServices(TemplateEngine.class)
public class JadeTemplateEngine extends AbstractTemplateEngine {

    public static final String JADE = "jade";

    private JadeConfiguration configuration;

    @Override
    public void init(Application application) {
        super.init(application);

        Router router = getRouter();
        PippoSettings pippoSettings = getPippoSettings();

        configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new ClassTemplateLoader(JadeTemplateEngine.class, getTemplatePathPrefix()));
        configuration.setMode(Mode.HTML);
        if (pippoSettings.isDev()) {
            configuration.setPrettyPrint(true);
            configuration.setCaching(false); // disable cache
        }

        // set global template variables
        configuration.getSharedVariables().put("contextPath", router.getContextPath());
        configuration.getSharedVariables().put("appPath", router.getApplicationPath());

        // allow custom initialization
        init(application, configuration);
    }

    @Override
    protected String getDefaultFileExtension() {
        return JADE;
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        model.put("pippo", new PippoHelper(getMessages(), language, locale, getRouter()));
        try (StringReader reader = new StringReader(templateContent)) {
            ReaderTemplateLoader stringTemplateLoader = new ReaderTemplateLoader(reader, "StringTemplate");

            JadeConfiguration stringTemplateConfiguration = new JadeConfiguration();
            stringTemplateConfiguration.setCaching(false);
            stringTemplateConfiguration.setTemplateLoader(stringTemplateLoader);
            stringTemplateConfiguration.setMode(configuration.getMode());
            stringTemplateConfiguration.setPrettyPrint(configuration.isPrettyPrint());

            JadeTemplate stringTemplate = configuration.getTemplate("StringTemplate");
            configuration.renderTemplate(stringTemplate, model, writer);
            writer.flush();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        model.put("pippo", new PippoHelper(getMessages(), language, locale, getRouter()));
        try {
            JadeTemplate template = configuration.getTemplate(templateName);
            configuration.renderTemplate(template, model, writer);
            writer.flush();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    /**
     * Override this method if you want to modify the template configuration.
     *
     * @param application
     * @param configuration
     */
    protected void init(Application application, JadeConfiguration configuration) {
    }

    private class ClassTemplateLoader implements TemplateLoader {

        private Class<?> clazz;
        private String pathPrefix;

        public ClassTemplateLoader(Class<?> clazz, String pathPrefix) {
            this.clazz = clazz;

            pathPrefix = pathPrefix.replace('\\', '/');
            // ensure there's a trailing slash
            if (pathPrefix.length() > 0 && !pathPrefix.endsWith("/")) {
                pathPrefix += "/";
            }

            this.pathPrefix = pathPrefix;
        }

        @Override
        public long getLastModified(String name) {
            return -1;
        }

        @Override
        public Reader getReader(String name) throws IOException {
            if (name.indexOf('.') == -1) {
                name += "." + getFileExtension();
            }

            String fullPath = pathPrefix + name;

            return new InputStreamReader(clazz.getResourceAsStream(fullPath), PippoConstants.UTF8);
        }

    }

}
