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

import ro.pippo.core.route.RouteContext;

/**
 * @author James Moger
 */
public class StatusCodeExceptionHandler implements ExceptionHandler {

    private final ErrorHandler errorHandler;

    public StatusCodeExceptionHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void handle(Exception exception, RouteContext routeContext) {
        StatusCodeException statusCodeException = (StatusCodeException) exception;
        routeContext.setLocal("message", statusCodeException.getMessage());
        errorHandler.handle(statusCodeException.getStatusCode(), routeContext);
    }
}
