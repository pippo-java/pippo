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
package ro.pippo.pebble;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.debug.DebugExtension;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.loader.StringLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.AbstractTemplateEngine;
import ro.pippo.core.Application;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.TemplateEngine;
import ro.pippo.core.route.Router;
import ro.pippo.core.util.StringUtils;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pebble template engine for Pippo.
 *
 * @author James Moger
 */
@MetaInfServices(TemplateEngine.class)
public class PebbleTemplateEngine extends AbstractTemplateEngine {

    private final Logger log = LoggerFactory.getLogger(PebbleTemplateEngine.class);

    private static final String PEBBLE = "peb";

    private PebbleEngine engine;

    @Override
    public void init(Application application) {
        super.init(application);

        Router router = getRouter();
        PippoSettings pippoSettings = getPippoSettings();

        List<Loader<?>> loaders = new ArrayList<>();
        PippoTemplateLoader templateLoader = new PippoTemplateLoader();

        templateLoader.setCharset(PippoConstants.UTF8);
        templateLoader.setPrefix(getTemplatePathPrefix());
        templateLoader.setSuffix("." + getFileExtension());
        loaders.add(templateLoader);

        PebbleEngine.Builder builder = new PebbleEngine.Builder()
            .loader(new DelegatingLoader(loaders))
            .strictVariables(false)
            .extension(new GlobalVariablesExtension()
                .set("contextPath", router.getContextPath())
                .set("appPath", router.getApplicationPath()))
            .extension(new I18nExtension(application.getMessages()))
            .extension(new FormatTimeExtension())
            .extension(new PrettyTimeExtension())
            .extension(new AngularJSExtension())
            .extension(new WebjarsAtExtension(router))
            .extension(new PublicAtExtension(router));

        if (pippoSettings.isDev()) {
            // do not cache templates in dev mode
            builder.cacheActive(false);
            builder.extension(new DebugExtension());
        }

        // allow custom initialization
        init(application, builder);

        engine = builder.build();
    }

    /**
     * Override this method if you want to modify the template configuration.
     *
     * @param application
     * @param configurationTarget
     */
    protected void init(Application application, PebbleEngine.Builder configurationTarget) {
    }

    @Override
    public void renderString(String templateContent, Map<String, Object> model, Writer writer) {
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);

        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }
        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        try {
            PebbleEngine stringEngine = new PebbleEngine.Builder()
                .loader(new StringLoader())
                .strictVariables(engine.isStrictVariables())
                .templateCache(null)
                .build();

            PebbleTemplate template = stringEngine.getTemplate(templateContent);
            template.evaluate(writer, model, locale);
            writer.flush();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    public void renderResource(String templateName, Map<String, Object> model, Writer writer) {
        String language = (String) model.get(PippoConstants.REQUEST_PARAMETER_LANG);

        if (StringUtils.isNullOrEmpty(language)) {
            language = getLanguageOrDefault(language);
        }

        Locale locale = (Locale) model.get(PippoConstants.REQUEST_PARAMETER_LOCALE);
        if (locale == null) {
            locale = getLocaleOrDefault(language);
        }

        try {
            PebbleTemplate template = null;

            if (locale != null) {
                // try the complete Locale
                template = getTemplate(templateName, locale.toString());
                if (template == null) {
                    // try only the language
                    template = getTemplate(templateName, locale.getLanguage());
                }
            }

            if (template == null) {
                // fallback to the template without any language or locale
                template = engine.getTemplate(templateName);
            }

            template.evaluate(writer, model, locale);
            writer.flush();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    @Override
    protected String getDefaultFileExtension() {
        return PEBBLE;
    }

    private PebbleTemplate getTemplate(String templateName, String localePart) throws PebbleException {
        PebbleTemplate template = null;
        try {
            if (StringUtils.isNullOrEmpty(localePart)) {
                template = engine.getTemplate(templateName);
            } else {
                String localizedName = StringUtils.removeEnd(templateName, "." +
                    getFileExtension()) + "_" + localePart;
                template = engine.getTemplate(localizedName);
            }
        } catch (LoaderException e) {
            log.debug(e.getMessage());
        }

        return template;
    }

}
