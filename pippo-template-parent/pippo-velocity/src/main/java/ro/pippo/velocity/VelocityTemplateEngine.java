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
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.kohsuke.MetaInfServices;
import ro.pippo.core.Application;
import ro.pippo.core.Languages;
import ro.pippo.core.Messages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

import java.io.StringReader;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author Decebal Suiu
 */
@MetaInfServices
public class VelocityTemplateEngine implements TemplateEngine {

    public static final String VM = "vm";

    private Languages languages;
    private Messages messages;
    private Router router;
    private VelocityEngine velocityEngine;

    private String extension = VM;

    @Override
    public void init(Application application) {
        this.languages = application.getLanguages();
        this.messages = application.getMessages();
        this.router = application.getRouter();

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

        // allow custom initialization
        init(application, properties);

        velocityEngine = new VelocityEngine(properties);
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        // create the velocity context
        VelocityContext context = createVelocityContext(model);

        // merge the template
        try {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            StringReader reader = new StringReader(templateContent);
            SimpleNode node = runtimeServices.parse(reader, "StringTemplate");
            Template template = new Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
            template.merge(context, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        // add the file suffix if it's missing
        if (templateName.indexOf('.') == -1) {
            templateName += "." + extension;
        }

        // create the velocity context
        VelocityContext context = createVelocityContext(model);

        // merge the template
        try {
            Template template = velocityEngine.getTemplate(templateName);
            template.merge(context, writer);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void setFileExtension(String extension) {
        this.extension = extension;
    }

    protected void init(Application application, Properties properties) {
    }

    private VelocityContext createVelocityContext(Map<String, Object> model) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(language);
        }

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }

        VelocityContext context = new VelocityContext(model);

        context.put("pippo", new PippoHelper(messages, language, locale, router));
        context.put("contextPath", router.getContextPath());
        context.put("appPath", router.getApplicationPath());

        return context;
    }

}
