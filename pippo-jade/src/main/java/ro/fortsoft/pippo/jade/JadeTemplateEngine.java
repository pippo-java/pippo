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

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.RuntimeMode;
import ro.fortsoft.pippo.core.TemplateEngine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class JadeTemplateEngine implements TemplateEngine {

    private static final String defaultPathPrefix = "/templates";

    private JadeConfiguration configuration;

    public JadeTemplateEngine() {
        this(defaultPathPrefix);
    }

    public JadeTemplateEngine(String pathPrefix) {
        configuration = new JadeConfiguration();
        configuration.setTemplateLoader(new ClassTemplateLoader(JadeTemplateEngine.class, pathPrefix));
        configuration.setPrettyPrint(true);
        if (RuntimeMode.getCurrent() == RuntimeMode.DEV) {
            configuration.setCaching(false); // disable cache
        }
    }

    public JadeTemplateEngine(JadeConfiguration configuration) {
        this.configuration = configuration;
    }

    public JadeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        try {
            JadeTemplate template = configuration.getTemplate(templateName);
            configuration.renderTemplate(template, model, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    private static class ClassTemplateLoader implements TemplateLoader {

        private static final String suffix = ".jade";

        private Class clazz;
        private String pathPrefix;

        public ClassTemplateLoader(Class clazz, String pathPrefix) {
            this.clazz = clazz;

            pathPrefix = pathPrefix.replace('\\', '/');
            // ensure there's a trailing slash
            if (pathPrefix.length() > 0 && !pathPrefix.endsWith("/")) {
                pathPrefix += "/";
            }

            this.pathPrefix = pathPrefix;
        }

        public long getLastModified(String name) {
            return -1;
        }

        @Override
        public Reader getReader(String name) throws IOException {
            if (!name.endsWith(suffix)) {
                name += suffix;
            }

            String fullPath = pathPrefix + name;

            return new InputStreamReader(clazz.getResourceAsStream(fullPath), "UTF-8");
        }

    }

}
