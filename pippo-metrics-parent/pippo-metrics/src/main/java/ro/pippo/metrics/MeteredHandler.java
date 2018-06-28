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
package ro.pippo.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import ro.pippo.core.Application;
import ro.pippo.core.route.RouteContext;
import ro.pippo.core.route.RouteHandler;

/**
 * It's a decorator route handler that add a {@link Meter} metric.
 *
 * @author James Moger
 */
public class MeteredHandler implements RouteHandler {

    final String meterName;
    final MetricRegistry metricRegistry;
    final RouteHandler routeHandler;

    /**
     * This constructor uses {@link SharedMetricRegistries#getDefault()} as metric registry.
     * The default (global) metric registry is set in {@link MetricsInitializer#init(Application)}.
     */
    public MeteredHandler(String meterName, RouteHandler routeHandler) {
        this(meterName, SharedMetricRegistries.getDefault(), routeHandler);
    }

    public MeteredHandler(String meterName, MetricRegistry metricRegistry, RouteHandler routeHandler) {
        this.meterName = meterName;
        this.metricRegistry = metricRegistry;
        this.routeHandler = routeHandler;
    }

    @Override
    public void handle(RouteContext routeContext) {
        Meter meter = metricRegistry.meter(meterName);
        meter.mark();

        routeHandler.handle(routeContext);
    }

}
