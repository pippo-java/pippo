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
package ro.pippo.freemarker;

import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import org.kohsuke.MetaInfServices;
import ro.pippo.core.AbstractTemplateEngine;
import ro.pippo.core.Application;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
@MetaInfServices(TemplateEngine.class)
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

    public static final String FTL = "ftl";

    private WebjarsAtMethod webjarResourcesMethod;
    private PublicAtMethod publicResourcesMethod;
    private Configuration configuration;

    static {
        try {
            Logger.selectLoggerLibrary(Logger.LIBRARY_SLF4J);
        } catch (ClassNotFoundException e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void init(Application application) {
        super.init(application);

        Router router = getRouter();
        PippoSettings pippoSettings = getPippoSettings();

        configuration = new Configuration(Configuration.VERSION_2_3_21);
        configuration.setDefaultEncoding(PippoConstants.UTF8);
        configuration.setOutputEncoding(PippoConstants.UTF8);
        configuration.setLocalizedLookup(true);
        configuration.setClassForTemplateLoading(FreemarkerTemplateEngine.class, getTemplatePathPrefix());

        // We also do not want Freemarker to chose a platform dependent
        // number formatting. Eg "1000" could be printed out by FTL as "1,000"
        // on some platforms.
        // See also:
        // http://freemarker.sourceforge.net/docs/app_faq.html#faq_number_grouping
        configuration.setNumberFormat("0.######"); // now it will print 1000000

        if (pippoSettings.isDev()) {
            configuration.setTemplateUpdateDelayMilliseconds(0); // disable cache
        } else {
            // never update the templates in production or while testing...
            configuration.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);

            // Hold 20 templates as strong references as recommended by:
            // http://freemarker.sourceforge.net/docs/pgui_config_templateloading.html
            configuration.setCacheStorage(new freemarker.cache.MruCacheStorage(20, Integer.MAX_VALUE));
        }

        // set global template variables
        configuration.setSharedVariable("contextPath", new SimpleScalar(router.getContextPath()));
        configuration.setSharedVariable("appPath", new SimpleScalar(router.getApplicationPath()));

        webjarResourcesMethod = new WebjarsAtMethod(router);
        publicResourcesMethod = new PublicAtMethod(router);

        // allow custom initialization
        init(application, configuration);
    }

    @Override
    protected String getDefaultFileExtension() {
        return FTL;
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        // prepare the locale-aware i18n method
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }
        model.put("i18n", new I18nMethod(getMessages(), language));

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }
        model.put("prettyTime", new PrettyTimeMethod(locale));
        model.put("formatTime", new FormatTimeMethod(locale));
        model.put("webjarsAt", webjarResourcesMethod);
        model.put("publicAt", publicResourcesMethod);

        try {
            Template template = new Template("StringTemplate", templateContent, configuration);
            template.process(model, writer);
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
        model.put("i18n", new I18nMethod(getMessages(), language));

        // prepare the locale-aware prettyTime method
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }
        model.put("prettyTime", new PrettyTimeMethod(locale));
        model.put("formatTime", new FormatTimeMethod(locale));
        model.put("webjarsAt", webjarResourcesMethod);
        model.put("publicAt", publicResourcesMethod);

        try {
            if (templateName.indexOf('.') == -1) {
                templateName += "." + getFileExtension();
            }
            Template template = configuration.getTemplate(templateName, locale);
            template.process(model, writer);
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
    protected void init(Application application, Configuration configuration) {
    }

}
