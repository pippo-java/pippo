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
package ro.fortsoft.pippo.metrics;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.RouteMatch;

import com.codahale.metrics.MetricRegistry;

/**
 * @author James Moger
 */
public class MetricsRouteHandlerChain implements RouteHandlerChain {

	private static final Logger log = LoggerFactory.getLogger(MetricsRouteHandlerChain.class);

	private final MetricRegistry metricRegistry;
	private final Request request;
	private final Response response;
	private final Iterator<RouteMatch> iterator;

	public MetricsRouteHandlerChain(MetricRegistry metricRegistry, Request request, Response response, List<RouteMatch> routeMatches) {
		this.metricRegistry = metricRegistry;
		this.request = request;
		this.response = response;
		this.iterator = routeMatches.iterator();
	}

	@Override
	public void next() {
        // TODO it's an idea to throw an exception (NotNextRouteException or similar) ?!
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
            RouteHandler handler = route.getRouteHandler();

            try {
            	Method method = route.getRouteHandler().getClass().getMethod("handle", Request.class, Response.class, RouteHandlerChain.class);
            	String metricName;

            	if (handler instanceof ControllerHandler) {
            		ControllerHandler controllerHandler = (ControllerHandler) handler;
            		metricName = MetricRegistry.name(controllerHandler.getControllerClass(), controllerHandler.getMethodName());
            	} else {
            		metricName = MetricRegistry.name(method.getDeclaringClass(), method.getName());
            	}

            	if (method.isAnnotationPresent(Metered.class)) {
            		// route handler is Metered
            		Metered metered = method.getAnnotation(Metered.class);
            		if (!metered.value().isEmpty()) {
          				metricName = metered.value();
            		}
            		handler = new MeteredRouteHandler(metricName, route.getRouteHandler(), metricRegistry);
            	} else if (method.isAnnotationPresent(Timed.class)) {
            		// route handler is Timed
            		Timed timed = method.getAnnotation(Timed.class);
            		if (!timed.value().isEmpty()) {
          				metricName = timed.value();
            		}
            		handler = new TimedRouteHandler(metricName, route.getRouteHandler(), metricRegistry);
            	} else if (method.isAnnotationPresent(Counted.class)) {
            		// route handler is Counted
            		Counted counted = method.getAnnotation(Counted.class);
            		if (!counted.value().isEmpty()) {
          				metricName = counted.value();
            		}
            		handler = new CountedRouteHandler(metricName, counted.active(), route.getRouteHandler(), metricRegistry);
            	}
            } catch (Exception e) {
            	log.error("Failed to get method?!", e);
            }

            handler.handle(request, response, this);
        }
    }
}
