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

/**
 * @author Decebal Suiu
 */
public abstract class AbstractWebServer<T extends WebServerSettings> implements WebServer {

    protected PippoFilter pippoFilter;
    protected String pippoFilterPath;

    protected PippoSettings pippoSettings;
    private T settings;

    protected abstract T createDefaultSettings();

    @Override
    public T getSettings() {
        if (settings == null) {
            settings = createDefaultSettings();
        }

        return settings;
    }

    @Override
    public PippoFilter getPippoFilter() {
        return pippoFilter;
    }

    @Override
    public void setPippoFilter(PippoFilter pippoFilter) {
        this.pippoFilter = pippoFilter;
    }

    @Override
    public String getPippoFilterPath() {
        return pippoFilterPath;
    }

    @Override
    public void setPippoFilterPath(String pippoFilterPath) {
        this.pippoFilterPath = pippoFilterPath;
    }

    @Override
    public void init(PippoSettings pippoSettings) {
        this.pippoSettings = pippoSettings;
    }

}
