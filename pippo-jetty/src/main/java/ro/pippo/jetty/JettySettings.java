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

/**
 * @author Decebal Suiu, Gustavo Galvan
 */
public class JettySettings extends WebServerSettings {

    public static final String MAX_THREADS = "jetty.maxThreads";
    public static final String MIN_THREADS = "jetty.minThreads";
    public static final String IDLE_TIMEOUT = "jetty.idleTimeout";

    public static final int DEFAULT_MAX_THREADS = 200;
    public static final int DEFAULT_MIN_THREADS = 8;
    public static final int DEFAULT_IDLE_TIMEOUT = 30000;

    private int maxThreads;
    private int minThreads;
    private int idleTimeout; // in miliseconds

    public JettySettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        maxThreads = pippoSettings.getInteger(JettySettings.MAX_THREADS, 0);
        minThreads = pippoSettings.getInteger(JettySettings.MIN_THREADS, 0);
        idleTimeout = pippoSettings.getInteger(JettySettings.IDLE_TIMEOUT, 0);
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
        return this;
    }

    public JettySettings minThreads(int minThreads) {
        this.minThreads = minThreads;
        return this;
    }

    public JettySettings idleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

}
