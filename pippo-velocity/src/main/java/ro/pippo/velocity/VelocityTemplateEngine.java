/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import ro.pippo.core.Application;
import ro.pippo.core.Languages;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.util.StringUtils;

import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author Decebal Suiu
 */
public class VelocityTemplateEngine implements TemplateEngine {

    public static final String VM = "vm";
    public static final String FILE_SUFFIX = "." + VM;

    private Languages languages;
    private Messages messages;
    private VelocityEngine velocityEngine;

    @Override
    public void init(Application application) {
        this.languages = application.getLanguages();
        this.messages = application.getMessages();

        PippoSettings pippoSettings = application.getPippoSettings();

        String pathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, null);
        if (StringUtils.isNullOrEmpty(pathPrefix)) {
            pathPrefix = TemplateEngine.DEFAULT_PATH_PREFIX;
        }

        // create properties (see http://velocity.apache.org/engine/releases/velocity-1.7/developer-guide.html#Configuring_Resource_Loaders)
        // maybe we can found in classpath a file velocity.properties and load all properties from this file
        Properties properties = new Properties();
//        properties.setProperty("pathPrefix", pathPrefix);
//        properties.setProperty("resource.loader", "classpath");
//        properties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
//        properties.setProperty("classpath.resource.loader.prefixPath", pathPrefix);
//        properties.setProperty("classpath.resource.loader.cache", String.valueOf(!pippoSettings.isDev()));

        properties.setProperty("resource.loader", "myloader");
        properties.setProperty("myloader.resource.loader.class", PrefixedClasspathResourceLoader.class.getName());
        properties.setProperty("myloader.resource.loader.prefix", StringUtils.addEnd(pathPrefix, "/"));
        properties.setProperty("myloader.resource.loader.cache", String.valueOf(!pippoSettings.isDev()));

//        properties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, pathPrefix);
        // velocity.properties
//        resource.loader = file
//        file.resource.loader.class = org.apache.velocity.runtime.resource.loader.FileResourceLoader
//        file.resource.loader.path = c:/tomcat/webapps/velocity/WEB-INF/templates
//        file.resource.loader.cache = true
//        file.resource.loader.modificationCheckInterval = 2
//        properties.setProperty("input.encoding","UTF-8");
//        properties.setProperty("output.encoding","UTF-8");

        velocityEngine = new VelocityEngine(properties);
    }

    public VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(language);
        }
//        model.put("i18n", new I18nMethod(messages, language));

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }
//        model.put("prettyTime", new PrettyTimeMethod(locale));
//        model.put("formatTime", new FormatTimeMethod(locale));
//        model.put("webjarsAt", webjarResourcesMethod);
//        model.put("publicAt", publicResourcesMethod);

        try {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            StringReader reader = new StringReader(templateContent);
            SimpleNode node = runtimeServices.parse(reader, "StringTemplate");
            Template template = new Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
            VelocityContext context = new VelocityContext(model);
            template.merge(context, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(language);
        }
//        model.put("i18n", new I18nMethod(messages, language));

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }
//        model.put("prettyTime", new PrettyTimeMethod(locale));
//        model.put("formatTime", new FormatTimeMethod(locale));
//        model.put("webjarsAt", webjarResourcesMethod);
//        model.put("publicAt", publicResourcesMethod);

        try {
            if (templateName.indexOf('.') == -1) {
                templateName += FILE_SUFFIX;
            }
            // TODO locale ?!
            Template template = velocityEngine.getTemplate(templateName);
            VelocityContext context = new VelocityContext(model);
            template.merge(context, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
