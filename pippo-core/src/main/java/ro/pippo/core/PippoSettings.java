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
package ro.pippo.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * A simple properties-file based settings class for Pippo applications.
 * <p>
 * The properties file is automatically loaded from one of the following
 * sources:
 * <ol>
 * <li>from the <code>-Dpippo.settings</code> system property
 * <li>from the current working directory (application.properties)
 * <li>from the application classpath (conf/application.properties)
 * <li>using the built-in Pippo properties
 * </ol>
 * </p>
 * <p>
 * A settings file located on the filesystem is automatically reloaded, if
 * modified, on the next setting lookup.
 * </p>
 * <p>
 * All settings support runtime-mode configuration allowing you to specify the
 * same setting with mode-dependent values.
 * </p>
 * <p>
 * Settings without a mode prefix are the shared for all modes unless explicitly
 * overridden by a mode-specific setting.
 * </p>
 * <p/>
 * <pre>
 * example.value = my example value
 * %prod.example.value = my production value
 * %dev.example.value = my development value
 * </pre>
 * <p>
 * Setting files support including/importing settings from other setting files
 * using the <i>include=</i> setting. The <i>include=</i> setting is a
 * comma-delimited field allowing importing settings from multiple sources.
 * Inclusions may be recursive.
 * </p>
 *
 * @author James Moger
 */
public class PippoSettings {

    private static final Logger log = LoggerFactory.getLogger(PippoSettings.class);

    private static final String USING_DEFAULT_OF = " using default of ";

    private static final String DEFAULT_LIST_DELIMITER = ",";

    private final RuntimeMode runtimeMode;

    private final Properties properties;

    private final Properties overrides;

    private final Map<String, String> interpolationValues;

    private final URL propertiesUrl;

    private final boolean isFile;

    private volatile long lastModified;

    public PippoSettings(RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
        this.propertiesUrl = getPropertiesUrl();
        this.isFile = propertiesUrl.getProtocol().equals("file:");
        this.interpolationValues = new HashMap<>();

        loadInterpolationValues();
        this.properties = loadProperties(propertiesUrl);
        this.overrides = new Properties();
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public boolean isDev() {
        return RuntimeMode.DEV == runtimeMode;
    }

    public boolean isTest() {
        return RuntimeMode.TEST == runtimeMode;
    }

    public boolean isProd() {
        return RuntimeMode.PROD == runtimeMode;
    }

    public String getLocalHostname() {
        // try InetAddress.LocalHost first;
        // NOTE -- InetAddress.getLocalHost().getHostName() will not work in
        // certain environments.
        try {
            String result = InetAddress.getLocalHost().getHostName();
            if (!StringUtils.isNullOrEmpty(result))
                return result;
        } catch (UnknownHostException e) {
            // failed; try alternate means.
        }

        // try environment properties.
        //
        String host = System.getenv("COMPUTERNAME");
        if (host != null)
            return host;
        host = System.getenv("HOSTNAME");
        if (host != null)
            return host;

        // undetermined.
        return "Pippo";
    }

    private URL getPropertiesUrl() {
        URL url = null;
        try {
            // System property
            String systemProperty = System.getProperty(PippoConstants.SYSTEM_PROPERTY_PIPPO_SETTINGS, null);
            if (!StringUtils.isNullOrEmpty(systemProperty)) {
                url = URI.create(systemProperty).toURL();
                log.debug("Located '{}' using the '{}' system property", url, PippoConstants.SYSTEM_PROPERTY_PIPPO_SETTINGS);
            }
        } catch (MalformedURLException e) {
            log.error("Failed to parse '{}' system property!", PippoConstants.SYSTEM_PROPERTY_PIPPO_SETTINGS);
        }

        try {
            // working directory
            Path path = Paths.get(System.getProperty("user.dir"), PippoConstants.APPLICATION_PROPERTIES);
            if (path.toFile().exists()) {
                url = path.toUri().toURL();
                log.debug("Located '{}' in the current working directory", url);
            }
        } catch (MalformedURLException e) {
            log.error("Failed to parse working directory properties!", e);
        }

        if (url == null) {
            // try locating an application classpath properties file
            url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_CLASSPATH_PROPERTIES);
        }

        if (url == null) {
            // last resort, use built-in properties
            url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
        }

        return url;
    }

    private synchronized Properties getProperties() {
        if (isFile) {
            File file = new File(propertiesUrl.getPath());
            if (file.lastModified() > lastModified) {
                Properties reloadedProperties = loadProperties(propertiesUrl);
                properties.clear();
                properties.putAll(reloadedProperties);
                lastModified = file.lastModified();
            }
        }

        return properties;
    }

    private synchronized Properties loadProperties(URL propertiesUrl) {
        final Properties properties = new Properties();
        try (BufferedReader is =  new BufferedReader(
                new InputStreamReader(propertiesUrl.openStream(), StandardCharsets.UTF_8))) {
            log.debug("loading {}", propertiesUrl);
            Properties props = new Properties();
            props.load(is);

            File baseDir;
            if (isFile) {
                baseDir = new File(propertiesUrl.getPath()).getParentFile();
            } else {
                // use current working directory for includes/overrides
                baseDir = new File(System.getProperty("user.dir"));
            }

            // allow including other properties files
            props = loadIncludes(baseDir, props);

            // allow overriding properties
            props = loadOverrides(baseDir, props);

            // collect settings for the current runtime mode
            String prefix = "%" + runtimeMode.toString() + ".";

            // sort names, mode-specific names are first
            Set<String> names = new TreeSet<>(props.stringPropertyNames());
            for (String name : names) {
                if (name.charAt(0) == '%') {
                    // mode-specific setting
                    if (name.startsWith(prefix)) {
                        // setting for the current runtime mode
                        properties.put(name.substring(prefix.length()), props.get(name));
                    }
                } else if (!properties.containsKey(name)) {
                    // setting for all modes
                    properties.put(name, props.get(name));
                }
            }

        } catch (FileNotFoundException f) {
            throw new PippoRuntimeException(f, "Failed to find {}", propertiesUrl);
        } catch (Exception t) {
            throw new PippoRuntimeException(t, "Failed to read {}", propertiesUrl);
        }

        return properties;
    }

    /**
     * Recursively read "include" properties files.
     * <p>
     * "Include" properties are the base properties which are overwritten by
     * the provided properties.
     * </p>
     *
     * @param baseDir
     * @param properties
     * @return the merged properties
     * @throws IOException
     */
    private Properties loadIncludes(File baseDir, Properties properties) throws IOException {
        return loadProperties(baseDir, properties, "include", false);
    }

    /**
     * Recursively read "override" properties files.
     * <p>
     * "Override" properties overwrite the provided properties.
     * </p>
     *
     * @param baseDir
     * @param properties
     * @return the merged properties
     * @throws IOException
     */
    private Properties loadOverrides(File baseDir, Properties properties) throws IOException {
        return loadProperties(baseDir, properties, "override", true);
    }

    /**
     * Recursively read referenced properties files.
     *
     * @param baseDir
     * @param currentProperties
     * @param key
     * @param override
     * @return the merged properties
     * @throws IOException
     */
    private Properties loadProperties(File baseDir, Properties currentProperties, String key, boolean override) throws IOException {
        Properties loadedProperties = new Properties();

        String include = (String) currentProperties.remove(key);
        if (!StringUtils.isNullOrEmpty(include)) {
            // allow for multiples
            List<String> names = StringUtils.getList(include, DEFAULT_LIST_DELIMITER);
            for (String name : names) {
                if (StringUtils.isNullOrEmpty(name)) {
                    continue;
                }

                // interpolate any variables
                final String fileName = interpolateString(name);

                // try co-located
                File file = new File(baseDir, fileName);
                if (!file.exists()) {
                    // try absolute path
                    file = new File(fileName);
                }

                if (!file.exists()) {
                    log.warn("failed to locate {}", file);
                    continue;
                }

                // load properties
                log.debug("loading {} settings from {}", key, file);
                try (FileInputStream iis = new FileInputStream(file)) {
                    loadedProperties.load(iis);
                }

                // read nested properties
                loadedProperties = loadProperties(file.getParentFile(), loadedProperties, key, override);
            }
        }

        Properties merged = new Properties();
        if (override) {
            // "override" settings overwrite the current properties
            merged.putAll(currentProperties);
            merged.putAll(loadedProperties);
        } else {
            // "include" settings are overwritten by the current properties
            merged.putAll(loadedProperties);
            merged.putAll(currentProperties);
        }

        return merged;
    }

    /**
     * Creates a map of prepared interpolation values.
     * <p>
     * All values support the standard ${name} syntax but also support
     * the @{name} syntax to cope with build systems with aggressive
     * token filtering.
     * </p>
     */
    protected void loadInterpolationValues() {
        // System properties may be accessed using ${name} or @{name}
        for (String propertyName : System.getProperties().stringPropertyNames()) {
            String value = System.getProperty(propertyName);
            if (!StringUtils.isNullOrEmpty(value)) {
                addInterpolationValue(propertyName, value);
            }
        }

        // Environment variables may be accessed using ${env.NAME} or @{env.NAME}
        for (String variableName : System.getenv().keySet()) {
            String value = System.getenv(variableName);
            if (!StringUtils.isNullOrEmpty(value)) {
                addInterpolationValue("env." + variableName, value);
            }
        }
    }

    /**
     * Add a value that may be interpolated.
     *
     * @param name
     * @param value
     */
    protected void addInterpolationValue(String name, String value) {
        interpolationValues.put(String.format("${%s}", name), value);
        interpolationValues.put(String.format("@{%s}", name), value);
    }

    /**
     * Interpolates a string value using System properties and Environment variables.
     *
     * @param value
     * @return an interpolated string
     */
    protected String interpolateString(String value) {
        String interpolatedValue = value;
        for (Map.Entry<String, String> entry : interpolationValues.entrySet()) {
            interpolatedValue = interpolatedValue.replace(entry.getKey(), entry.getValue());
        }

        return interpolatedValue;
    }

    /**
     * Returns the list of all setting names.
     *
     * @return list of setting names
     */
    public List<String> getSettingNames() {
        return getSettingNames(null);
    }

    /**
     * Returns the list of settings whose name starts with the specified prefix. If
     * the prefix is null or empty, all settings names are returned.
     *
     * @param startingWith
     * @return list of setting names
     */
    public List<String> getSettingNames(String startingWith) {
        List<String> names = new ArrayList<>();
        Properties props = getProperties();
        if (StringUtils.isNullOrEmpty(startingWith)) {
            names.addAll(props.stringPropertyNames());
        } else {
            startingWith = startingWith.toLowerCase();
            for (Object o : props.keySet()) {
                String name = o.toString();
                if (name.toLowerCase().startsWith(startingWith)) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    /**
     * Returns the string value for the specified name. If the name does not exist
     * or the value for the name can not be interpreted as a string, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public String getString(String name, String defaultValue) {
        String value = getProperties().getProperty(name, defaultValue);
        value = overrides.getProperty(name, value);

        return value;
    }

    /**
     * Returns a string value that has been interpolated from System Properties
     * and Environment Variables using the ${name} or @{name} syntax.
     *
     * @param name
     * @param defaultValue
     * @return an interpolated string
     */
    public String getInterpolatedString(String name, String defaultValue) {
        String value = getString(name, defaultValue);
        return interpolateString(value);
    }

    /**
     * Returns the boolean value for the specified name. If the name does not
     * exist or the value for the name can not be interpreted as a boolean, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = getString(name, null);
        if (!StringUtils.isNullOrEmpty(value)) {
            return Boolean.parseBoolean(value.trim());
        }

        return defaultValue;
    }

    /**
     * Returns the integer value for the specified name. If the name does not
     * exist or the value for the name can not be interpreted as an integer, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public int getInteger(String name, int defaultValue) {
        try {
            String value = getString(name, null);
            if (!StringUtils.isNullOrEmpty(value)) {
                return Integer.parseInt(value.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer for " + name + USING_DEFAULT_OF
                + defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the long value for the specified name. If the name does not
     * exist or the value for the name can not be interpreted as an long, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public long getLong(String name, long defaultValue) {
        try {
            String value = getString(name, null);
            if (!StringUtils.isNullOrEmpty(value)) {
                return Long.parseLong(value.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long for " + name + USING_DEFAULT_OF
                + defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the float value for the specified name. If the name does not
     * exist or the value for the name can not be interpreted as a float, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public float getFloat(String name, float defaultValue) {
        try {
            String value = getString(name, null);
            if (!StringUtils.isNullOrEmpty(value)) {
                return Float.parseFloat(value.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse float for " + name + USING_DEFAULT_OF
                + defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the double value for the specified name. If the name does not
     * exist or the value for the name can not be interpreted as a double, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public double getDouble(String name, double defaultValue) {
        try {
            String value = getString(name, null);
            if (!StringUtils.isNullOrEmpty(value)) {
                return Double.parseDouble(value.trim());
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double for " + name + USING_DEFAULT_OF
                + defaultValue);
        }

        return defaultValue;
    }

    /**
     * Returns the char value for the specified name. If the name does not exist
     * or the value for the name can not be interpreted as a char, the
     * defaultValue is returned.
     *
     * @param name
     * @param defaultValue
     * @return name value or defaultValue
     */
    public char getChar(String name, char defaultValue) {
        String value = getString(name, null);
        if (!StringUtils.isNullOrEmpty(value)) {
            return value.trim().charAt(0);
        }

        return defaultValue;
    }

    /**
     * Returns the string value for the specified name.  If the name does not
     * exist an exception is thrown.
     *
     * @param name
     * @return name value
     */
    public String getRequiredString(String name) {
        String value = getString(name, null);
        if (value != null) {
            return value.trim();
        }
        throw new PippoRuntimeException("Setting '{}' has not been configured!", name);
    }

    /**
     * Returns a list of comma-delimited strings from the specified name.
     *
     * @param name
     * @return list of strings
     */
    public List<String> getStrings(String name) {
        return getStrings(name, DEFAULT_LIST_DELIMITER);
    }

    /**
     * Returns a list of strings from the specified name using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of strings
     */
    public List<String> getStrings(String name, String delimiter) {
        String value = getString(name, null);
        if (StringUtils.isNullOrEmpty(value)) {
            return Collections.emptyList();
        }

        value = value.trim();
        // to handles cases where value is specified like [a,b, c]
        if (value.startsWith("[") && value.endsWith("]")) {
            value = value.substring(1, value.length() - 1);
        }
        return StringUtils.getList(value, delimiter);
    }

    /**
     * Returns a list of comma-delimited integers from the specified name.
     *
     * @param name
     * @return list of integers
     */
    public List<Integer> getIntegers(String name) {
        return getIntegers(name, DEFAULT_LIST_DELIMITER);
    }

    /**
     * Returns a list of integers from the specified name using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of integers
     */
    public List<Integer> getIntegers(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);

        List<Integer> ints = new ArrayList<>(strings.size());
        for (String value : strings) {
            try {
                int i = Integer.parseInt(value);
                ints.add(i);
            } catch (NumberFormatException e) {
            }
        }

        return Collections.unmodifiableList(ints);
    }

    /**
     * Returns a list of comma-delimited longs from the specified name.
     *
     * @param name
     * @return list of longs
     */
    public List<Long> getLongs(String name) {
        return getLongs(name, DEFAULT_LIST_DELIMITER);
    }

    /**
     * Returns a list of longs from the specified name using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of longs
     */
    public List<Long> getLongs(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);

        List<Long> longs = new ArrayList<>(strings.size());
        for (String value : strings) {
            try {
                long i = Long.parseLong(value);
                longs.add(i);
            } catch (NumberFormatException e) {
            }
        }

        return Collections.unmodifiableList(longs);
    }

    /**
     * Returns a list of comma-delimited floats from the specified name.
     *
     * @param name
     * @return list of floats
     */
    public List<Float> getFloats(String name) {
        return getFloats(name, DEFAULT_LIST_DELIMITER);
    }

    /**
     * Returns a list of floats from the specified name using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of floats
     */
    public List<Float> getFloats(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);

        List<Float> floats = new ArrayList<>(strings.size());
        for (String value : strings) {
            try {
                float i = Float.parseFloat(value);
                floats.add(i);
            } catch (NumberFormatException e) {
            }
        }
        return Collections.unmodifiableList(floats);
    }

    /**
     * Returns a list of comma-delimited doubles from the specified name.
     *
     * @param name
     * @return list of doubles
     */
    public List<Double> getDoubles(String name) {
        return getDoubles(name, DEFAULT_LIST_DELIMITER);
    }

    /**
     * Returns a list of doubles from the specified name using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of doubles
     */
    public List<Double> getDoubles(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);

        List<Double> doubles = new ArrayList<>(strings.size());
        for (String value : strings) {
            try {
                double i = Double.parseDouble(value);
                doubles.add(i);
            } catch (NumberFormatException e) {
            }
        }
        return Collections.unmodifiableList(doubles);
    }

    /**
     * Gets the duration setting and converts it to milliseconds.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param name
     * @return milliseconds
     */
    public long getDurationInMilliseconds(String name) {
        return getDurationInMilliseconds(name, 0);
    }

    /**
     * Gets the duration setting and converts it to milliseconds.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @param defaultValue in milliseconds
     * @return milliseconds
     */
    public long getDurationInMilliseconds(String name, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(name, defaultValue + " MILLISECONDS");
        long duration = getLong(name, defaultValue);

        return timeUnit.toMillis(duration);
    }

    /**
     * Gets the duration setting and converts it to seconds.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @return seconds
     */
    public long getDurationInSeconds(String name) {
        return getDurationInSeconds(name, 0);
    }

    /**
     * Gets the duration setting and converts it to seconds.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @param defaultValue in seconds
     * @return seconds
     */
    public long getDurationInSeconds(String name, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(name, defaultValue + " SECONDS");
        long duration = getLong(name, defaultValue);

        return timeUnit.toSeconds(duration);
    }

    /**
     * Gets the duration setting and converts it to minutes.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param name
     * @return minutes
     */
    public long getDurationInMinutes(String name) {
        return getDurationInMinutes(name, 0);
    }

    /**
     * Gets the duration setting and converts it to minutes.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @param defaultValue in minutes
     * @return minutes
     */
    public long getDurationInMinutes(String name, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(name, defaultValue + " MINUTES");
        long duration = getLong(name, defaultValue);

        return timeUnit.toMinutes(duration);
    }

    /**
     * Gets the duration setting and converts it to hours.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param name
     * @return hours
     */
    public long getDurationInHours(String name) {
        return getDurationInHours(name, 0);
    }

    /**
     * Gets the duration setting and converts it to hours.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @param defaultValue in hours
     * @return hours
     */
    public long getDurationInHours(String name, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(name, defaultValue + " HOURS");
        long duration = getLong(name, defaultValue);

        return timeUnit.toHours(duration);
    }

    /**
     * Gets the duration setting and converts it to days.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param name
     * @return days
     */
    public long getDurationInDays(String name) {
        return getDurationInDays(name, 0);
    }

    /**
     * Gets the duration setting and converts it to days.
     * <p/>
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param name
     * @param defaultValue in days
     * @return days
     */
    public long getDurationInDays(String name, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(name, defaultValue + " DAYS");
        long duration = getLong(name, defaultValue);

        return timeUnit.toDays(duration);
    }

    /**
     * Extracts the TimeUnit from the name.
     *
     * @param name
     * @param defaultValue
     * @return the extracted TimeUnit
     */
    private TimeUnit extractTimeUnit(String name, String defaultValue) {
        String value = getString(name, defaultValue);
        try {
            final String[] s = value.split(" ", 2);
            return TimeUnit.valueOf(s[1].trim().toUpperCase());
        } catch (Exception e) {
            throw new PippoRuntimeException("{} must have format '<n> <TimeUnit>' where <TimeUnit> is one of 'MILLISECONDS', 'SECONDS', 'MINUTES', 'HOURS', 'DAYS'", name);
        }
    }

    /**
     * Tests for the existence of a setting.
     *
     * @param name
     * @return true if the setting exists
     */
    public boolean hasSetting(String name) {
        return getString(name, null) != null;
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, boolean value) {
        overrides.put(name, Boolean.toString(value));
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, String value) {
        overrides.put(name, value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, char value) {
        overrides.put(name, Character.toString(value));
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, int value) {
        overrides.put(name, Integer.toString(value));
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, long value) {
        overrides.put(name, Long.toString(value));
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, float value) {
        overrides.put(name, Float.toString(value));
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param name
     * @param value
     */
    public void overrideSetting(String name, double value) {
        overrides.put(name, Double.toString(value));
    }

}
