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
import ro.pippo.core.RuntimeMode;
import ro.pippo.core.route.StaticResourceHandler;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Jipa
 */
public class LessResourceHandler extends StaticResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LessResourceHandler.class);

    private final String resourceBasePath;

    private ConcurrentMap<String, String> sourceMap = new ConcurrentHashMap<>();

    public LessResourceHandler(String urlPath, String resourceBasePath) {
        super(urlPath);
        this.resourceBasePath = getNormalizedPath(resourceBasePath);
    }

    @Override
    public URL getResourceUrl(String resourcePath) {
        if (Application.get().getRuntimeMode().equals(RuntimeMode.DEV)){
            sourceMap.clear();
        }
        try{
            URL url = this.getClass().getClassLoader().getResource(getResourceBasePath() + "/" + resourcePath);
            LessSource.URLSource source = new LessSource.URLSource(url);
            String content = source.getContent();
            String result = sourceMap.get(content);
            if (result == null) {
                ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
                LessCompiler.CompilationResult compilationResult = compiler.compile(url);
                for (LessCompiler.Problem warning : compilationResult.getWarnings()) {
                    LOG.warn("Line: {}, Character: {}, Message: {} ", warning.getLine(), warning.getCharacter(), warning.getMessage());
                }
                result =  compilationResult.getCss();
                sourceMap.put(content, result);
            }
            File file = new File("output.css");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(result);
            writer.flush();
            writer.close();
            return file.toURI().toURL();
        }catch (Exception e){
            LOG.error("Error", e);
        }
        return null;
    }

    public String getResourceBasePath() {
        return resourceBasePath;
    }

}
