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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves file resources.
 *
 * @author Decebal Suiu
 */
public class FileResourceHandler extends UrlResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(FileResourceHandler.class);

    final String directory;

    public FileResourceHandler(String urlPath, File directory) {
        super(urlPath);

        this.directory = directory.getAbsolutePath();
    }

    public FileResourceHandler(String urlPath, String directory) {
        this(urlPath, new File(directory));
    }

    @Override
    public URL getResourceUrl(String resourcePath) {
        URL url = null;

        try {
            Path requestedPath = Paths.get(directory, resourcePath).normalize().toAbsolutePath();
            if (!requestedPath.startsWith(directory)) {
                log.warn("Request for '{}' which is not located in '{}'", requestedPath, directory);
                return null;
            }

            File file = requestedPath.toFile();
            if (file.exists() && file.isFile()) {
                url = requestedPath.toUri().toURL();
            } else {
                log.warn("File '{}' not found", resourcePath);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }

        return url;
    }

}
