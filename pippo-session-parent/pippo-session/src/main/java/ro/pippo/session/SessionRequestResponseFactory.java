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
package ro.pippo.session;

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
public class SessionRequestResponseFactory extends RequestResponseFactory {

    private final SessionManager sessionManager;

    public SessionRequestResponseFactory(Application application, SessionManager sessionManager) {
        super(application);

        this.sessionManager = sessionManager;
    }

    @Override
    public RequestResponse createRequestResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        SessionHttpServletRequest sessionHttpServletRequest = new SessionHttpServletRequest(httpServletRequest, sessionManager);
        Request request = new Request(sessionHttpServletRequest, application);
        Response response = new Response(httpServletResponse, application);
        response.getFinalizeListeners().add(r -> sessionHttpServletRequest.commitSession(httpServletResponse));

        return new RequestResponse(request, response);
    }

}
