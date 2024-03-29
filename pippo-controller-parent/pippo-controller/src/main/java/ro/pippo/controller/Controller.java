/*
 * Copyright (C) 2014-present the original author or authors.
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
package ro.pippo.controller;

import ro.pippo.core.Messages;
import ro.pippo.core.PippoSettings;
import ro.pippo.core.Request;
import ro.pippo.core.Response;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteDispatcher;

/**
 * Superclass for all controllers.
 * This class contains only helper methods which facilitates the interaction with {@link Request} and {@link Response}.
 *
 * @author Decebal Suiu
 */
public class Controller {

    public RouteContext getRouteContext() {
        return RouteDispatcher.getRouteContext();
    }

    public Request getRequest() {
        return getRouteContext().getRequest();
    }

    public Response getResponse() {
        return getRouteContext().getResponse();
    }

    @SuppressWarnings("unchecked")
    public <T extends ControllerApplication> T getApplication() {
        return (T) getRouteContext().getApplication();
    }

    public Messages getMessages() {
        return getRouteContext().getMessages();
    }

    public PippoSettings getSettings() {
        return getRouteContext().getSettings();
    }

}
