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
import ro.pippo.core.route.RoutePostDispatchListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Decebal Suiu
 */
public class GZipRequestResponseFactory extends RequestResponseFactory implements RoutePostDispatchListener {

    public GZipRequestResponseFactory(Application application) {
        super(application);

        application.getRoutePostDispatchListeners().add(this);
    }

    @Override
    public RequestResponse createRequestResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Request request = new Request(httpServletRequest, application);
        Response response;

        boolean acceptsGZipEncoding = acceptsGZipEncoding(httpServletRequest);
        if (acceptsGZipEncoding) {
            GZipResponseWrapper responseWrapper = new GZipResponseWrapper(httpServletResponse);
            response = new Response(responseWrapper, application);
        } else {
            response = new Response(httpServletResponse, application);
        }

        return new RequestResponse(request, response);
    }

    @Override
    public void onPostDispatch(Request request, Response response) {
        HttpServletResponse httpServletResponse = response.getHttpServletResponse();
        if (httpServletResponse instanceof GZipResponseWrapper) {
            GZipResponseWrapper responseWrapper = (GZipResponseWrapper) httpServletResponse;
            responseWrapper.finish();
        }
    }

    protected boolean acceptsGZipEncoding(HttpServletRequest httpServletRequest) {
        String acceptEncoding = httpServletRequest.getHeader("accept-encoding");

        return acceptEncoding != null && acceptEncoding.contains("gzip");
    }

}
