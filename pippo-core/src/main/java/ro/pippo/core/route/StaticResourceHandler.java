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
import ro.pippo.core.Request;
import ro.pippo.core.Response;
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
    public final void handle(Request request, Response response, RouteHandlerChain chain) {
        String resourcePath = getResourcePath(request);
        log.debug("Request resource '{}'", resourcePath);

        URL url = getResourceUrl(resourcePath);
        if (url == null) {
            response.notFound().commit();
        } else {
            streamResource(url, request, response);
        }

        chain.next();
    }

    public abstract URL getResourceUrl(String resourcePath);

    protected String getResourcePath(Request request) {
        return getNormalizedPath(request.getParameter(PATH_PARAMETER).toString());
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

    protected void streamResource(URL resourceUrl, Request request, Response response) {
        try {
            URLConnection urlConnection = resourceUrl.openConnection();
            long lastModified = urlConnection.getLastModified();
            httpCacheToolkit.addEtag(request, response, lastModified);

            if (response.getStatus() == HttpConstants.StatusCode.NOT_MODIFIED) {
                // Do not stream anything out. Simply return 304
                log.debug("Unmodified resource '{}'", resourceUrl);
                response.commit();
            } else {
                String filename = resourceUrl.getFile();

                // Try to set the mimetype:
                String mimeType = mimeTypes.getContentType(request, response, filename);

                if (!StringUtils.isNullOrEmpty(mimeType)) {
                    // stream the resource
                    log.debug("Streaming as resource '{}'", resourceUrl);
                    response.contentType(mimeType);
                    response.resource(urlConnection.getInputStream());
                } else {
                    // stream the file
                    log.debug("Streaming as file '{}'", resourceUrl);
                    response.file(filename, urlConnection.getInputStream());
                }
            }
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to stream resource " + resourceUrl, e);
        }
    }

}
