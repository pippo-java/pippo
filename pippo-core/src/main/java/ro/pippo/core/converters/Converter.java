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
package ro.pippo.core.converters;

import java.util.Locale;

/**
 * General purpose data type converter that converts an incoming
 * {@code String[]} to an {@code Object} of type {@code T}.
 *
 * @param <T>
 *            the desired result type
 */
public interface Converter<T> {

    /**
     * Convert the specified input object into an output object of the specified
     * type.
     *
     * @param values
     *            The input value to be converted
     * @param locale
     *            The locale
     * @param pattern
     *            The convertion pattern
     *
     * @return The converted value
     */
    public T getAsObject(String[] values, Locale locale, String pattern);

}
