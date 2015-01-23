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

import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.util.HttpCacheToolkit;
import ro.pippo.core.util.MimeTypes;
import ro.pippo.core.util.StringUtils;

import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves classpath resources.
 *
 * @author James Moger
 */
public class ClasspathResourceHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(ClasspathResourceHandler.class);

    public static String PATH_PARAMETER = "path";

    private final String uriPattern;
    private final String resourceBasePath;

    private MimeTypes mimeTypes;

    private HttpCacheToolkit httpCacheToolkit;

    public ClasspathResourceHandler(String urlPath, String resourceBasePath) {
        this.uriPattern = String.format("/%s/{%s: .*}", getNormalizedPath(urlPath), PATH_PARAMETER);
        this.resourceBasePath = getNormalizedPath(resourceBasePath);
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
    public void handle(Request request, Response response, RouteHandlerChain chain) {
        String path = getRequestedPath(request);
        log.debug("Request for '{}'", path);

        URL url = this.getClass().getClassLoader().getResource(path);
        if (url == null) {
            response.notFound().commit();
        } else {
            streamClasspathResource(url, request, response);
        }

        chain.next();
    }

    protected String getRequestedPath(Request request) {
        String path = request.getParameter(PATH_PARAMETER).toString();
        String normalizedPath = getNormalizedPath(path);
        return resourceBasePath + "/" + normalizedPath;
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

    protected String getFilename(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    protected void streamClasspathResource(URL url, Request request, Response response) {
        try {
            URLConnection urlConnection = url.openConnection();
            long lastModified = urlConnection.getLastModified();
            httpCacheToolkit.addEtag(request, response, lastModified);

            if (response.getStatus() == HttpConstants.StatusCode.NOT_MODIFIED) {
                // Do not stream anything out. Simply return 304
                log.debug("Unmodified resource '{}'", url);
                response.commit();
            } else {
                String filename = url.getFile();

                // Try to set the mimetype:
                String mimeType = mimeTypes.getContentType(request, response, filename);

                if (!StringUtils.isNullOrEmpty(mimeType)) {
                    // stream the resource
                    log.debug("Streaming as resource '{}'", url);
                    response.contentType(mimeType);
                    response.resource(urlConnection.getInputStream());
                } else {
                    // stream the file
                    log.debug("Streaming as file '{}'", url);
                    response.file(filename, urlConnection.getInputStream());
                }
            }
        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to stream classpath resource " + url, e);
        }
    }

}
