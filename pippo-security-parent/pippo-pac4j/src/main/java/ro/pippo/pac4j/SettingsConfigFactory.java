/*
 * Copyright (C) 2017 the original author or authors.
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
package ro.pippo.pac4j;

import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.config.Config;
import ro.pippo.core.PippoSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It's a {@link PropertiesConfigFactory} that uses {@link PippoSettings} to build a
 * PAC4J configuration.
 * Take a look at <a href="http://www.pac4j.org/docs/config.html#2-the-pac4j-config-module">The pac4j-config module</a>.
 *
 * @author Decebal Suiu
 */
public class SettingsConfigFactory extends PropertiesConfigFactory {

    /**
     * Call {@link SettingsConfigFactory#SettingsConfigFactory(PippoSettings, String)} with {@code pac4j.} as prefix.
     *
     * @param settings
     */
    public SettingsConfigFactory(PippoSettings settings) {
        this(settings, "pac4j.");
    }

    public SettingsConfigFactory(PippoSettings settings, String prefix) {
        super(getPac4jSettings(settings, prefix));
    }

    @Override
    public Config build(Object... parameters) {
        Config config = super.build(parameters);
        config.setHttpActionAdapter(PippoNopHttpActionAdapter.INSTANCE);

        return config;
    }

    private static Map<String, String> getPac4jSettings(PippoSettings settings, String prefix) {
        Map<String, String> pac4jSettings = new HashMap<>();

        int beginIndex = prefix.length();
        List<String> names = settings.getSettingNames(prefix);
        for (String name : names) {
            pac4jSettings.put(name.substring(beginIndex), settings.getString(name, ""));
        }

        return pac4jSettings;
    }

}
