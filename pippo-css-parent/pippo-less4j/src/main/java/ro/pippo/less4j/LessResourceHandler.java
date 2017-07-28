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
package ro.pippo.less4j;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class LessResourceHandler extends ClasspathResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(LessResourceHandler.class);

    private boolean minify;
    private Map<String, String> sourceMap; // cache (activated only in prod mode)

    public LessResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath, resourceBasePath);

        sourceMap = new ConcurrentHashMap<>();
    }

    public LessResourceHandler useMinimized(boolean minimized) {
        this.minify = minimized;

        return this;
    }

    @Override
    protected void sendResource(URL resourceUrl, RouteContext routeContext) throws IOException {
        try {
            // compile less to css
            log.trace("Send css for '{}'", resourceUrl);
            LessSource.URLSource source = new LessSource.URLSource(resourceUrl);
            String content = source.getContent();
            String result = sourceMap.get(content);
            if (result == null) {
                ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
                LessCompiler.Configuration configuration = new LessCompiler.Configuration();
                configuration.setCompressing(minify);
                LessCompiler.CompilationResult compilationResult = compiler.compile(resourceUrl, configuration);
                for (LessCompiler.Problem warning : compilationResult.getWarnings()) {
                    log.warn("Line: {}, Character: {}, Message: {} ", warning.getLine(), warning.getCharacter(), warning.getMessage());
                }
                result = compilationResult.getCss();

                if (routeContext.getApplication().getPippoSettings().isProd()) {
                    sourceMap.put(content, result);
                }
            }

            // send css
            routeContext.getResponse().contentType("text/css");
            routeContext.getResponse().ok().send(result);
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

}
