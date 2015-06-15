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
package ro.pippo.undertow;

import ro.pippo.core.PippoSettings;
import ro.pippo.core.WebServerSettings;

/**
 * @author Decebal Suiu
 */
public class UndertowSettings extends WebServerSettings {

    public static final String BUFFER_SIZE = "undertow.bufferSize";

    private int bufferSize;

    public UndertowSettings(PippoSettings pippoSettings) {
        super(pippoSettings);

        bufferSize = pippoSettings.getInteger(UndertowSettings.BUFFER_SIZE, 0);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public UndertowSettings setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;

        return this;
    }

}
