/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.freemarker;

import freemarker.log.Logger;
import freemarker.template.Configuration;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.RuntimeMode;
import ro.fortsoft.pippo.core.TemplateEngine;

import java.io.Writer;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class FreemarkerTemplateEngine implements TemplateEngine {

    private static final String defaultPathPrefix = "/templates";

    private Configuration configuration;

    static {
        try {
            Logger.selectLoggerLibrary(Logger.LIBRARY_SLF4J);
        } catch (ClassNotFoundException e) {
            throw new PippoRuntimeException(e);
        }
    }

    public FreemarkerTemplateEngine() {
        this(defaultPathPrefix);
    }

    public FreemarkerTemplateEngine(String pathPrefix) {
        configuration = new Configuration();
        configuration.setClassForTemplateLoading(FreemarkerTemplateEngine.class, pathPrefix);
        if (RuntimeMode.getCurrent() == RuntimeMode.DEV) {
            configuration.setTemplateUpdateDelay(0); // disable cache
        }
    }

    public FreemarkerTemplateEngine(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        try {
            configuration.getTemplate(templateName).process(model, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
