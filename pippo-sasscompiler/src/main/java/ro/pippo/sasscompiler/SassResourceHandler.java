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
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.route.RouteContext;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Jipa
 */
public class SassResourceHandler extends ClasspathResourceHandler {

    private Map<String, String> sourceMap = new ConcurrentHashMap<>(); // cache

    public SassResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath, resourceBasePath);
    }

    @Override
    protected void sendResource(URL resourceUrl, RouteContext routeContext) throws IOException {
        try {
            // compile sass to css
            ScssContext.UrlMode urlMode = ScssContext.UrlMode.ABSOLUTE;
            ScssStylesheet scssStylesheet = ScssStylesheet.get(resourceUrl.getFile());
            String content = scssStylesheet.toString();
            String result = sourceMap.get(content);
            if (result == null) {
                scssStylesheet.compile(urlMode);
                result = scssStylesheet.printState();

                if (routeContext.getApplication().getPippoSettings().isDev()) {
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
