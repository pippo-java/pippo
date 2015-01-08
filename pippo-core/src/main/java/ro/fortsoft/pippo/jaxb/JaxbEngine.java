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
package ro.fortsoft.pippo.jaxb;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.ContentTypeEngine;
import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * An XmlEngine based on JAXB.
 *
 * @author James Moger
 */
public class JaxbEngine implements ContentTypeEngine {

    boolean prettyPrint;

    @Override
    public void init(Application application) {
        prettyPrint = application.getPippoSettings().isDev();
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_XML;
    }

    @Override
    public String toString(Object object) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, prettyPrint);

            StringWriter writer = new StringWriter();
            jaxbMarshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new PippoRuntimeException("Failed to serialize '{}' to XML'", e, object.getClass().getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromString(String content, Class<T> classOfT) {
        try (StringReader reader = new StringReader(content)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(classOfT);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            T t = (T) jaxbUnmarshaller.unmarshal(reader);
            return t;
        } catch (JAXBException e) {
            throw new PippoRuntimeException("Failed to deserialize content to '{}'", e, classOfT.getName());
        }
    }

}
