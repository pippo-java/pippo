/*
 * Copyright (C) 2016 the original author or authors.
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
package ro.pippo.core.gzip;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.RequestResponse;
import ro.pippo.core.RequestResponseFactory;
import ro.pippo.core.Response;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class GZipRequestResponseFactory extends RequestResponseFactory {

    public GZipRequestResponseFactory(Application application) {
        super(application);
    }

    @Override
    public RequestResponse createRequestResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = new Request(httpServletRequest, application);
        Response response;

        boolean acceptsGZipEncoding = acceptsGZipEncoding(httpServletRequest);
        if (acceptsGZipEncoding) {
            // the response with be finished in Response.finishGZip() method
            GZipResponseWrapper responseWrapper = new GZipResponseWrapper(httpServletResponse);
            response = new Response(responseWrapper, application);
        } else {
            response = new Response(httpServletResponse, application);
        }

        return new RequestResponse(request, response);
    }

    protected boolean acceptsGZipEncoding(HttpServletRequest httpServletRequest) {
        String acceptEncoding = httpServletRequest.getHeader("accept-encoding");

        return acceptEncoding != null && acceptEncoding.contains("gzip");
    }

}
