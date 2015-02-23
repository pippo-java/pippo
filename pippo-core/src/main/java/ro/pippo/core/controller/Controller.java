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
package ro.pippo.core.controller;

import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.RouteContext;
import ro.pippo.core.route.RouteHandlerChain;

/**
 * @author Decebal Suiu
 */
public class Controller {

    private RouteContext routeContext;
    private RouteHandlerChain chain;

    public final RouteContext getRouteContext() {
        return routeContext;
    }

    public final Request getRequest() {
        return routeContext.getRequest();
    }

    public final Response getResponse() {
        return routeContext.getResponse();
    }

    public final RouteHandlerChain getChain() {
        return chain;
    }

    public Application getApplication() {
        return Application.get();
    }

    protected void init(RouteContext routeContext, RouteHandlerChain chain) {
        this.routeContext = routeContext;
        this.chain = chain;
    }

}
