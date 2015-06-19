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
package ro.pippo.core;

import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

import java.util.Map;

/**
 * A very simple route handler that redirects a request to a path via
 * {@link ro.pippo.core.route.RouteContext#redirect(String)} or
 * {@link ro.pippo.core.route.RouteContext#redirect(String, java.util.Map)}.
 *
 * @author James Moger
 */
public class RedirectHandler implements RouteHandler {

    private String path;
    private String nameOrUriPattern;
    private Map<String, Object> parameters;

    public RedirectHandler(String path) {
        this.path = path;
    }

    public RedirectHandler(String nameOrUriPattern, Map<String, Object> parameters) {
        this.nameOrUriPattern = nameOrUriPattern;
        this.parameters = parameters;
    }

    @Override
    public void handle(RouteContext routeContext) {
        if (path != null) {
            routeContext.redirect(path);
        } else {
            routeContext.redirect(nameOrUriPattern, parameters);
        }
    }

}
