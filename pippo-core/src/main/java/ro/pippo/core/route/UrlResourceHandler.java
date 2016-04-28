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
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serves static resources.
 *
 * @author James Moger
 */
public abstract class UrlResourceHandler extends ResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(UrlResourceHandler.class);

    private static final Pattern VERSION_PATTERN = Pattern.compile("-ver-[0-9a-f]+\\.");

    public UrlResourceHandler(String urlPath) {
        super(urlPath);
    }

    @Override
    public final void handleResource(String resourcePath, RouteContext routeContext) {
        URL url = getResourceUrl(resourcePath);
        if (url != null) {
            streamResource(url, routeContext);
        }
    }

    public abstract URL getResourceUrl(String resourcePath);

    protected String getResourceVersion(String resourcePath) {
        String version = null;

        URL resourceUrl = getResourceUrl(resourcePath);
        if (resourceUrl != null) {
            try {
                version = Long.toString(resourceUrl.openConnection().getLastModified());
            } catch (NullPointerException | IOException e) {
                log.error("Failed to read lastModified property for {}", resourceUrl, e);
            }
        }

        return version;
    }

    @Override
    public String injectVersion(String resourcePath) {
        String version = getResourceVersion(resourcePath);
        if (StringUtils.isNullOrEmpty(version)) {
            // unversioned, pass-through resource path
            return resourcePath;
        }

        // check for extension
        int extensionAt = resourcePath.lastIndexOf('.');

        StringBuilder versionedResourcePath = new StringBuilder();

        if (extensionAt == -1) {
            versionedResourcePath.append(resourcePath);
            versionedResourcePath.append("-ver-").append(version);
        } else {
            versionedResourcePath.append(resourcePath.substring(0, extensionAt));
            versionedResourcePath.append("-ver-").append(version);
            versionedResourcePath.append(resourcePath.substring(extensionAt, resourcePath.length()));
        }

        log.trace("Inject version in resource path: '{}' => '{}'", resourcePath, versionedResourcePath);

        return versionedResourcePath.toString();
    }

    @Override
    public String removeVersion(String resourcePath) {
        Matcher matcher = VERSION_PATTERN.matcher(resourcePath);
        if (matcher.find()) {
            int startIndex = matcher.start() - 1;
            int endIndex = matcher.end() - 1;
            String version = resourcePath.substring(startIndex + 1, endIndex);

            String unversionedResourcePath = resourcePath.replace(version, "");
            log.trace("Remove version from resource path: '{}' => '{}'", resourcePath, unversionedResourcePath);

            return unversionedResourcePath;
        }

        return resourcePath;
    }

    protected void streamResource(URL resourceUrl, RouteContext routeContext) {
        try {
            long lastModified = resourceUrl.openConnection().getLastModified();
            routeContext.getApplication().getHttpCacheToolkit().addEtag(routeContext, lastModified);

            if (routeContext.getResponse().getStatus() == HttpConstants.StatusCode.NOT_MODIFIED) {
                // do not stream anything out, simply return 304
                routeContext.getResponse().commit();
            } else {
                sendResource(resourceUrl, routeContext);
            }
        } catch (Exception e) {
            throw new PippoRuntimeException(e, "Failed to stream resource {}", resourceUrl);
        }
    }

    protected void sendResource(URL resourceUrl, RouteContext routeContext) throws IOException {
        String filename = resourceUrl.getFile();
        String mimeType = routeContext.getApplication().getMimeTypes().getContentType(filename);
        if (!StringUtils.isNullOrEmpty(mimeType)) {
            // stream the resource
            log.debug("Streaming as resource '{}'", resourceUrl);
            routeContext.getResponse().contentType(mimeType);
            routeContext.getResponse().ok().resource(resourceUrl.openStream());
        } else {
            // stream the file
            log.debug("Streaming as file '{}'", resourceUrl);
            routeContext.getResponse().ok().file(filename, resourceUrl.openStream());
        }
    }

}
