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
package ro.fortsoft.pippo.snakeyaml;

import org.yaml.snakeyaml.Yaml;
import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.ContentTypeEngine;
import ro.fortsoft.pippo.core.HttpConstants;

/**
 * An YAML content-type engine based on SnakeYAML.
 *
 * @author James Moger
 */
public class SnakeYamlEngine implements ContentTypeEngine {

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_X_YAML;
    }

    @Override
    public String toString(Object object) {
        String content = new Yaml().dump(object);
        return content;
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        Object o = new Yaml().load(content);
        return (T) o;
    }

}
