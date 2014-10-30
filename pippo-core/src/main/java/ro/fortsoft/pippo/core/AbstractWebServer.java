/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractWebServer implements WebServer {

    protected WebServerSettings settings;
    protected PippoFilter pippoFilter;

    @Override
    public WebServerSettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(WebServerSettings settings) {
        this.settings = settings;
    }

    @Override
    public PippoFilter getPippoFilter() {
        return pippoFilter;
    }

    @Override
    public void setPippoFilter(PippoFilter pippoFilter) {
        this.pippoFilter = pippoFilter;
    }

}
