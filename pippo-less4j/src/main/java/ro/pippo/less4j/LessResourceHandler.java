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
import ro.pippo.core.Application;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.RuntimeMode;
import ro.pippo.core.route.ClasspathResourceHandler;
import ro.pippo.core.util.IoUtils;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Jipa
 */
public class LessResourceHandler extends ClasspathResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(LessResourceHandler.class);

    private Map<String, String> sourceMap = new ConcurrentHashMap<>(); // cache

    private boolean compress;

    public LessResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath, resourceBasePath);
        this.compress = Application.get().getPippoSettings().isProd();
    }

    @Override
    public URL getResourceUrl(String resourcePath) {
        URL url = super.getResourceUrl(resourcePath);
        if (url == null) {
            return null;
        }

        // clear cache for DEV mode
        if (Application.get().getRuntimeMode().equals(RuntimeMode.DEV)) {
            sourceMap.clear();
        }

        try {
            // compile less to css
            LessSource.URLSource source = new LessSource.URLSource(url);
            String content = source.getContent();
            String result = sourceMap.get(content);
            if (result == null) {
                ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
                LessCompiler.Configuration configuration = new LessCompiler.Configuration();
                configuration.setCompressing(compress);
                LessCompiler.CompilationResult compilationResult = compiler.compile(url, configuration);
                for (LessCompiler.Problem warning : compilationResult.getWarnings()) {
                    log.warn("Line: {}, Character: {}, Message: {} ", warning.getLine(), warning.getCharacter(), warning.getMessage());
                }
                result = compilationResult.getCss();
                sourceMap.put(content, result);
            }

            // create a temporary file with a unique name
            File file = File.createTempFile("pippo-less4j-" + UUID.randomUUID().toString(), ".css");
            file.deleteOnExit();

            // write the css to above file
            IoUtils.copy(result, file);

            return file.toURI().toURL();
        } catch (Exception e) {
            throw new PippoRuntimeException(e);
        }
    }

    public LessResourceHandler useMinimized(boolean minimized){
        this.compress = minimized;
        return this;
    }


}
