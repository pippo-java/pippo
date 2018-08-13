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
package ro.pippo.pebble;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.ClasspathUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

class PippoTemplateLoader extends ClasspathLoader {

    @Override
    public Reader getReader(String templateName) throws LoaderException {
        StringBuilder path = new StringBuilder();
        if (getPrefix() != null) {
            path.append(getPrefix());

            if (!getPrefix().endsWith(String.valueOf('/'))) {
                path.append('/');
            }
        }

        String resource = path.toString() + templateName + (getSuffix() == null ? "" : getSuffix());
        String location = resource;
        if (resource.charAt(0) == '/') {
            location = resource.substring(1);
        }

        URL url = ClasspathUtils.locateOnClasspath(location);
        if (url == null) {
            throw new LoaderException(null, "Could not find template \"" + location + "\"");
        }

        Reader reader;
        try {
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is, getCharset());
            reader = new BufferedReader(isr);
        } catch (IOException e) {
            throw new PippoRuntimeException(e);
        }

        return reader;
    }

}
