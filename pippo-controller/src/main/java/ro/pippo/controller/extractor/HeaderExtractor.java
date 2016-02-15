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
package ro.pippo.controller.extractor;

import ro.pippo.controller.Context;
import ro.pippo.controller.Header;
import ro.pippo.core.ParameterValue;

/**
 * @author James Moger
 */
public class HeaderExtractor extends DefaultObjectExtractor
        implements NamedExtractor, PatternExtractor, ConfigurableExtractor<Header> {

    private String name;
    private String pattern;

    public Class<Header> getAnnotationClass() {
        return Header.class;
    }

    @Override
    public void configure(Header header) {
        setName(header.value());
        setPattern(header.pattern());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Object extract(Context context) {
        ParameterValue parameterValue = new ParameterValue(context.getHeader(name));
        if (collectionType == null) {
            return parameterValue.to(objectType, pattern);
        }

        return parameterValue.toCollection(collectionType, objectType, pattern);
    }

}
