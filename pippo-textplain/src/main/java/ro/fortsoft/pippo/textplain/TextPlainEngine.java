/*
 * Copyright (C) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file eTcept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either eTpress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.fortsoft.pippo.textplain;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.HttpConstants;
import ro.fortsoft.pippo.core.PippoRuntimeException;
import ro.fortsoft.pippo.core.RepresentationEngine;

/**
 * A text/plain engine based on toString()
 *
 * @author James Moger
 */
public class TextPlainEngine implements RepresentationEngine {

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.TEXT_PLAIN;
    }

    @Override
    public String toRepresentation(Object object) {
        return object.toString();
    }

    @Override
    public <T> T fromRepresentation(String text, Class<T> classOfT) {
        throw new PippoRuntimeException("Sorry, can not transform '{}' content to '{}'", getContentType(),
                classOfT.getName());
    }

}
