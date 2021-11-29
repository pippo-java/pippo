/*
 * Copyright (C) 2016-present the original author or authors.
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
package ro.pippo.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author James Moger
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {

    /**
     * HTTP GET controllerMethod.
     */
    String GET = "GET";

    /**
     * HTTP PATCH controllerMethod.
     */
    String PATCH = "PATCH";

    /**
     * HTTP POST controllerMethod.
     */
    String POST = "POST";

    /**
     * HTTP PUT controllerMethod.
     */
    String PUT = "PUT";

    /**
     * HTTP DELETE controllerMethod.
     */
    String DELETE = "DELETE";

    /**
     * HTTP HEAD controllerMethod.
     */
    String HEAD = "HEAD";

    /**
     * HTTP OPTIONS controllerMethod.
     */
    String OPTIONS = "OPTIONS";

    /**
     * ANY filter.
     */
    String ANY = "ANY";

    /**
     * @deprecated Replaced by {@link #ANY}.
     */
    @Deprecated
    String ALL = ANY;

    /**
     * Specifies the name of an HTTP controllerMethod. E.g. "GET".
     */
    String value();

}
