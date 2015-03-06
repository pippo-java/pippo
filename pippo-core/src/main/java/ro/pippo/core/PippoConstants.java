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

import ro.pippo.core.util.ClasspathUtils;
import ro.pippo.core.util.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class PippoConstants {

    public static final String LOCATION_OF_PIPPO_MIMETYPE_PROPERTIES = "pippo/mime-types.properties";

    public static final String LOCATION_OF_PIPPO_BUILTIN_PROPERTIES = "pippo/pippo-builtin.properties";

    public static final String LOCATION_OF_PIPPO_CLASSPATH_PROPERTIES = "conf/application.properties";

    public static final String APPLICATION_PROPERTIES = "application.properties";

    public static final String SYSTEM_PROPERTY_PIPPO_MODE = "pippo.mode";

    public static final String SYSTEM_PROPERTY_PIPPO_SETTINGS = "pippo.settings";

    public static final String SETTING_APPLICATION_NAME = "application.name";

    public static final String SETTING_APPLICATION_VERSION = "application.version";

    public static final String SETTING_APPLICATION_LANGUAGES = "application.languages";

    public static final String SETTING_APPLICATION_COOKIE_PREFIX = "application.cookie.prefix";

    public static final String SETTING_HTTP_CACHE_CONTROL = "http.cacheControl";

    public static final String SETTING_HTTP_USE_ETAG = "http.useETag";

    public static final String SETTING_MIMETYPE_PREFIX = "mimetype.";

    public static final String SETTING_TEMPLATE_PATH_PREFIX = "template.pathPrefix";

    public static final String SETTING_SERVER_PORT = "server.port";

    public static final String SETTING_SERVER_HOST = "server.host";

    public static final String SETTING_SERVER_CONTEXT_PATH = "server.contextPath";

    public static final String SETTING_SERVER_KEYSTORE_FILE = "server.keystoreFile";

    public static final String SETTING_SERVER_KEYSTORE_PASSWORD = "server.keystorePassword";

    public static final String SETTING_SERVER_TRUSTSTORE_FILE = "server.truststoreFile";

    public static final String SETTING_SERVER_TRUSTSTORE_PASSWORD = "server.truststorePassword";

    public static final String REQUEST_PARAMETER_LANG = "lang";

    public static final String REQUEST_PARAMETER_LOCALE = "locale";

    public static final String UTF8 = StandardCharsets.UTF_8.toString();

    public static final String PIPPO_LOGO = "\n"
        + " ____  ____  ____  ____  _____\n"
        + "(  _ \\(_  _)(  _ \\(  _ \\(  _  )\n"
        + " ) __/ _)(_  ) __/ ) __/ )(_)(   http://pippo.ro\n"
        + "(__)  (____)(__)  (__)  (_____)  {}\n";

    public static String getPippoLogo() {
        return StringUtils.format(PIPPO_LOGO, getPippoVersion());
    }

    /**
     * Simply reads a property resource file that contains the version of this
     * Pippo build. Helps to identify the Pippo version currently running.
     *
     * @return The version of Pippo. Eg. "1.6-SNAPSHOT" while developing of "1.6" when released.
     */
    public static String getPippoVersion() {
        // and the key inside the properties file.
        String PIPPO_VERSION_PROPERTY_KEY = "pippo.version";

        String pippoVersion;

        try {
            Properties prop = new Properties();
            URL url = ClasspathUtils.locateOnClasspath(PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
            InputStream stream = url.openStream();
            prop.load(stream);

            pippoVersion = prop.getProperty(PIPPO_VERSION_PROPERTY_KEY);
        } catch (Exception e) {
            //this should not happen. Never.
            throw new PippoRuntimeException("Something is wrong with your build. Cannot find resource {}",
                PippoConstants.LOCATION_OF_PIPPO_BUILTIN_PROPERTIES);
        }

        return pippoVersion;
    }


    private PippoConstants() {
        // restrict instantiation
    }
}
