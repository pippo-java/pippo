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

import java.nio.charset.StandardCharsets;

public final class PippoConstants {

    public static final String LOCATION_OF_PIPPO_MIMETYPE_PROPERTIES = "pippo/mime-types.properties";

    public static final String LOCATION_OF_PIPPO_BUILTIN_PROPERTIES = "pippo/pippo-builtin.properties";

    public static final String LOCATION_OF_PIPPO_CLASSPATH_PROPERTIES = "conf/application.properties";

    public static final String APPLICATION_PROPERTIES = "application.properties";

    // SYSTEM PROPERTIES

    public static final String SYSTEM_PROPERTY_PIPPO_MODE = "pippo.mode";

    public static final String SYSTEM_PROPERTY_PIPPO_SETTINGS = "pippo.settings";

    public static final String SYSTEM_PROPERTY_APPLICATION_CLASS_NAME = "pippo.applicationClassName";

    public static final String SYSTEM_PROPERTY_RELOAD_ENABLED = "pippo.reload.enabled";

    public static final String SYSTEM_PROPERTY_RELOAD_TARGET_CLASSES = "pippo.reload.targetClasses";

    public static final String SYSTEM_PROPERTY_RELOAD_ROOT_PACKAGE_NAME = "pippo.reload.rootPackageName";

    // SETTINGS

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

    public static final String SETTING_TEMPLATE_EXTENSION = "template.extension";

    // OTHERS

    public static final String UTF8 = StandardCharsets.UTF_8.toString();

    private PippoConstants() {
        // restrict instantiation
    }

}
