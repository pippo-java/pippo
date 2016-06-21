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
package ro.pippo.groovy;

import groovy.text.markup.MarkupTemplateEngine.TemplateResource;
import groovy.text.markup.TemplateConfiguration;
import groovy.text.markup.TemplateResolver;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ro.pippo.core.util.ClasspathUtils;

public class GroovyTemplateResolver implements TemplateResolver {

    private TemplateConfiguration templateConfiguration;
    protected final String pathPrefix;
    protected final Map<String, URL> cache;
    protected boolean useCache = false;

    /**
     * Creates a new caching template resolver. The cache implementation being
     * used depends on the use of the template engine. If multiple templates can
     * be rendered in parallel, it <b>must</b> be using a thread-safe cache.
     *
     * @param cache
     *            the backing cache
     */
    public GroovyTemplateResolver(final String pathPrefix, final Map<String, URL> cache) {
        this.pathPrefix = pathPrefix;
        this.cache = cache;
    }

    /**
     * Creates a new caching template resolver using a concurrent hash map as
     * the backing cache.
     */
    public GroovyTemplateResolver(String pathPrefix) {
        this(pathPrefix, new ConcurrentHashMap<String, URL>());
    }

    @Override
    public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
        this.templateConfiguration = configuration;
        this.useCache = configuration.isCacheTemplates();
    }

    @Override
    public URL resolveTemplate(final String templatePath) throws IOException {
        final String path = pathPrefix == null ? templatePath : (pathPrefix + "/" + templatePath);
        TemplateResource templateResource = TemplateResource.parse(path);
        String configurationLocale = templateConfiguration.getLocale().toString().replace("-", "_");
        URL resource = templateResource.hasLocale() ? getResource(templateResource.toString()) : null;
        if (resource == null) {
            // no explicit locale in the template path or resource not found
            // fallback to the default configuration locale
            resource = getResource(templateResource.withLocale(configurationLocale).toString());
        }
        if (resource == null) {
            // no resource found with the default locale, try without any locale
            resource = getResource(templateResource.withLocale(null).toString());
        }
        if (resource == null) {
            throw new IOException("Unable to load template:" + path);
        }

        if (useCache) {
            cache.put(templatePath, resource);
        }
        return resource;
    }

    protected URL getResource(String templatePath) {
        if (useCache) {
            URL cachedURL = cache.get(templatePath);
            if (cachedURL != null) {
                return cachedURL;
            }
        }
        URL resource = ClasspathUtils.locateOnClasspath(templatePath);
        return resource;
    }
}
