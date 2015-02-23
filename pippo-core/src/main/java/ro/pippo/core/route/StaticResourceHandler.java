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
import ro.pippo.core.util.HttpCacheToolkit;
import ro.pippo.core.util.MimeTypes;
import ro.pippo.core.util.StringUtils;

import java.net.URL;
import java.net.URLConnection;

/**
 * Serves static resources.
 *
 * @author James Moger
 */
public abstract class StaticResourceHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceHandler.class);

    public static final String PATH_PARAMETER = "path";

    private final String uriPattern;

    private MimeTypes mimeTypes;

    private HttpCacheToolkit httpCacheToolkit;

    public StaticResourceHandler(String urlPath) {
        this.uriPattern = String.format("/%s/{%s: .*}", getNormalizedPath(urlPath), PATH_PARAMETER);
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setMimeTypes(MimeTypes mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public void setHttpCacheToolkit(HttpCacheToolkit httpCacheToolkit) {
        this.httpCacheToolkit = httpCacheToolkit;
    }

    @Override
    public final void handle(RouteContext routeContext) {
        String resourcePath = getResourcePath(routeContext);
        log.trace("Request resource '{}'", resourcePath);

        URL url = getResourceUrl(resourcePath);
        if (url == null) {
            routeContext.getResponse().notFound().commit();
        } else {
            streamResource(url, routeContext);
        }

        routeContext.next();
    }

    public abstract URL getResourceUrl(String resourcePath);

    protected String getResourcePath(RouteContext routeContext) {
        return getNormalizedPath(routeContext.getRequest().getParameter(PATH_PARAMETER).toString());
    }

    protected String getNormalizedPath(String path) {
        if ('/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if ('/' == path.charAt(path.length() - 1)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    protected void streamResource(URL resourceUrl, RouteContext routeContext) {
        try {
            URLConnection urlConnection = resourceUrl.openConnection();
            long lastModified = urlConnection.getLastModified();
            httpCacheToolkit.addEtag(routeContext, lastModified);

            if (routeContext.getResponse().getStatus() == HttpConstants.StatusCode.NOT_MODIFIED) {
                // Do not stream anything out. Simply return 304
                routeContext.getResponse().commit();
            } else {
                String filename = resourceUrl.getFile();

                // Try to set the mimetype:
                String mimeType = mimeTypes.getContentType(routeContext, filename);

                if (!StringUtils.isNullOrEmpty(mimeType)) {
                    // stream the resource
                    log.debug("Streaming as resource '{}'", resourceUrl);
                    routeContext.getResponse().contentType(mimeType);
                    routeContext.getResponse().ok().resource(urlConnection.getInputStream());
                } else {
                    // stream the file
                    log.debug("Streaming as file '{}'", resourceUrl);
                    routeContext.getResponse().ok().file(filename, urlConnection.getInputStream());
                }
            }
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to stream resource {}", e, resourceUrl);
        }
    }

}
