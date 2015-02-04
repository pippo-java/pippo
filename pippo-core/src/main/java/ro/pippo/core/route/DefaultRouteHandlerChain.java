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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.Request;
import ro.pippo.core.Response;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class DefaultRouteHandlerChain implements RouteHandlerChain {

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteHandlerChain.class);

    protected Request request;
    protected Response response;
    protected Iterator<RouteMatch> iterator;

    public DefaultRouteHandlerChain(Request request, Response response, List<RouteMatch> routeMatches) {
        this.request = request;
        this.response = response;

        iterator = routeMatches.iterator();
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
        route.getRouteHandler().handle(request, response, this);
    }

}
