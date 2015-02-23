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

import ro.pippo.core.Languages;
import ro.pippo.core.PippoConstants;
import ro.pippo.core.RouteContext;
import ro.pippo.core.util.StringUtils;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The RequestLanguageFilter determines the appropriate language, binds the lang
 * and locale Response models, and continues the handler chain.
 *
 * @author James Moger
 */
public class RequestLanguageFilter implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestLanguageFilter.class);

    protected final Languages languages;
    protected final boolean enableQueryParameter;

    /**
     * Create the language filter with optional support for accepting the
     * language specification from a query parameter (e.g. "?lang=LN")
     *
     * @param languages
     * @param enableQueryParameter
     */
    public RequestLanguageFilter(Languages languages, boolean enableQueryParameter) {
        this.languages = languages;
        this.enableQueryParameter = enableQueryParameter;
    }

    @Override
    public void handle(RouteContext routeContext, RouteHandlerChain chain) {
        String language = enableQueryParameter ? routeContext.getRequest().getParameter(PippoConstants.REQUEST_PARAMETER_LANG).toString()
                : null;

        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(routeContext);
        }
        Locale locale = languages.getLocaleOrDefault(language);

        routeContext.getResponse().bind(PippoConstants.REQUEST_PARAMETER_LANG, language);
        routeContext.getResponse().bind(PippoConstants.REQUEST_PARAMETER_LOCALE, locale);

        chain.next();
    }

}
