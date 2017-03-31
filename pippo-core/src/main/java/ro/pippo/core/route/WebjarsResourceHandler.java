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
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.CryptoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Serves Webjars classpath resources.
 *
 * @author James Moger
 */
public class WebjarsResourceHandler extends ClasspathResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(WebjarsResourceHandler.class);

    private final Map<String, String> pathAliases;

    public WebjarsResourceHandler() {
        this("/webjars");
    }

    public WebjarsResourceHandler(String urlPath) {
        super(urlPath, "META-INF/resources/webjars");

        this.pathAliases = indexWebjars();
    }

    @Override
    public URL getResourceUrl(String resourcePath) {
        if (!isValidResourcePath(resourceBasePath, resourcePath)) {
            log.warn("Request for '{}' which is not located in '{}'", resourcePath, resourceBasePath);
            return null;
        }

        String resourceName = getResourceBasePath() + "/" + resourcePath;
        String artifactPath = resourcePath.substring(0, resourcePath.indexOf('/') + 1);
        if (pathAliases.containsKey(artifactPath)) {
            String artifactVersion = pathAliases.get(artifactPath);

            // Do not replace already fixed-version paths.
            // i.e. skip replacing first path segment of "/jquery/1.11.1/jquery.min.js"
            // BUT do replace first path segment of "jquery/jquery.min.js".
            if (!resourcePath.startsWith(artifactVersion)) {
                String aliasedPath = artifactVersion + resourcePath.substring(artifactPath.length());
                log.trace("Replaced Webjar path {} with {}", resourcePath, aliasedPath);
                resourceName = getResourceBasePath() + "/" + aliasedPath;
            }
        }

        URL url = this.getClass().getClassLoader().getResource(resourceName);
        if (url == null) {
            log.warn("Resource '{}' not found", resourceName);
        }

        return url;
    }

    @Override
    protected String getResourceVersion(String resourcePath) {
        String artifactPath = resourcePath.substring(0, resourcePath.indexOf('/') + 1);
        if (pathAliases.containsKey(artifactPath)) {
            String artifactVersion = pathAliases.get(artifactPath);

            // Do not replace already fixed-version paths.
            // i.e. skip replacing first path segment of "/jquery/1.11.1/jquery.min.js"
            // BUT do replace first path segment of "jquery/jquery.min.js".
            if (!resourcePath.startsWith(artifactVersion)) {
                return CryptoUtils.getHashMD5(artifactVersion);
            }
        }

        return null;
    }

    /**
     * Scans the classpath for Webjars metadata and creates an alias registry of path aliases to fixed version paths
     * for all Webjars on the classpath.
     * <p>
     * This allows specifications of a path like "jquery/jquery.min.js" which will be substituted
     * at runtime as "jquery/1.11.1/jquery.min.js".</p>
     *
     * @return a map of path pathAliases
     */
    private Map<String, String> indexWebjars() {
        Map<String, String> pathAliases = new TreeMap<>();
        for (String pomProperties : locatePomProperties()) {
            log.debug("Parsing Webjars metadata {}", pomProperties);
            URL url = ClasspathUtils.locateOnClasspath(pomProperties);
            Properties pom = new Properties();
            try (InputStream is = url.openStream()) {
                pom.load(is);
            } catch (IOException e) {
                log.error("Failed to read {}", url);
                continue;
            }

            String artifactId = pom.getProperty("artifactId");
            String version = pom.getProperty("version");
            String fixedPath = artifactId + '/' + version + '/';
            String aliasPath = artifactId + '/';
            pathAliases.put(aliasPath, fixedPath);
        }

        for (Map.Entry<String, String> alias : pathAliases.entrySet()) {
            log.debug("Registered Webjars path alias '{}' => '{}'", alias.getKey(), alias.getValue());
        }

        return pathAliases;
    }

    /**
     * Locate all Webjars Maven pom.properties files on the classpath.
     *
     * @return a list of Maven pom.properties files on the classpath
     */
    private List<String> locatePomProperties() {
        List<String> propertiesFiles = new ArrayList<>();
        List<URL> packageUrls = ClasspathUtils.getResources(getResourceBasePath());
        for (URL packageUrl : packageUrls) {
            if (packageUrl.getProtocol().equals("jar")) {
                // We only care about Webjars jar files
                log.debug("Scanning {}", packageUrl);
                try {
                    String jar = packageUrl.toString().substring("jar:".length()).split("!")[0];
                    File file = new File(new URI(jar));
                    try (JarInputStream is = new JarInputStream(new FileInputStream(file))) {
                        JarEntry entry = null;
                        while ((entry = is.getNextJarEntry()) != null) {
                            if (!entry.isDirectory() && entry.getName().endsWith("pom.properties")) {
                                propertiesFiles.add(entry.getName());
                            }
                        }
                    }
                } catch (URISyntaxException | IOException e) {
                    throw new PippoRuntimeException("Failed to get classes for package '{}'", packageUrl);
                }
            }
        }

        return propertiesFiles;
    }

}
