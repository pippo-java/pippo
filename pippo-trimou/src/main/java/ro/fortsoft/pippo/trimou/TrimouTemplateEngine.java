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
package ro.fortsoft.pippo.trimou;

import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.handlebars.HelpersBuilder;
import org.trimou.handlebars.i18n.DateTimeFormatHelper;
import org.trimou.minify.Minify;
import org.trimou.prettytime.PrettyTimeHelper;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Languages;
import ro.fortsoft.pippo.core.PippoConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.PippoSettings;
import ro.fortsoft.pippo.core.TemplateEngine;
import ro.fortsoft.pippo.core.route.UrlBuilder;
import ro.fortsoft.pippo.core.util.StringUtils;

/**
 * Trimou template engine for Pippo.
 *
 * @author James Moger
 */
public class TrimouTemplateEngine extends TemplateEngine {

    private static final Logger log = LoggerFactory.getLogger(TrimouTemplateEngine.class);

    public static final String MUSTACHE = "mustache";
    public static final String FILE_SUFFIX = "." + MUSTACHE;

    private Languages languages;
    private ThreadLocalLocaleSupport localeSupport;
    private MustacheEngine engine;

    @Override
    public void init(Application application) {
        this.languages = application.getLanguages();
        this.localeSupport = new ThreadLocalLocaleSupport();

        UrlBuilder urlBuilder = application.getUrlBuilder();
        PippoSettings pippoSettings = application.getPippoSettings();

        MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
        builder.setLocaleSupport(localeSupport);
        builder.setProperty(EngineConfigurationKey.DEFAULT_FILE_ENCODING, PippoConstants.UTF8);
        builder.registerHelper("ng", new AngularJsHelper());
        builder.registerHelper("i18n", new I18nHelper(application.getMessages()));
        builder.registerHelper("formatTime", new DateTimeFormatHelper());
        builder.registerHelper("prettyTime", new PrettyTimeHelper());
        builder.registerHelper("webjarsAt", new WebjarsAtHelper(urlBuilder));
        builder.registerHelper("publicAt", new PublicAtHelper(urlBuilder));
        builder.registerHelpers(HelpersBuilder.extra().build());

        String pathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, DEFAULT_PATH_PREFIX);
        pathPrefix = StringUtils.removeStart(pathPrefix, "/");
        builder.addTemplateLocator(new PippoTemplateLocator(10, pathPrefix));

        if (pippoSettings.isDev()) {
            // enable debug mode
            builder.setProperty(EngineConfigurationKey.DEBUG_MODE, true);
        } else {
            // automatically minify pages generated in production/test
            builder.addMustacheListener(Minify.htmlListener());
        }

        // set global template variables
        builder.addGlobalData("contextPath", urlBuilder.getContextPath());

        engine = builder.build();
    }

    @Override
    public void render(String templateName, Map<String, Object> model, Writer writer) {
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);
        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(null);
        }

        // prepare the locale
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = languages.getLocaleOrDefault(language);
        }

        try {
            Mustache template = null;

            localeSupport.setCurrentLocale(locale);

            templateName = StringUtils.removeEnd(templateName, FILE_SUFFIX);

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

    private String getLocalizedTemplateName(String templateName, String localePart) {
        return StringUtils.removeEnd(templateName, FILE_SUFFIX) + "_" + localePart;
    }

}
