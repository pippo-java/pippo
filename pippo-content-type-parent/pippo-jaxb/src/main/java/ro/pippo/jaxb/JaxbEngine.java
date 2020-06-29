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
package ro.pippo.jaxb;

import org.kohsuke.MetaInfServices;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * An XmlEngine based on JAXB.
 *
 * @author James Moger
 *
 * @see https://docs.oracle.com/javase/8/docs/technotes/guides/security/jaxp/jaxp.html#jaxp-properties-for-processing-limits
 * @see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
 */
@MetaInfServices
public class JaxbEngine implements ContentTypeEngine {

    boolean prettyPrint;
    private final XMLInputFactory xmlInputFactory;

    public JaxbEngine() {
        xmlInputFactory = buildXMLInputFactory();
    }

    @Override
    public void init(Application application) {
        prettyPrint = application.getPippoSettings().isDev();
        // JAXBContext is thread-safe - it is possible to cache by class
        // https://javaee.github.io/jaxb-v2/doc/user-guide/ch03.html#other-miscellaneous-topics-performance-and-thread-safety
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
            throw new PippoRuntimeException(e, "Failed to serialize '{}' to XML", object.getClass().getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromString(String content, Class<T> classOfT) {
        try (StringReader reader = new StringReader(content)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(classOfT);

            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            return (T) unmarshaller.unmarshal(xmlStreamReader);
        } catch (JAXBException | XMLStreamException e) {
            throw new PippoRuntimeException(e, "Failed to deserialize content to '{}'", classOfT.getName());
        }
    }

    /**
     * Create a new instance of the factory with some configurations.
     *
     * @return the factory implementation
     *
     * @see XMLInputFactory#newFactory()
     */
    protected XMLInputFactory buildXMLInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        return xmlInputFactory;
    }

    /**
     * Allows the user to set specific feature/property to the parser.
     *
     * @param name  The name of the property
     * @param value The value of the property
     */
    void setProperty(String name, Object value) {
        xmlInputFactory.setProperty(name, value);
    }

}
