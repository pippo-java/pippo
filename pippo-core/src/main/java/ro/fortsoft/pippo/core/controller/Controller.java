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
package ro.fortsoft.pippo.core.controller;

import ro.fortsoft.pippo.core.Application;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

/**
 * @author Decebal Suiu
 */
public class Controller {

    private Request request;
    private Response response;
    private RouteHandlerChain chain;

    public final Request getRequest() {
        return request;
    }

    public final Response getResponse() {
        return response;
    }

    public final RouteHandlerChain getChain() {
        return chain;
    }

    public Application getApplication() {
        return Application.get();
    }

    protected void init(Request request, Response response, RouteHandlerChain chain) {
        this.request = request;
        this.response = response;
        this.chain = chain;
    }

}
