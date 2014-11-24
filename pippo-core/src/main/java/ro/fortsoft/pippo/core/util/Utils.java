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
package ro.fortsoft.pippo.core.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Miscellaneous utility functions to keep pippo-core small.
 *
 */
public class Utils {

    private final static Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Tries to find a resource with the given name in the classpath.
     *
     * @param resourceName
     *            the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     *         cannot be found
     */
    public static URL locateOnClasspath(String resourceName) {
        URL url = null;
        // attempt to load from the context classpath
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            url = loader.getResource(resourceName);

            if (url != null) {
                log.debug("Located '{}' in the context classpath", resourceName);
            }
        }

        // attempt to load from the system classpath
        if (url == null) {
            url = ClassLoader.getSystemResource(resourceName);

            if (url != null) {
                log.debug("Located '{}' in the system classpath", resourceName);
            }
        }
        return url;
    }
}
