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
package ro.pippo.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.kohsuke.MetaInfServices;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.HttpConstants;

/**
 * An XML ContentTypeEngine based on Jackson.
 *
 * @author James Moger
 */
@MetaInfServices(ContentTypeEngine.class)
public class JacksonXmlEngine extends JacksonBaseEngine {

    @Override
    protected ObjectMapper getObjectMapper() {
        // Check out: https://github.com/FasterXML/jackson-dataformat-xml
        JacksonXmlModule module = new JacksonXmlModule();
        // setDefaultUseWrapper produces more similar output to
        // the Json output. You can change that with annotations in your
        // models.
        module.setDefaultUseWrapper(false);
        return new XmlMapper(module);
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_XML;
    }

}
