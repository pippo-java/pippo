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

import ro.fortsoft.pippo.core.Request;
import ro.fortsoft.pippo.core.Response;
import ro.fortsoft.pippo.core.route.RouteHandler;
import ro.fortsoft.pippo.core.route.RouteHandlerChain;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**
 * @author James Moger
 */
public class MeteredRouteHandler implements RouteHandler {

	final String meterName;
	final RouteHandler routeHandler;
	final MetricRegistry metricRegistry;

	public MeteredRouteHandler(String meterName, RouteHandler routeHandler, MetricRegistry metricRegistry) {
		this.meterName = meterName;
		this.routeHandler = routeHandler;
		this.metricRegistry = metricRegistry;
	}

	@Override
	public void handle(Request request, Response response, RouteHandlerChain chain) {
		Meter meter = metricRegistry.meter(meterName);
		meter.mark();

		try {
			routeHandler.handle(request, response, chain);
		} finally {

		}
	}

}
