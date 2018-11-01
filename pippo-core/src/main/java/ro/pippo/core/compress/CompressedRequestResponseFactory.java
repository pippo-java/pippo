/*
 * Copyright (C) 2018 the original author or authors.
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
package ro.pippo.core.compress;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.RequestResponse;
import ro.pippo.core.RequestResponseFactory;
import ro.pippo.core.Response;
import ro.pippo.core.util.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CompressedRequestResponseFactory extends RequestResponseFactory {

    public CompressedRequestResponseFactory(Application application) {
        super(application);
    }

    @Override
    public RequestResponse createRequestResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = new Request(httpServletRequest, application);
        HttpServletResponseWrapper responseWrapper = HttpUtils.getHttpResponseWrapper(httpServletResponse, httpServletRequest);
        Response response = new Response(responseWrapper, application);

        return new RequestResponse(request, response);
    }
}
