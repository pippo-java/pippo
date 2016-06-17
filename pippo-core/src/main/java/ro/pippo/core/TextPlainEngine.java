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
package ro.pippo.core;


import java.nio.charset.StandardCharsets;

/**
 * A text/plain engine based on toString()
 *
 * @author James Moger
 */
public class TextPlainEngine implements ContentTypeEngine {

    @Override
    public void init(Application application) {
    }

    @Override
    public String getContentType() {
        return HttpConstants.ContentType.TEXT_PLAIN;
    }

    @Override
    public String toString(Object object) {
        return object.toString();
    }

    @Override
    public <T> T fromString(String content, Class<T> classOfT) {
        if (String.class.equals(classOfT)) {
            return (T) content;
        } else if (CharSequence.class.equals(classOfT)) {
            return (T) content;
        } else if (char[].class.equals(classOfT)) {
            return (T) content.toCharArray();
        } else if (byte[].class.equals(classOfT)) {
            return (T) content.getBytes(StandardCharsets.UTF_8);
        }

        throw new PippoRuntimeException("Sorry, can not transform '{}' content to '{}'", getContentType(),
            classOfT.getName());
    }

}
