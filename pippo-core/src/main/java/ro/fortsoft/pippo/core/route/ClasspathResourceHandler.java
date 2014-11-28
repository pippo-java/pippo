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
package ro.fortsoft.pippo.core.route;

import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.util.HttpCacheToolkit;
import ro.fortsoft.pippo.core.util.MimeTypes;
import ro.fortsoft.pippo.core.util.StringUtils;

import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves classpath resources.
 *
 * @author James Moger
 *
 */
public class ClasspathResourceHandler implements RouteHandler {

    protected static String PATH_PARAMETER = "path";

    private final Logger log = LoggerFactory.getLogger(ClasspathResourceHandler.class);

    private final String urlPattern;

    private final String resourceBasePath;

    public ClasspathResourceHandler(String urlPath, String resourceBasePath) {
        this.urlPattern = String.format("/%s/{%s: .*}", getNormalizedPath(urlPath), PATH_PARAMETER);
        this.resourceBasePath = getNormalizedPath(resourceBasePath);
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    @Override
    public void handle(Request request, Response response, RouteHandlerChain chain) {

        String path = getRequestedPath(request);
        log.debug("classpath resource request for '{}'", path);

        URL url = this.getClass().getClassLoader().getResource(path);
        if (url == null) {
            response.notFound();
            return;
        }

        streamClasspathResource(url, request, response);
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
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return filename;
    }

    protected void streamClasspathResource(URL url, Request request, Response response) {

        try {

            URLConnection urlConnection = url.openConnection();
            String filename = url.getFile();

            // stream the file
            log.debug("Streaming as file '{}'", url);
            response.file(filename, urlConnection.getInputStream());

        } catch (Exception e) {
            throw new PippoRuntimeException("Failed to stream classpath resource " + url, e);
        }
    }
}
