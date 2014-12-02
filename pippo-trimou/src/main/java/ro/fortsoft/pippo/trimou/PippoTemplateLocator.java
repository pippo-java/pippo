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
package ro.fortsoft.pippo.trimou;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trimou.engine.locator.FilePathTemplateLocator;
import org.trimou.exception.MustacheException;
import org.trimou.exception.MustacheProblem;

import ro.fortsoft.pippo.core.PippoConstants;
import ro.fortsoft.pippo.core.util.ClasspathUtils;

/**
 * Locates a template in the classpath.
 *
 * @author James Moger
 *
 */
class PippoTemplateLocator extends FilePathTemplateLocator {
    private final Logger log = LoggerFactory.getLogger(PippoTemplateLocator.class);

    public PippoTemplateLocator(int priority, String rootPath) {
        super(priority, rootPath, TrimouTemplateEngine.MUSTACHE);
        checkRootDir();
    }

    @Override
    public Set<String> getAllIdentifiers() {
        if (getRootPath() == null) {
            return Collections.emptySet();
        }
        return super.getAllIdentifiers();
    }

    @Override
    public Reader locateRealPath(String realPath) {

        String name = getRootPath() != null ? getRootPath() + addSuffix(realPath) : addSuffix(realPath);
        URL url = ClasspathUtils.locateOnClasspath(name);
        if (url == null) {
            return null;
        }
        log.debug("Template located: {}", getRootPath() + realPath);
        try {
            InputStream in = url.openStream();
            return new InputStreamReader(in, getDefaultFileEncoding());
        } catch (IOException e) {
            throw new MustacheException(MustacheProblem.TEMPLATE_LOADING_ERROR, e);
        }
    }

    @Override
    protected File getRootDir() {

        if (getRootPath() == null) {
            return null;
        }

        try {

            URL url = ClasspathUtils.locateOnClasspath(getRootPath());

            if (url == null) {
                throw new MustacheException(MustacheProblem.TEMPLATE_LOCATOR_INVALID_CONFIGURATION,
                        "Root path resource not found: %s", getRootPath());
            }
            return new File(URLDecoder.decode(url.getFile(), PippoConstants.UTF8));

        } catch (UnsupportedEncodingException e) {
            throw new MustacheException(MustacheProblem.TEMPLATE_LOCATOR_INVALID_CONFIGURATION, e);
        }
    }
}