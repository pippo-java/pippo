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

/**
 * Base class for all the resource handlers.
 *
 * @author Decebal Suiu
 */
public abstract class ResourceHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ResourceHandler.class);

    public static final String PATH_PARAMETER = "path";

    private final String uriPattern;
    private boolean versioned = true;

    public ResourceHandler(String urlPath) {
        String normalizedPath = getNormalizedPath(urlPath);
        if (normalizedPath.length() > 0) {
            this.uriPattern = String.format("/%s/{%s: .+}", getNormalizedPath(urlPath), PATH_PARAMETER);
        } else {
            this.uriPattern = String.format("/{%s: .+}", PATH_PARAMETER);
        }
    }

    public String getUriPattern() {
        return uriPattern;
    }

    @Override
    public final void handle(RouteContext routeContext) {
        String resourcePath = getResourcePath(routeContext);
        log.trace("Request resource '{}'", resourcePath);

        if (versioned) {
            resourcePath = removeVersion(resourcePath);
        }

        handleResource(resourcePath, routeContext);

        routeContext.next();
    }

    public abstract void handleResource(String resourcePath, RouteContext routeContext);

    /**
     * Inject version fragment.
     */
    public abstract String injectVersion(String resourcePath);

    /**
     * Remove version fragment.
     */
    public abstract String removeVersion(String resourcePath);

    public boolean isVersioned() {
        return versioned;
    }

    public ResourceHandler setVersioned(boolean versioned) {
        this.versioned = versioned;

        return this;
    }

    protected String getResourcePath(RouteContext routeContext) {
        return getNormalizedPath(routeContext.getParameter(PATH_PARAMETER).toString());
    }

    protected String getNormalizedPath(String path) {
        if (path.length() > 0 && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.length() > 0 && '/' == path.charAt(path.length() - 1)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

}
