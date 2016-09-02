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
package ro.pippo.jackson;

import org.kohsuke.MetaInfServices;

import ro.pippo.core.Application;
import ro.pippo.core.Initializer;
import ro.pippo.core.util.ClasspathUtils;

/**
 * @author James Moger
 */
@MetaInfServices
public class JacksonInitializer implements Initializer {

    @Override
    public void init(Application application) {
        application.registerContentTypeEngine(JacksonJsonEngine.class);
        if (ClasspathUtils.hasClass("com.fasterxml.jackson.dataformat.xml.XmlMapper")) {
            application.registerContentTypeEngine(JacksonXmlEngine.class);
        }
        if (ClasspathUtils.hasClass("com.fasterxml.jackson.dataformat.yaml.YAMLMapper")) {
            application.registerContentTypeEngine(JacksonYamlEngine.class);
        }
    }

    @Override
    public void destroy(Application application) {
    }

}
