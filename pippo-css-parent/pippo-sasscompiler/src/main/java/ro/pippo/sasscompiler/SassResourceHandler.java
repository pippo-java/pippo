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
package ro.pippo.sasscompiler;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssStylesheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.RouteContext;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Jipa
 */
public class SassResourceHandler extends ClasspathResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(SassResourceHandler.class);

    private boolean minify;
    private Map<String, String> sourceMap; // cache (activated only in prod mode)

    public SassResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath, resourceBasePath);

        sourceMap = new ConcurrentHashMap<>();
    }

    public SassResourceHandler useMinimized(boolean minimized) {
        this.minify = minimized;

        return this;
    }

    @Override
    protected void sendResource(URL resourceUrl, RouteContext routeContext) throws IOException {
        try {
            // compile sass to css
            log.trace("Send css for '{}'", resourceUrl);
            ScssContext.UrlMode urlMode = ScssContext.UrlMode.ABSOLUTE;
            String identifier = resourceUrl.getFile();
            ScssStylesheet scssStylesheet = ScssStylesheet.get(identifier);
            if (scssStylesheet == null) {
                throw new Exception("ScssStylesheet is null for '" + identifier + "'");
            }

            String content = scssStylesheet.toString();
            String result = sourceMap.get(content);
            if (result == null) {
                scssStylesheet.compile(urlMode);
                Writer writer = new StringWriter();
                scssStylesheet.write(writer, minify);
                result = writer.toString();

                if (routeContext.getApplication().getPippoSettings().isProd()) {
                    sourceMap.put(content, result);
                }
            }

            // send css
            routeContext.getResponse().contentType("text/css");
            routeContext.getResponse().ok().send(result);
        }  catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
