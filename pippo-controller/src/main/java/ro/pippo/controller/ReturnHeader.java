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

import java.util.Date;
import java.util.UUID;

/**
 * Used to declare a response header in a Return declaration and to optionally validate it.
 *
 * @author James Moger
 */
public interface ReturnHeader {

    /**
     * Returns the name of the header.
     *
     * @return the header name
     */
    String getHeaderName();

    /**
     * Return the Java type this header represents.
     *
     * @return the Java type of the header
     */
    Class<?> getHeaderType();

    /**
     * Specifies the default value returned if no value was set.
     *
     * @return the default value
     */
    String getDefaultValue();

    /**
     * Validate the response header value.
     *
     * @param value
     * @return true if the response header value is valid
     */
    boolean validate(String value);

    abstract class IntHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return int.class;
        }

    }

    abstract class LongHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return long.class;
        }

    }

    abstract class FloatHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return float.class;
        }

    }

    abstract class DoubleHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return double.class;
        }

    }

    abstract class StringHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return String.class;
        }

    }

    abstract class BooleanHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return boolean.class;
        }

    }

    abstract class DateTimeHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return Date.class;
        }

    }

    abstract class DateHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return java.sql.Date.class;
        }

    }

    abstract class UUIDHeader implements ReturnHeader {

        @Override
        public final Class<?> getHeaderType() {
            return UUID.class;
        }

    }

    abstract class EnumHeader implements ReturnHeader {

        @Override
        public abstract Class<? extends Enum> getHeaderType();

    }

    /**
     * Used to declare a response header that represents an array,
     */
    interface ReturnHeaderArray extends ReturnHeader {

        enum Delimiter { csv, ssv, tsv, pipes }

        /**
         * The delimiter of the array.
         *
         * csv - comma separated value (foo,bar)
         * ssv - space separated value (foo bar)
         * tsv - tab separated value (foo\tbar)
         * pipes - pipe separated value (foo|bar)
         *
         * @return the delimiter
         */
        Delimiter getDelimiter();

    }

    abstract class IntArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return int[].class;
        }

    }

    abstract class LongArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return long[].class;
        }

    }

    abstract class FloatArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return float[].class;
        }

    }

    abstract class DoubleArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return double[].class;
        }

    }

    abstract class StringArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return String[].class;
        }

    }

    abstract class BooleanArrayHeader implements ReturnHeaderArray {

        @Override
        public final Class<?> getHeaderType() {
            return boolean[].class;
        }

    }

    abstract class EnumArrayHeader implements ReturnHeaderArray {

        @Override
        public abstract Class<? extends Enum[]> getHeaderType();

    }

}
