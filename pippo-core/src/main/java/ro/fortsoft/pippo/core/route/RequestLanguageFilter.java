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
package ro.fortsoft.pippo.core.route;

import ro.fortsoft.pippo.core.Languages;
import ro.fortsoft.pippo.core.PippoConstants;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.util.StringUtils;

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
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        String language = enableQueryParameter ? request.getParameter(PippoConstants.REQUEST_PARAMETER_LANG).toString()
                : null;

        if (StringUtils.isNullOrEmpty(language)) {
            language = languages.getLanguageOrDefault(request, response);
        }
        Locale locale = languages.getLocaleOrDefault(language);

        response.bind(PippoConstants.REQUEST_PARAMETER_LANG, language);
        response.bind(PippoConstants.REQUEST_PARAMETER_LOCALE, locale);

        chain.next();
    }

}
