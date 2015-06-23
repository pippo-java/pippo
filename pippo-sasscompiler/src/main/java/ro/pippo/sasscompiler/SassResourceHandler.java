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
import ro.pippo.core.Application;
import ro.pippo.core.RuntimeMode;
import ro.pippo.core.route.StaticResourceHandler;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Jipa
 */
public class SassResourceHandler extends StaticResourceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SassResourceHandler.class);

    private final String resourceBasePath;

    private ConcurrentMap<String, String> sourceMap = new ConcurrentHashMap<>();

    public SassResourceHandler(String urlPath, String resourceBasePath) {
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
            ScssContext.UrlMode urlMode = ScssContext.UrlMode.ABSOLUTE;
            ScssStylesheet scss = ScssStylesheet.get(url.getFile());
            if (scss == null) {
                LOG.error("The scss file {} could not be found", url.getFile());
                return null;
            }
            String content = scss.toString();
            String result = sourceMap.get(content);
            if (result == null) {
                scss.compile(urlMode);
                result = scss.printState();
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
