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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import ro.fortsoft.pippo.core.Languages;
import ro.fortsoft.pippo.core.Messages;
import ro.fortsoft.pippo.core.PippoConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.PippoSettings;
import ro.fortsoft.pippo.core.TemplateEngine;
import ro.fortsoft.pippo.core.route.UrlBuilder;
import ro.fortsoft.pippo.core.util.StringUtils;
import de.neuland.jade4j.Jade4J.Mode;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;

/**
 * @author Decebal Suiu
 */
public class JadeTemplateEngine implements TemplateEngine {

    private Languages languages;
    private Messages messages;
    private UrlBuilder urlBuilder;
    private JadeConfiguration configuration;

    @Override
    public void init(PippoSettings pippoSettings, Languages languages, Messages messages, UrlBuilder urlBuilder) {
        this.languages = languages;
        this.messages = messages;
        this.urlBuilder = urlBuilder;

        String pathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, DEFAULT_PATH_PREFIX);
        configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new ClassTemplateLoader(JadeTemplateEngine.class, pathPrefix));
        configuration.setMode(Mode.HTML);
        if (pippoSettings.isDev()) {
            configuration.setPrettyPrint(true);
            configuration.setCaching(false); // disable cache
        }

        // set global template variables
        configuration.getSharedVariables().put("contextPath", urlBuilder.getContextPath());
    }

    public JadeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(null);
        }

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }

        model.put("pippo", new PippoHelper(messages, language, locale, urlBuilder));
        try {
            JadeTemplate template = configuration.getTemplate(templateName);
            configuration.renderTemplate(template, model, writer);
            writer.flush();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    private static class ClassTemplateLoader implements TemplateLoader {

        private static final String suffix = ".jade";

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
            if (!name.endsWith(suffix)) {
                name += suffix;
            }

            String fullPath = pathPrefix + name;

            return new InputStreamReader(clazz.getResourceAsStream(fullPath), PippoConstants.UTF8);
        }

    }

}
