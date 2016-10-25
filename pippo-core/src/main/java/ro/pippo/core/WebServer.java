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

import javax.servlet.ServletContextListener;

/**
 * @author Decebal Suiu
 */
public interface WebServer<T extends WebServerSettings> {

    T getSettings();

    PippoFilter getPippoFilter();

    WebServer<T> setPippoFilter(PippoFilter pippoFilter);

    String getPippoFilterPath();

    /**
     * The <code>pippoFilterPath</code> must start with <code>"/"</code> and end with <code>"/*"</code>.
     * For example: <code>/*, /app/*</code>
     *
     * @param pippoFilterPath
     * @return
     */
    WebServer<T> setPippoFilterPath(String pippoFilterPath);

    WebServer<T> init(PippoSettings pippoSettings);

    void start();

    void stop();

    /**
     * Add a {@link ServletContextListener} programmatically.
     * The preferred approach is to use {@link WebServerInitializer}.
     *
     * @param listener
     * @return
     */
    WebServer addListener(Class<? extends ServletContextListener> listener);

}
