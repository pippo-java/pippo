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
package ro.pippo.controller;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a possible response of a Fathom Controller method.
 * This annotation my be applied to a Fathom Controller class or method.
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Returns.class)
public @interface Return {

    /**
     * The HTTP status code to send in the response.
     */
    int code();

    /**
     * Human-readable description to accompany a response.
     */
    String description() default "";

    /**
     * Description localization key for messages.properties lookup.
     *
     * If this value is non-empty, a localized variant of the description will be retrieved
     * from messages.properties with a fallback to value().
     */
    String descriptionKey() default "";

    /**
     * Describes an expected result type or a expected thrown exception.
     */
    Class<?> onResult() default Void.class;

    /**
     * A list of headers that are returned with the response.
     */
    Class<? extends ReturnHeader> [] headers() default {};

}
