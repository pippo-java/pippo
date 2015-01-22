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

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.controller.ControllerHandler;
import ro.fortsoft.pippo.core.route.DefaultRouteHandlerChain;
import ro.fortsoft.pippo.core.route.Route;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;
import ro.fortsoft.pippo.core.route.RouteMatch;
import ro.fortsoft.pippo.core.util.LangUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author James Moger
 */
public class MetricsRouteHandlerChain extends DefaultRouteHandlerChain {

	private static final Logger log = LoggerFactory.getLogger(MetricsRouteHandlerChain.class);

	private final MetricRegistry metricRegistry;

	public MetricsRouteHandlerChain(MetricRegistry metricRegistry, Request request, Response response, List<RouteMatch> routeMatches) {
		super(request, response, routeMatches);

		this.metricRegistry = metricRegistry;
	}

	@Override
	protected void handleRoute(Route route) {
		RouteHandler handler = route.getRouteHandler();

		try {
			Method method;
			if (handler instanceof ControllerHandler) {
				ControllerHandler controllerHandler = (ControllerHandler) handler;
				method = controllerHandler.getMethod();
			} else {
				method = route.getRouteHandler().getClass().getMethod("handle", Request.class, Response.class, RouteHandlerChain.class);
			}

			String metricName = MetricRegistry.name(method.getDeclaringClass(), method.getName());

			if (method.isAnnotationPresent(Metered.class)) {
				log.debug("Found '{}' annotation on method '{}'", Metered.class.getSimpleName(), LangUtils.toString(method));
				// route handler is Metered
				Metered metered = method.getAnnotation(Metered.class);
				if (!metered.value().isEmpty()) {
					metricName = metered.value();
				}
				handler = new MeteredRouteHandler(metricName, route.getRouteHandler(), metricRegistry);
			} else if (method.isAnnotationPresent(Timed.class)) {
				log.debug("Found '{}' annotation on method '{}'", Timed.class.getSimpleName(), LangUtils.toString(method));
				// route handler is Timed
				Timed timed = method.getAnnotation(Timed.class);
				if (!timed.value().isEmpty()) {
					metricName = timed.value();
				}
				handler = new TimedRouteHandler(metricName, route.getRouteHandler(), metricRegistry);
			} else if (method.isAnnotationPresent(Counted.class)) {
				log.debug("Found '{}' annotation on method '{}'", Counted.class.getSimpleName(), LangUtils.toString(method));
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

		logUnhandledRoutes();
	}

}
