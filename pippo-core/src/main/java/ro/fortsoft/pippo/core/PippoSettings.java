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
package ro.fortsoft.pippo.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pippo.core.util.Utils;

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
 *
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
 *
 */
public class PippoSettings {

    private final Logger log = LoggerFactory.getLogger(PippoSettings.class);

    private final String defaultListDelimiter = ",";

    private final Properties properties;

    private final Properties overrides;

    private final URL propertiesUrl;

    private final boolean isFile;

    private volatile long lastModified;

    public PippoSettings() {
        propertiesUrl = getPropertiesUrl();
        isFile = propertiesUrl.getProtocol().equals("file:");
        properties = loadProperties(propertiesUrl);
        overrides = new Properties();
    }

    private URL getPropertiesUrl() {
        URL url = null;
        try {
            // System property
            String systemProperty = System.getProperty(PippoConstant.SYSTEM_PROPERTY_PIPPO_SETTINGS, null);
            if (!Utils.isNullOrEmpty(systemProperty)) {
                url = URI.create(systemProperty).toURL();
                log.debug("Located '{}' using the '{}' system property", url, PippoConstant.SYSTEM_PROPERTY_PIPPO_SETTINGS);
            }
        } catch (MalformedURLException e) {
            log.error("Failed to parse '{}' system property!", PippoConstant.SYSTEM_PROPERTY_PIPPO_SETTINGS);
        }

        try {
            // working directory
            Path path = Paths.get(System.getProperty("user.dir"), PippoConstant.APPLICATION_PROPERTIES);
            if (path.toFile().exists()) {
                url = path.toUri().toURL();
                log.debug("Located '{}' in the current working directory", url);
            }
        } catch (MalformedURLException e) {
            log.error("Failed to parse working directory properties!", e);
        }

        if (url == null) {
            // try locating an application classpath properties file
            url = Utils.locateOnClasspath(PippoConstant.LOCATION_OF_PIPPO_CLASSPATH_PROPERTIES);
        }

        if (url == null) {
            // last resort, use built-in properties
            url = Utils.locateOnClasspath(PippoConstant.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
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
        try (InputStream is = propertiesUrl.openStream()) {
            log.debug("loading {}", propertiesUrl);
            Properties props = new Properties();
            props.load(is);

            File baseDir;
            if (isFile) {
                baseDir = new File(propertiesUrl.getPath()).getParentFile();
            } else {
                // use current working directory for includes
                baseDir = new File(System.getProperty("user.dir"));
            }

            // allow including other properties files
            props = loadIncludes(baseDir, props);

            // collect settings for the current runtime mode
            String prefix = "%" + RuntimeMode.getCurrent().toString() + ".";

            // sort keys, mode-specific keys are first
            Set<String> keys = new TreeSet<>(props.stringPropertyNames());
            for (String key : keys) {
                if (key.charAt(0) == '%') {
                    // mode-specific setting
                    if (key.startsWith(prefix)) {
                        // setting for the current runtime mode
                        properties.put(key.substring(prefix.length()), props.get(key));
                    }
                } else if (!properties.containsKey(key)) {
                    // setting for all modes
                    properties.put(key, props.get(key));
                }
            }

        } catch (FileNotFoundException f) {
            throw new PippoRuntimeException("Failed to find " + propertiesUrl, f);
        } catch (Exception t) {
            throw new PippoRuntimeException("Failed to read " + propertiesUrl, t);
        }

        return properties;
    }

    /**
     * Recursively read "include" properties files.
     *
     * @param baseDir
     * @param properties
     * @return the merged properties
     * @throws IOException
     */
    private Properties loadIncludes(File baseDir, Properties properties) throws IOException {

        Properties baseProperties = new Properties();

        String include = (String) properties.remove("include");
        if (!Utils.isNullOrEmpty(include)) {

            // allow for multiples
            List<String> names = Utils.getStringList(include, defaultListDelimiter);
            for (String name : names) {

                if (Utils.isNullOrEmpty(name)) {
                    continue;
                }

                // try co-located
                File file = new File(baseDir, name.trim());
                if (!file.exists()) {
                    // try absolute path
                    file = new File(name.trim());
                }

                if (!file.exists()) {
                    log.warn("failed to locate {}", file);
                    continue;
                }

                // load properties
                log.debug("loading includes from {}", file);
                try (FileInputStream iis = new FileInputStream(file)) {
                    baseProperties.load(iis);
                }

                // read nested includes
                baseProperties = loadIncludes(file.getParentFile(), baseProperties);

            }

        }

        // includes are "default" properties, they must be set first and the
        // props which specified the "includes" must override
        Properties merged = new Properties();
        merged.putAll(baseProperties);
        merged.putAll(properties);

        return merged;
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
        List<String> keys = new ArrayList<String>();
        Properties props = getProperties();
        if (Utils.isNullOrEmpty(startingWith)) {
            keys.addAll(props.stringPropertyNames());
        } else {
            startingWith = startingWith.toLowerCase();
            for (Object o : props.keySet()) {
                String key = o.toString();
                if (key.toLowerCase().startsWith(startingWith)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    /**
     * Returns the string value for the specified key. If the key does not exist
     * or the value for the key can not be interpreted as a string, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public String getString(String name, String defaultValue) {
        String value = getProperties().getProperty(name, defaultValue);
        value = overrides.getProperty(name, value);
        return value;
    }

    /**
     * Returns the boolean value for the specified key. If the key does not
     * exist or the value for the key can not be interpreted as a boolean, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = getString(name, null);
        if (!Utils.isNullOrEmpty(value)) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }

    /**
     * Returns the integer value for the specified key. If the key does not
     * exist or the value for the key can not be interpreted as an integer, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public int getInteger(String name, int defaultValue) {
        try {
            String value = getString(name, null);
            if (!Utils.isNullOrEmpty(value)) {
                return Integer.parseInt(value.trim().split(" ")[0]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer for " + name + " using default of "
                    + defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the long value for the specified key. If the key does not
     * exist or the value for the key can not be interpreted as an long, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public long getLong(String name, long defaultValue) {
        try {
            String value = getString(name, null);
            if (!Utils.isNullOrEmpty(value)) {
                return Long.parseLong(value.trim().split(" ")[0]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long for " + name + " using default of "
                    + defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the float value for the specified key. If the key does not
     * exist or the value for the key can not be interpreted as a float, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public float getFloat(String name, float defaultValue) {
        try {
            String value = getString(name, null);
            if (!Utils.isNullOrEmpty(value)) {
                return Float.parseFloat(value.trim().split(" ")[0]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse float for " + name + " using default of "
                    + defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the double value for the specified key. If the key does not
     * exist or the value for the key can not be interpreted as a double, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public double getDouble(String name, double defaultValue) {
        try {
            String value = getString(name, null);
            if (!Utils.isNullOrEmpty(value)) {
                return Double.parseDouble(value.trim().split(" ")[0]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double for " + name + " using default of "
                    + defaultValue);
        }
        return defaultValue;
    }

    /**
     * Returns the char value for the specified key. If the key does not exist
     * or the value for the key can not be interpreted as a char, the
     * defaultValue is returned.
     *
     * @param key
     * @param defaultValue
     * @return key value or defaultValue
     */
    public char getChar(String name, char defaultValue) {
        String value = getString(name, null);
        if (!Utils.isNullOrEmpty(value)) {
            return value.trim().charAt(0);
        }
        return defaultValue;
    }

    /**
     * Returns the string value for the specified key.  If the key does not
     * exist an exception is thrown.
     *
     * @param key
     * @return key value
     */
    public String getRequiredString(String name) {
        String value = getString(name, null);
        if (value != null) {
            return value.trim();
        }
        throw new PippoRuntimeException("Setting '" + name + "' has not been configured!");
    }

    /**
     * Returns a list of comma-delimited strings from the specified key.
     *
     * @param name
     * @return list of strings
     */
    public List<String> getStrings(String name) {
        return getStrings(name, defaultListDelimiter);
    }

    /**
     * Returns a list of strings from the specified key using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of strings
     */
    public List<String> getStrings(String name, String delimiter) {
        String value = getString(name, null);
        if (Utils.isNullOrEmpty(value)) {
            return Collections.emptyList();
        }
        List<String> stringList = Utils.getStringList(value, delimiter);
        return stringList;
    }

    /**
     * Returns a list of comma-delimited integers from the specified key.
     *
     * @param name
     * @return list of strings
     */
    public List<Integer> getIntegers(String name) {
        return getIntegers(name, defaultListDelimiter);
    }

    /**
     * Returns a list of integers from the specified key using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of integers
     */
    public List<Integer> getIntegers(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);
        if (strings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> ints = new ArrayList<Integer>();
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
     * Returns a list of comma-delimited longs from the specified key.
     *
     * @param name
     * @return list of strings
     */
    public List<Long> getLongs(String name) {
        return getLongs(name, defaultListDelimiter);
    }

    /**
     * Returns a list of longs from the specified key using the specified delimiter.
     *
     * @param name
     * @param delimiter
     * @return list of longs
     */
    public List<Long> getLongs(String name, String delimiter) {
        List<String> strings = getStrings(name, delimiter);
        if (strings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> longs = new ArrayList<Long>();
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
     * Gets the duration setting and converts it to milliseconds.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param key
     * @return milliseconds
     */
    public long getDurationInMilliseconds(String key) {
        return getDurationInMilliseconds(key, 0);
    }

    /**
     * Gets the duration setting and converts it to milliseconds.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @param defaultValue
     *            in milliseconds
     * @return milliseconds
     */
    public long getDurationInMilliseconds(String key, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(key, defaultValue + " MILLISECONDS");
        long duration =  getLong(key, defaultValue);
        return timeUnit.toMillis(duration);
    }

    /**
     * Gets the duration setting and converts it to seconds.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @return seconds
     */
    public long getDurationInSeconds(String key) {
        return getDurationInSeconds(key, 0);
    }

    /**
     * Gets the duration setting and converts it to seconds.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @param defaultValue
     *            in seconds
     * @return seconds
     */
    public long getDurationInSeconds(String key, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(key, defaultValue + " SECONDS");
        long duration =  getLong(key, defaultValue);
        return timeUnit.toSeconds(duration);
    }

    /**
     * Gets the duration setting and converts it to minutes.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param key
     * @return minutes
     */
    public long getDurationInMinutes(String key) {
        return getDurationInMinutes(key, 0);
    }

    /**
     * Gets the duration setting and converts it to minutes.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @param defaultValue
     *            in minutes
     * @return minutes
     */
    public long getDurationInMinutes(String key, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(key, defaultValue + " MINUTES");
        long duration =  getLong(key, defaultValue);
        return timeUnit.toMinutes(duration);
    }

    /**
     * Gets the duration setting and converts it to hours.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param key
     * @return hours
     */
    public long getDurationInHours(String key) {
        return getDurationInHours(key, 0);
    }

    /**
     * Gets the duration setting and converts it to hours.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @param defaultValue
     *            in hours
     * @return hours
     */
    public long getDurationInHours(String key, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(key, defaultValue + " HOURS");
        long duration =  getLong(key, defaultValue);
        return timeUnit.toHours(duration);
    }

    /**
     * Gets the duration setting and converts it to days.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li> n MILLISECONDS
     * <li> n SECONDS
     * <li> n MINUTES
     * <li> n HOURS
     * <li> n DAYS
     * </ul>
     *
     * @param key
     * @return days
     */
    public long getDurationInDays(String key) {
        return getDurationInDays(key, 0);
    }

    /**
     * Gets the duration setting and converts it to days.
     *
     * The setting must be use one of the following conventions:
     * <ul>
     * <li>n MILLISECONDS
     * <li>n SECONDS
     * <li>n MINUTES
     * <li>n HOURS
     * <li>n DAYS
     * </ul>
     *
     * @param key
     * @param defaultValue
     *            in days
     * @return days
     */
    public long getDurationInDays(String key, long defaultValue) {
        TimeUnit timeUnit = extractTimeUnit(key, defaultValue + " DAYS");
        long duration =  getLong(key, defaultValue);
        return timeUnit.toDays(duration);
    }

    /**
     * Extracts the TimeUnit from the key.
     *
     * @param key
     * @param defaultValue
     * @return the extracted TimeUnit
     */
    private TimeUnit extractTimeUnit(String key, String defaultValue) {
        String value = getString(key, defaultValue);
        try {
            final String[] s = value.split(" ", 2);
            TimeUnit timeUnit = TimeUnit.valueOf(s[1].trim().toUpperCase());
            return timeUnit;
        } catch (Exception ex) {
            throw new PippoRuntimeException(key + " must have format '<n> <TimeUnit>' where <TimeUnit> is one of 'MILLISECONDS', 'SECONDS', 'MINUTES', 'HOURS', 'DAYS'");
        }
    }

    /**
     * Tests for the existence of a setting.
     *
     * @param key
     * @return true if the setting exists
     */
    public boolean hasSetting(String key) {
        return getString(key, null) != null;
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, boolean value) {
        overrides.put(key, "" + value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, String value) {
        overrides.put(key, value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, char value) {
        overrides.put(key, "" + value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, int value) {
        overrides.put(key, "" + value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, long value) {
        overrides.put(key, "" + value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, float value) {
        overrides.put(key, "" + value);
    }

    /**
     * Override the setting at runtime with the specified value.
     * This change does not persist.
     *
     * @param key
     * @param value
     */
    public void overrideSetting(String key, double value) {
        overrides.put(key, "" + value);
    }

}
