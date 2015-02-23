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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Application;
import ro.pippo.core.Request;
import ro.pippo.core.Response;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author James Moger
 */
public class DefaultRouteContext implements RouteContext {

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteContext.class);

    protected final Application application;
    protected final Request request;
    protected final Response response;
    protected final Iterator<RouteMatch> iterator;

    protected DefaultRouteContext(Application application, Request request, Response response, List<RouteMatch> routeMatches) {
        this.application = application;
        this.request = request;
        this.response = response;
        this.iterator = routeMatches.iterator();
    }

    @Override
    public Application getApplication() {
        return application;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public Response getResponse() {
        return response;
    }

    @Override
    public void next() {
        if (iterator.hasNext()) {
            // retrieves the next route
            RouteMatch routeMatch = iterator.next();
            Route route = routeMatch.getRoute();
            log.debug("Found {}", route);

            // set the new path parameters in request
            Map<String, String> pathParameters = routeMatch.getPathParameters();
            if (pathParameters != null) {
                request.setPathParameters(pathParameters);
                log.debug("Added path parameters to request");
            }

            // remove route from chain
            iterator.remove();

            log.debug("Call handler for {}", route);
            handleRoute(route);
        }
    }

    /**
     * Execute all routes that are flagged to run as finally.
     */
    @Override
    public void runFinallyRoutes() {
        while (iterator.hasNext()) {
            RouteMatch routeMatch = iterator.next();
            if (routeMatch.getRoute().isRunAsFinally()) {
                try {
                    handleRoute(routeMatch.getRoute());
                } catch (Exception e) {
                    log.error("Unexpected error in Finally Route", e);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("chain.next() not called, skipping {}", routeMatch);
            }
        }
    }

    protected void handleRoute(Route route) {
        route.getRouteHandler().handle(this);
    }
}
