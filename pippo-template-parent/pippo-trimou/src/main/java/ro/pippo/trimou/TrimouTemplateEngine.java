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
package ro.pippo.trimou;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.handlebars.HelpersBuilder;
import org.trimou.handlebars.i18n.DateTimeFormatHelper;
import org.trimou.minify.Minify;
import org.trimou.prettytime.PrettyTimeHelper;
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
 * Trimou template engine for Pippo.
 *
 * @author James Moger
 */
@MetaInfServices(TemplateEngine.class)
public class TrimouTemplateEngine extends AbstractTemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TrimouTemplateEngine.class);

    public static final String MUSTACHE = "mustache";

    private ThreadLocalLocaleSupport localeSupport;
    private MustacheEngine engine;

    @Override
    public void init(Application application) {
        super.init(application);

        this.localeSupport = new ThreadLocalLocaleSupport();

        Router router = getRouter();
        PippoSettings pippoSettings = getPippoSettings();

        MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
        builder.setLocaleSupport(localeSupport);
        builder.setProperty(EngineConfigurationKey.DEFAULT_FILE_ENCODING, PippoConstants.UTF8);
        builder.registerHelper("ng", new AngularJsHelper());
        builder.registerHelper("i18n", new I18nHelper(application.getMessages()));
        builder.registerHelper("formatTime", new DateTimeFormatHelper());
        builder.registerHelper("prettyTime", new PrettyTimeHelper());
        builder.registerHelper("webjarsAt", new WebjarsAtHelper(router));
        builder.registerHelper("publicAt", new PublicAtHelper(router));
        builder.registerHelpers(HelpersBuilder.extra().build());

        String pathPrefix = getTemplatePathPrefix();
        pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        builder.addTemplateLocator(new ClassPathTemplateLocator(10, pathPrefix, MUSTACHE));

        if (pippoSettings.isDev()) {
            // enable debug mode
            builder.setProperty(EngineConfigurationKey.DEBUG_MODE, true);
        } else {
            // automatically minify pages generated in production/test
            builder.addMustacheListener(Minify.htmlListener());
        }

        // set global template variables
        builder.addGlobalData("contextPath", router.getContextPath());
        builder.addGlobalData("appPath", router.getApplicationPath());

        // allow custom initialization
        init(application, builder);

        engine = builder.build();
    }

    @Override
    protected String getDefaultFileExtension() {
        return MUSTACHE;
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }

        // prepare the locale
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        try {
            localeSupport.setCurrentLocale(locale);
            Mustache template = engine.compileMustache("StringTemplate", templateContent);
            template.render(writer, model);
            writer.flush();
        } catch (Exception e) {
            if (e instanceof PippoRuntimeException) {
                throw (PippoRuntimeException) e;
            }
            throw new PippoRuntimeException(e);
        } finally {
            localeSupport.remove();
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }

        // prepare the locale
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        try {
            Mustache template = null;

            localeSupport.setCurrentLocale(locale);

            templateName = StringUtils.removeEnd(templateName, "." + getFileExtension());

            if (locale != null) {
                // try the complete Locale
                template = engine.getMustache(getLocalizedTemplateName(templateName, locale.toString()));
                if (template == null) {
                    // try only the language
                    template = engine.getMustache(getLocalizedTemplateName(templateName, locale.getLanguage()));
                }
            }

            if (template == null) {
                // fallback to the template without any language or locale
                template = engine.getMustache(templateName);
            }

            if (template == null) {
                throw new PippoRuntimeException("Template '{}' not found!", templateName);
            }

            template.render(writer, model);
            writer.flush();
        } catch (Exception e) {
            if (e instanceof PippoRuntimeException) {
                throw (PippoRuntimeException) e;
            }
            throw new PippoRuntimeException(e);
        } finally {
            localeSupport.remove();
        }
    }

    /**
     * Override this method if you want to modify the template configuration.
     *
     * @param application
     * @param builder
     */
    protected void init(Application application, MustacheEngineBuilder builder) {
    }

    private String getLocalizedTemplateName(String templateName, String localePart) {
        return StringUtils.removeEnd(templateName, "." + getFileExtension()) + "_" + localePart;
    }

}
