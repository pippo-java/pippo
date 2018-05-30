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
import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Loads and caches message resource files based on the registered languages in
 * application.properties.
 * <p/>
 * This class is based on MessagesImpl.java from the Ninja Web Framework.
 *
 * @author James Moger
 */
public class Messages {

    private static final Logger log = LoggerFactory.getLogger(Messages.class);

    private final Map<String, Properties> languageMessages;

    private final Languages languages;

    public Messages(Languages languages) {
        this.languages = languages;
        this.languageMessages = loadRegisteredMessageResources();
    }

    /**
     * Gets the requested localized message.
     * <p/>
     * <p>
     * The current Request and Response are used to help determine the messages
     * resource to use.
     * <ol>
     * <li>Exact locale match, return the registered locale message
     * <li>Language match, but not a locale match, return the registered
     * language message
     * <li>Return the default resource message
     * </ol>
     * <p>
     * The message can be formatted with optional arguments using the
     * {@link java.text.MessageFormat} syntax.
     * </p>
     * <p>
     * If the key does not exist in the messages resource, then the key name is
     * returned.
     * </p>
     *
     * @param key
     * @param routeContext
     * @param args
     * @return the message or the key if the key does not exist
     */
    public String get(String key, RouteContext routeContext, Object... args) {
        String language = languages.getLanguageOrDefault(routeContext);
        return get(key, language, args);
    }

    /**
     * Gets the requested localized message.
     * <p/>
     * <ol>
     * <li>Exact locale match, return the registered locale message
     * <li>Language match but not a locale match, return the registered language
     * message
     * <li>Return the default resource message
     * </ol>
     * <p>
     * The message can be formatted with optional arguments using the
     * {@link java.text.MessageFormat} syntax.
     * </p>
     * <p>
     * If the key does not exist in the messages resource, then the key name is
     * returned.
     * </p>
     *
     * @param key
     * @param language
     * @param args
     * @return the message or the key if the key does not exist
     */
    public String get(String key, String language, Object... args) {
        Properties messages = getMessagesForLanguage(language);
        String value = messages.getProperty(key);
        if (value != null) {
            return formatMessage(value, language, args);
        } else {
            log.warn("Failed to find '{}' in Messages", key);
            return key;
        }
    }

    /**
     * Gets the requested localized message.
     * <p/>
     * <p>
     * The current Request and Response are used to help determine the messages
     * resource to use.
     * <ol>
     * <li>Exact locale match, return the registered locale message
     * <li>Language match, but not a locale match, return the registered
     * language message
     * <li>Return the supplied default message
     * </ol>
     * <p>
     * The message can be formatted with optional arguments using the
     * {@link java.text.MessageFormat} syntax.
     * </p>
     * <p>
     * If the key does not exist in the messages resource, then the key name is
     * returned.
     * </p>
     *
     * @param key
     * @param defaultMessage
     * @param routeContext
     * @param args
     * @return the message or the key if the key does not exist
     */
    public String getWithDefault(String key, String defaultMessage, RouteContext routeContext, Object... args) {
        String language = languages.getLanguageOrDefault(routeContext);
        return getWithDefault(key, defaultMessage, language, args);
    }

    /**
     * Gets the requested localized message.
     * <p/>
     * <p>
     * The current Request and Response are used to help determine the messages
     * resource to use.
     * <ol>
     * <li>Exact locale match, return the registered locale message
     * <li>Language match, but not a locale match, return the registered
     * language message
     * <li>Return supplied default message
     * </ol>
     * <p>
     * The message can be formatted with optional arguments using the
     * {@link java.text.MessageFormat} syntax.
     * </p>
     *
     * @param key
     * @param defaultMessage
     * @param args
     * @return the message or the key if the key does not exist
     */
    public String getWithDefault(String key, String defaultMessage, String language, Object... args) {
        String value = get(key, language, args);
        if (value.equals(key)) {
            // key does not exist, format default message
            value = formatMessage(defaultMessage, language, args);
        }

        return value;
    }

    /**
     * Returns all localized messages.
     * <p>
     * The current Request and Response are used to help determine the messages
     * resource to use.
     * <ol>
     * <li>Exact locale match, return the registered locale messages
     * <li>Language match but not a locale match, return the registered language
     * messages
     * <li>Return the default messages
     * </ol>
     * </p>
     *
     * @param routeContext
     * @return all localized messages
     */
    public Map<String, String> getAll(RouteContext routeContext) {
        String language = languages.getLanguageOrDefault(routeContext);
        return getAll(language);
    }

    /**
     * Returns all localized messages.
     * <ol>
     * <li>Exact locale match, return the registered locale messages
     * <li>Language match but not a locale match, return the registered language
     * messages
     * <li>Return the default messages
     * </ol>
     *
     * @param language
     * @return all localized messages
     */
    public Map<String, String> getAll(String language) {
        Properties messages = getMessagesForLanguage(language);
        Map<String, String> map = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : messages.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return map;
    }

    /**
     * Loads Pippo internal messages & application messages and returns the merger.
     *
     * @return all messages
     */
    private Map<String, Properties> loadRegisteredMessageResources() {
        Map<String, Properties> internalMessages = loadRegisteredMessageResources("pippo/pippo-messages%s.properties");
        Map<String, Properties> applicationMessages = loadRegisteredMessageResources("conf/messages%s.properties");
        Map<String, Properties> allMessages = new TreeMap<>();

        Set<String> merged = new HashSet<>();
        // create aggregate messages
        for (Map.Entry<String, Properties> entry : internalMessages.entrySet()) {
            String language = entry.getKey();
            Properties messages = entry.getValue();
            allMessages.put(language, messages);

            if (applicationMessages.containsKey(language)) {
                // override internal messages with application messages
                messages.putAll(applicationMessages.get(language));
            }

            merged.add(language);
        }

        // bring in the application languages which do not have an internal counterpart
        Set<String> unmerged = new HashSet<>(applicationMessages.keySet());
        unmerged.removeAll(merged);
        for (String language : unmerged) {
            allMessages.put(language, applicationMessages.get(language));
        }

        return allMessages;
    }

    /**
     * Loads all registered message resources.
     */
    private Map<String, Properties> loadRegisteredMessageResources(String name) {
        Map<String, Properties> messageResources = new TreeMap<>();

        // Load default messages
        Properties defaultMessages = loadMessages(String.format(name, ""));
        if (defaultMessages == null) {
            log.error("Could not locate the default messages resource '{}', please create it.", String.format(name, ""));
        } else {
            messageResources.put("", defaultMessages);
        }

        // Load the registered language resources
        List<String> registeredLanguages = languages.getRegisteredLanguages();
        for (String language : registeredLanguages) {
            // First step: Load complete language eg. en-US
            Properties messages = loadMessages(String.format(name, "_" + language));

            Properties messagesLangOnly = null;

            // If the language has a country code load the default values for
            // the language. For example missing keys in en-US will
            // be filled-in by the default language.
            String langComponent = languages.getLanguageComponent(language);
            if (!langComponent.equals(language)) {
                // see if we have already loaded the language messages
                messagesLangOnly = messageResources.get(langComponent);
                if (messagesLangOnly == null) {
                    // load the language messages
                    messagesLangOnly = loadMessages(String.format(name, "_" + langComponent));
                }
            }

            // If a language is registered in application.properties it should
            // be there.
            if (messages == null) {
                log.error(
                    "Could not locate the '{}' messages resource '{}' specified in '{}'.",
                    language, String.format(name, "_" + language),
                    PippoConstants.SETTING_APPLICATION_LANGUAGES);
            } else {
                // add a new language

                // start with the default messages
                Properties compositeMessages = new Properties(defaultMessages);

                // put all the language component messages "en"
                if (messagesLangOnly != null) {
                    compositeMessages.putAll(messagesLangOnly);

                    // cache language component messages
                    if (!messageResources.containsKey(langComponent)) {
                        Properties langResources = new Properties();
                        langResources.putAll(compositeMessages);
                        messageResources.put(langComponent, langResources);
                    }
                }

                // put all the language specific messages "en-US"
                compositeMessages.putAll(messages);

                // and add the composite messages to the hashmap with the
                // mapping.
                messageResources.put(language.toLowerCase(), compositeMessages);
            }
        }

        return Collections.unmodifiableMap(messageResources);
    }

    /**
     * Attempts to load a message resource.
     */
    private Properties loadMessages(String fileOrUrl) {
        URL url = ClasspathUtils.locateOnClasspath(fileOrUrl);
        if (url != null) {
            try (InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                Properties messages = new Properties();
                messages.load(reader);
                return messages;
            } catch (IOException e) {
                log.error("Failed to load {}", fileOrUrl, e);
            }
        }

        return null;
    }

    /**
     * Retrieves the messages from an arbitrary one or two component language
     * String ("en-US", or "en" or "de"...).
     * <p/>
     *
     * @param language A one or two component language code such as "en", "en-US", or
     *                 "en-US,en;q=0.8,de;q=0.6".
     * @return The messages for the requested language or the default messages.
     */
    private Properties getMessagesForLanguage(String language) {
        if (StringUtils.isNullOrEmpty(language)) {
            return languageMessages.get("");
        }

        String supportedLanguage = languages.getLanguageOrDefault(language);
        if (StringUtils.isNullOrEmpty(supportedLanguage)) {
            log.debug("Messages for '{}' were requested. Using default messages.", language);
            return languageMessages.get("");
        }

        // try the supported language
        Properties messages = languageMessages.get(supportedLanguage);
        if (messages != null) {
            return messages;
        }

        // check the supported language component
        String langComponent = languages.getLanguageComponent(supportedLanguage);

        messages = languageMessages.get(langComponent);
        if (messages != null) {
            return messages;
        }

        // return the default messages resource
        return languageMessages.get("");
    }

    /**
     * Optionally formats a message for the requested language with
     * {@link java.text.MessageFormat}.
     *
     * @param message
     * @param language
     * @param args
     * @return the message
     */
    private String formatMessage(String message, String language, Object... args) {
        if (args != null && args.length > 0) {
            // only format a message if we have arguments
            Locale locale = languages.getLocaleOrDefault(language);
            MessageFormat messageFormat = new MessageFormat(message, locale);
            return messageFormat.format(args);
        }

        return message;
    }

}
