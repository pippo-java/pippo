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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Languages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.util.StringUtils;

import java.util.Locale;

/**
 * The {@code LanguageHandler} determines the appropriate language, binds the lang
 * and locale Response models, and continues the handler chain.
 *
 * @author James Moger
 */
public class LanguageHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(LanguageHandler.class);

    protected final Languages languages;
    protected final boolean enableQueryParameter;
    protected final boolean setCookie;

    /**
     * Create the language filter with optional support for accepting the
     * language specification from a query parameter (e.g. "?lang=LN")
     *
     * @param languages
     * @param enableQueryParameter
     * @param setCookie
     */
    public LanguageHandler(Languages languages, boolean enableQueryParameter, boolean setCookie) {
        this.languages = languages;
        this.enableQueryParameter = enableQueryParameter;
        this.setCookie = setCookie;
    }

    @Override
    public void handle(RouteContext routeContext) {
        String language = enableQueryParameter ? routeContext.getParameter(PippoConstants.REQUEST_PARAMETER_LANG).toString()
            : null;

        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(routeContext);
        }
        Locale locale = languages.getLocaleOrDefault(language);

        routeContext.setLocal(PippoConstants.REQUEST_PARAMETER_LANG, language);
        routeContext.setLocal(PippoConstants.REQUEST_PARAMETER_LOCALE, locale);

        if (setCookie) {
            if (routeContext.getResponse().isCommitted()) {
                log.debug("LANG cookie NOT set, Response already committed!");
            } else {
                languages.setLanguageCookie(language, routeContext);
            }
        }

        routeContext.next();
    }

}
