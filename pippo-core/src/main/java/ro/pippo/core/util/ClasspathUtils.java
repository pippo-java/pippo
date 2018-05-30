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
package ro.pippo.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.PippoRuntimeException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Classpath utility functions.
 */
public class ClasspathUtils {

    private final static Logger log = LoggerFactory.getLogger(ClasspathUtils.class);

    private ClasspathUtils() {}

    /**
     * Tries to find a resource with the given name in the classpath.
     *
     * @param resourceName the name of the resource
     * @return the URL to the found resource or <b>null</b> if the resource
     * cannot be found
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

    /**
     * Return a list of resource URLs that match the given name.
     *
     * @param name
     * @return a list of resource URLs that match the name
     */
    public static List<URL> getResources(String name) {
        List<URL> list = new ArrayList<>();

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = loader.getResources(name);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                list.add(url);
            }
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }

        return list;
    }

    /**
     * Returns true if the specified class can be found on the classpath.
     *
     * @param className
     * @return true if the class is on the classpath
     */
    public static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception e) {
            // do nothing
        }

        return false;
    }

}
