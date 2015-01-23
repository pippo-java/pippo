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
package ro.pippo.core;

/**
 * @author Decebal Suiu
 */
public class PippoRuntimeException extends RuntimeException {

    public PippoRuntimeException() {
    }

    public PippoRuntimeException(String message, Object... parameters) {
        super(format(message, parameters));
    }

    public PippoRuntimeException(String message, Throwable cause, Object... parameters) {
        super(format(message, parameters), cause);
    }

    public PippoRuntimeException(Throwable cause) {
        super(cause);
    }

    public PippoRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Format the message. Replace "{}" with %s and format the message using String.format
     */
    static String format(String message, Object... parameters) {
        message = message.replaceAll("\\{\\}", "%s");
        return String.format(message, parameters);
    }

}
