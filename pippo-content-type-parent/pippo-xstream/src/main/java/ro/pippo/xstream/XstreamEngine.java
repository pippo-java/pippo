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
package ro.pippo.xstream;

import org.kohsuke.MetaInfServices;
import ro.pippo.core.Application;
import ro.pippo.core.ContentTypeEngine;
import ro.pippo.core.HttpConstants;

import com.thoughtworks.xstream.XStream;

/**
 * An XmlEngine based on XStream.
 *
 * @author James Moger
 */
@MetaInfServices
public class XstreamEngine implements ContentTypeEngine {

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.APPLICATION_XML;
    }

    private XStream xstream() {
        XStream xstream = new XStream();
        // allow annotations on models for maximum flexibility
        xstream.autodetectAnnotations(true);
        // prevent xstream from creating complex XML graphs
        xstream.setMode(XStream.NO_REFERENCES);
        return xstream;
    }

    @Override
    public String toString(Object object) {
        return xstream().toXML(object);
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        return (T) xstream().fromXML(content);
    }

}
