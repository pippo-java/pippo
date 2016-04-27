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

package ro.pippo.core;

/**
 * Exception that results in an status code being sent to the client.
 *
 * @author James Moger
 */
public class StatusCodeException extends PippoRuntimeException {

    private final int statusCode;

    public StatusCodeException(int statusCode, String message, Object... parameters) {
        super(message, parameters);
        this.statusCode = statusCode;
    }

    public StatusCodeException(int statusCode, Throwable cause, String message, Object... parameters) {
        super(message, cause, parameters);
        this.statusCode = statusCode;
    }

    public StatusCodeException(int statusCode) {
        super(Integer.toString(statusCode));
        this.statusCode = statusCode;
    }

    public StatusCodeException(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
