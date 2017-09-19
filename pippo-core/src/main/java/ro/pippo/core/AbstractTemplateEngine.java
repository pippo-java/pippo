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

import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.Router;

import java.util.Locale;

/**
 * Convenience abstract implementation of {@link TemplateEngine}
 *
 * To use the convenience methods in this class, implementations must override {@link #init(Application)}
 * and call this class' implementation before performing any of their initialization.
 *
 * @see TemplateEngine
 *
 * @author Ranganath Kini
 */
public abstract class AbstractTemplateEngine implements TemplateEngine {

    private Languages languages;
    private Messages messages;
    private Router router;
    private PippoSettings pippoSettings;

    private String fileExtension;
    private String templatePathPrefix;

    /**
     * Performs common initialization for template engines.
     *
     * Implementations must override this method to do their own template engine specific initialization.
     * To use the convenience of this class, implementations must invoke this class's implementation before
     * performing their own initialization.
     *
     * @param application reference to the Pippo {@link Application} that can be used to retrieve settings
     *                     and other settings for the initialization
     */
    @Override
    public void init(Application application) {
        languages = application.getLanguages();
        messages = application.getMessages();
        router = application.getRouter();
        pippoSettings = application.getPippoSettings();

        fileExtension = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_EXTENSION, getDefaultFileExtension());
        templatePathPrefix = pippoSettings.getString(PippoConstants.SETTING_TEMPLATE_PATH_PREFIX, TemplateEngine.DEFAULT_PATH_PREFIX);
    }

    /**
     * Returns the default file extension for template resources.
     *
     * This will be used in case there is <code>template.extension</code> is not provided in
     * application settings.
     *
     * @return String the default file extension for templates resources
     */
    protected abstract String getDefaultFileExtension();

    /**
     * @see TemplateEngine#setFileExtension(String)
     */
    @Override
    public final void setFileExtension(String extension) {
        this.fileExtension = extension;
    }

    /**
     * Returns the configured file extension for template resources.
     *
     * @return String the configured file extension for template resources
     */
    protected final String getFileExtension() {
        return fileExtension;
    }

    /**
     * @see Application#getLanguages()
     */
    protected final Languages getLanguages() {
        return languages;
    }

    /**
     * @see Application#getMessages()
     */
    protected final Messages getMessages() {
        return this.messages;
    }

    /**
     * @see Application#getPippoSettings()
     */
    protected final PippoSettings getPippoSettings() {
        return pippoSettings;
    }

    /**
     * @see Languages#getLocaleOrDefault(String)
     */
    protected final Locale getLocaleOrDefault(String language) {
        return languages.getLocaleOrDefault(language);
    }

    /**
     * @see Languages#getLocaleOrDefault(RouteContext)
     */
    protected final Locale getLocaleOrDefault(RouteContext routeContext) {
        return languages.getLocaleOrDefault(routeContext);
    }

    /**
     * @see Languages#getLanguageOrDefault(String)
     */
    protected final String getLanguageOrDefault(String language) {
        return languages.getLanguageOrDefault(language);
    }

    /**
     * @see Languages#getLanguageOrDefault(RouteContext)
     */
    protected final String getLanguageOrDefault(RouteContext routeContext) {
        return languages.getLanguageOrDefault(routeContext);
    }

    /**
     * @see Application#getRouter()
     */
    protected final Router getRouter() {
        return router;
    }

    /**
     * Returns the template path prefix to be used to load template resources.
     *
     * @return String the template path prefix for loading template resources
     */
    protected final String getTemplatePathPrefix() {
        return templatePathPrefix;
    }

}
