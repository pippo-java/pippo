/*
 * Copyright (C) 2015 the original author or authors.
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
package ro.pippo.jetty;

import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Decebal Suiu, Gustavo Galvan
 */
public class JettySettings extends WebServerSettings {

    public static final String MAX_THREADS = "jetty.maxThreads";
    public static final String MIN_THREADS = "jetty.minThreads";
    public static final String IDLE_TIMEOUT = "jetty.idleTimeout";

    private int maxThreads = 200;
    private int minThreads = 8;
    private int idleTimeout = 30000; // in miliseconds

    private Set<String> modified;

    public JettySettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        modified = new HashSet<>();
        if (pippoSettings.hasSetting(JettySettings.MAX_THREADS)) {
            modified.add(JettySettings.MAX_THREADS);
        }
        if (pippoSettings.hasSetting(JettySettings.MIN_THREADS)) {
            modified.add(JettySettings.MIN_THREADS);
        }
        if (pippoSettings.hasSetting(JettySettings.IDLE_TIMEOUT)) {
            modified.add(JettySettings.IDLE_TIMEOUT);
        }

        maxThreads = pippoSettings.getInteger(JettySettings.MAX_THREADS, maxThreads);
        minThreads = pippoSettings.getInteger(JettySettings.MIN_THREADS, minThreads);
        idleTimeout = pippoSettings.getInteger(JettySettings.IDLE_TIMEOUT, idleTimeout);
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getMinThreads() {
        return minThreads;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public JettySettings maxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        modified.add(JettySettings.MAX_THREADS);

        return this;
    }

    public JettySettings minThreads(int minThreads) {
        this.minThreads = minThreads;
        modified.add(JettySettings.MIN_THREADS);

        return this;
    }

    public JettySettings idleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
        modified.add(JettySettings.IDLE_TIMEOUT);

        return this;
    }

    public boolean isModified(String settingName) {
        return modified.contains(settingName);
    }

    public boolean isModified() {
        return !modified.isEmpty();
    }

}
