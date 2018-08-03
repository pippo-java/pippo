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
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Response;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Displays settings.
 * See {@link PippoSettings}.
 *
 * @author Decebal Suiu
 */
public class SettingsHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(SettingsHandler.class);

    private boolean maskingPassword = true;
    private String settingNamePasswordToken;

    public SettingsHandler() {
        this("password");
    }
    
    public SettingsHandler(boolean maskingPassword) {
        this.maskingPassword = maskingPassword;
    }

    public SettingsHandler(String settingNamePasswordToken) {
        this.settingNamePasswordToken = settingNamePasswordToken;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Map<String, String> settingsMap = settingsToMap(routeContext.getSettings());

        Response response = routeContext.getResponse().noCache().text();

        try (BufferedWriter writer = new BufferedWriter(response.getWriter())) {
            writeSettings(settingsMap, writer);
            writer.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected Map<String, String> settingsToMap(PippoSettings settings) {
        List<String> settingNames = settings.getSettingNames();
        Map<String, String> settingsMap = new TreeMap<>();
        for (String settingName : settingNames) {
            settingsMap.put(settingName, settings.getRequiredString(settingName));
        }

        return settingsMap;
    }

    protected void writeSettings(Map<String, String> settings, BufferedWriter writer) throws IOException {
        Set<String> names = new TreeSet<>(settings.keySet());
        for (String name : names) {
            if (maskingPassword && name.contains(settingNamePasswordToken)) {
                writer.write(name + " = **********");
            } else {
                writer.write(name + " = " + settings.get(name));
            }
            writer.newLine();
        }
    }

}
