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

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves classpath resources.
 *
 * @author James Moger
 */
public class ClasspathResourceHandler extends UrlResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(ClasspathResourceHandler.class);

    protected final String resourceBasePath;

    public ClasspathResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath);

        this.resourceBasePath = getNormalizedPath(resourceBasePath);
    }

    @Override
    public URL getResourceUrl(String resourcePath) {
        if (!isValidResourcePath(resourceBasePath, resourcePath)) {
            log.warn("Request for '{}' which is not located in '{}'", resourcePath, resourceBasePath);
            return null;
        }

        String resourceName = getResourceBasePath() + "/" + resourcePath;
        return this.getClass().getClassLoader().getResource(resourceName);
    }

    public String getResourceBasePath() {
        return resourceBasePath;
    }

    protected boolean isValidResourcePath(String resourceBasePath, String resourcePath) {
        Path requestedPath = Paths.get(resourceBasePath).resolve(resourcePath).normalize();
        return requestedPath.startsWith(resourceBasePath);
    }

}
