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
package ro.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.util.StringUtils;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Languages class manages the language and Locale of a Request & Response
 * cycle. It can optionally persist a Language preference as a cookie.
 * <p/>
 * This class is based on LangImpl.java from the Ninja Web Framework.
 *
 * @author James Moger
 */
public class Languages {

    private static final Logger log = LoggerFactory.getLogger(Languages.class);

    private final String applicationCookiePrefix;

    private static final int TEN_YEARS = 60 * 60 * 24 * 365 * 10;

    private final PippoSettings pippoSettings;

    private final String defaultLanguage;

    private final Map<String, String> languageLookup;

    public Languages(PippoSettings pippoSettings) {
        this.pippoSettings = pippoSettings;

        this.applicationCookiePrefix = pippoSettings.getString(
            PippoConstants.SETTING_APPLICATION_COOKIE_PREFIX, "PIPPO");

        // build a lookup of supported languages
        this.languageLookup = new ConcurrentHashMap<>();
        List<String> languages = pippoSettings
            .getStrings(PippoConstants.SETTING_APPLICATION_LANGUAGES);

        for (String language : languages) {
            String lang = language.toLowerCase();
            String langComponent = getLanguageComponent(lang);
            languageLookup.put(lang, language);
            languageLookup.put(langComponent, language);
        }

        this.defaultLanguage = getDefaultLanguage(languages);
        log.info("Using '{}' as default language.", defaultLanguage);
    }

    /**
     * Returns the language component of a language string.
     *
     * @param language
     * @return the language component
     */
    public String getLanguageComponent(String language) {
        if (StringUtils.isNullOrEmpty(language)) {
            return "";
        }

        if (language.contains("-")) {
            return language.split("-")[0];
        } else if (language.contains("_")) {
            return language.split("_")[0];
        }

        return language;
    }

    /**
     * Returns true if the language component is supported by this application.
     * <p>
     * For example, this application may have Portuguese (pt) as a registered
     * language but not Brazilian Portuguese (pt-BR). In this case, pt-BR would
     * be supported because of the pt registration.
     * </p>
     *
     * @param language
     * @return true if the language is supported
     */
    public boolean isSupportedLanguage(String language) {
        String lang = getLanguageComponent(language);
        return languageLookup.containsKey(lang);
    }

    /**
     * Returns true if the exact language is a registered language in this
     * application.
     * <p>
     * For example, this application may have a Portuguese translation
     * (messages_pt.properties) but not a Brazilian Portuguese translation
     * (messages_pt-BR.properties). In this case, pt-BR is not a registered
     * language, though it may be a supported language by the 'pt' language
     * component.
     * </p>
     *
     * @param language
     * @return true if the language is registered
     */
    public boolean isRegisteredLanguage(String language) {
        String lang = getLanguageOrDefault(language);
        return getRegisteredLanguages().contains(lang);
    }

    /**
     * Returns the list of registered languages.
     *
     * @return a list of registered languages
     */
    public List<String> getRegisteredLanguages() {
        return pippoSettings.getStrings(PippoConstants.SETTING_APPLICATION_LANGUAGES);
    }

    /**
     * Clears the application language cookie.
     *
     * @param response
     */
    public void clearLanguageCookie(Response response) {
        String cookieName = generateLanguageCookie("").getName();
        response.removeCookie(cookieName);
    }

    /**
     * Sets the application language cookie.
     * <p>
     * If the language does not match a registered language or language
     * component an exception is thrown.
     * </p>
     *
     * @param language
     * @param routeContext
     * @throws PippoRuntimeException
     */
    public void setLanguageCookie(String language, RouteContext routeContext) {
        String lang = getLanguageOrDefault(language);
        if (lang.equals(language)) {
            Cookie cookie = generateLanguageCookie(language);
            routeContext.getResponse().cookie(cookie);
        } else {
            throw new PippoRuntimeException("'{}' is not a registered language!", language);
        }
    }

    /**
     * Returns the language for the request. This process considers Request &
     * Response cookies, the Request ACCEPT_LANGUAGE header, and finally the
     * application default language.
     *
     * @param routeContext
     * @return the language for the request
     */
    public String getLanguageOrDefault(RouteContext routeContext) {
        final String cookieName = generateLanguageCookie(defaultLanguage).getName();

        // Step 1: Look for a Response cookie.
        // The Response always has priority over the Request because it may have
        // been set earlier in the HandlerChain.
        Cookie cookie = routeContext.getResponse().getCookie(cookieName);
        if (cookie != null && !StringUtils.isNullOrEmpty(cookie.getValue())) {
            return getLanguageOrDefault(cookie.getValue());
        }

        // Step 2: Look for a Request cookie.
        cookie = routeContext.getRequest().getCookie(cookieName);
        if (cookie != null && !StringUtils.isNullOrEmpty(cookie.getValue())) {
            return getLanguageOrDefault(cookie.getValue());
        }

        // Step 3: Look for a lang parameter in the response locals
        if (routeContext.getResponse().getLocals().containsKey(PippoConstants.REQUEST_PARAMETER_LANG)) {
            String language = routeContext.getLocal(PippoConstants.REQUEST_PARAMETER_LANG);
            language = getLanguageOrDefault(language);
            return language;
        }

        // Step 4: Look for a language in the Accept-Language header.
        String acceptLanguage = routeContext.getHeader(HttpConstants.Header.ACCEPT_LANGUAGE);
        return getLanguageOrDefault(acceptLanguage);
    }

    /**
     * Returns the Java Locale for the Request & Response cycle. If the language
     * specified in the Request/Response cycle can not be mapped to a Java
     * Locale, the default language Locale is returned.
     *
     * @param routeContext
     * @return a Java Locale
     */
    public Locale getLocaleOrDefault(RouteContext routeContext) {
        String language = getLanguageOrDefault(routeContext);
        return Locale.forLanguageTag(language);
    }

    /**
     * Returns the Java Locale for the specified language or the Locale for the
     * default language if the requested language can not be mapped to a Locale.
     *
     * @param language
     * @return a Java Locale
     */
    public Locale getLocaleOrDefault(String language) {
        String lang = getLanguageOrDefault(language);
        return Locale.forLanguageTag(lang);
    }

    /**
     * Returns a registered language if one can be matched from the input string
     * OR returns the default language. The input string may be a simple
     * language or locale value or may be as complex as an ACCEPT-LANGUAGE
     * header.
     *
     * @param language
     * @return the language or the default language
     */
    public String getLanguageOrDefault(String language) {
        if (!StringUtils.isNullOrEmpty(language)) {
            // Check if we get a registered mapping for the language input
            // string. The language may be either 'language-country' or
            // 'language'.
            String[] languages = language.toLowerCase().split(",");

            for (String lang : languages) {
                // Ignore the relative quality factor in Accept-Language header
                if (lang.contains(";")) {
                    lang = lang.split(";")[0];
                }

                if (isSupportedLanguage(lang)) {
                    return lang;
                }
            }
        }

        return defaultLanguage;
    }

    /**
     * Generates a language cookie with a very long max age (ten years).
     *
     * @param language
     * @return The cookie
     */
    private Cookie generateLanguageCookie(String language) {
        Cookie cookie = new Cookie(applicationCookiePrefix + "_LANG", language);
        cookie.setSecure(true);
        cookie.setMaxAge(TEN_YEARS);
        return cookie;
    }

    /**
     * Returns the default language as derived from PippoSettings.
     *
     * @param applicationLanguages
     * @return the default language
     */
    private String getDefaultLanguage(List<String> applicationLanguages) {
        if (applicationLanguages.isEmpty()) {
            String NO_LANGUAGES_TEXT = "Please specify the supported languages in 'application.properties'."
                + " For example 'application.languages=en, ro, de, pt-BR' makes 'en' your default language.";

            log.error(NO_LANGUAGES_TEXT);
            return "en";
        }

        // the first language specified is the default language
        return applicationLanguages.get(0);
    }

}
