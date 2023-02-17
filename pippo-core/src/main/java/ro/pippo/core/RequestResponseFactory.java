/*
 * Copyright (C) 2015-present the original author or authors.
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Helps in creating <code>Request<code/> and <code>Response<code/> objects.
 * Here you can wrap/customize HttpServletRequest or HttpServletResponse.
 *
 * @author Decebal Suiu
 */
public class RequestResponseFactory {

    protected final Application application;

    public RequestResponseFactory(Application application) {
        this.application = application;
    }

    public RequestResponse createRequestResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = new Request(httpServletRequest, application);
        Response response = new Response(httpServletResponse, application);

        return new RequestResponse(request, response);
    }

}
